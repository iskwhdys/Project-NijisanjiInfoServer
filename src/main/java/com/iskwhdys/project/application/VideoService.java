package com.iskwhdys.project.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoFactory;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.domain.video.VideoSpecification;
import com.iskwhdys.project.infra.twitter.TwitterApi;
import com.iskwhdys.project.infra.youtube.ChannelFeedXml;
import com.twitter.twittertext.TwitterTextParser;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class VideoService {

  @Autowired ChannelRepository channelRepository;
  @Autowired VideoRepository videoRepository;
  @Autowired VideoThumbnailService videoThumbnailService;

  @Autowired VideoFactory videoFactory;

  @Autowired TwitterApi twitterApi;

  @Autowired VideoSpecification videoApi;

  public List<VideoEntity> update5min() {
    return update(false, false, false);
  }

  public List<VideoEntity> update20min() {
    return update(true, true, false);
  }

  public List<VideoEntity> allMaintenace() {
    return update(true, true, true);
  }

  private List<VideoEntity> update(
      boolean isUpdateLiveVideos, boolean isUpdateEccentricVideos, boolean isAllThumbnailUopdate) {

    // 全チャンネルのRssXmlから動画情報Elementを取得
    Map<String, Element> elements = ChannelFeedXml.getVideoElement(getAllChannelId());
    List<VideoEntity> videos = new ArrayList<>();

    // 全動画情報Elementを元にEntityを作成 or 情報の更新
    for (var set : elements.entrySet()) {
      Element element = set.getValue();

      var video = videoRepository.findById(set.getKey()).orElse(null);
      if (video == null) {
        video = createNewVideo(element);
      } else if (video.isUpload() || video.isPremierUpload()) {
        updateUploadVideo(element, video);
      } else if (video.isLiveArchive()) {
        updateLiveArchiveVideo(element, video);
      } else if (video.isPremierLive() || video.isLiveLive()) {
        updateLiveVideo(element, video, isUpdateLiveVideos);
      } else if (video.isPremierReserve() || video.isLiveReserve()) {
        updateReserveVideo(element, video);
      } else if (video.isUnknown()) {
        updateUnknownVideo(element, video);
      }
      videos.add(video);
    }

    var videoIds = videos.stream().map(VideoEntity::getId).collect(Collectors.toList());
    videos.addAll(updateNoXmlLives(videoIds, isUpdateEccentricVideos));
    videos.addAll(updateNoXmlTodayVideos(videoIds));

    if (isAllThumbnailUopdate) {
      videoIds = videos.stream().map(VideoEntity::getId).collect(Collectors.toList());
      videos.addAll(updateOtherVideos(videoIds));
    }

    videoRepository.saveAll(videos);
    return videos;
  }

  private List<String> getAllChannelId() {
    return channelRepository
        .findByEnabledTrue()
        .stream()
        .map(ChannelEntity::getId)
        .collect(Collectors.toList());
  }

  /**
   * XMLにないライブ情報の更新（非公開系？ライブ完了してホーム(XML)に公開されるまでの動画がここに来た）
   *
   * @param videoIds
   * @return
   */
  private List<VideoEntity> updateNoXmlLives(
      List<String> videoIds, boolean isUpdateEccentricVideos) {
    var videos =
        videoRepository.findByTypeInAndEnabledTrueAndIdNotInOrderByLiveStartDesc(
            VideoEntity.TYPE_LIVES, videoIds);
    for (var video : videos) {
      boolean success = videoThumbnailService.downloadThumbnails(video);
      video.setEnabled(success);
      if (success && isUpdateEccentricVideos) {
        videoApi.updateEntity(video);
        log.info("API None ->" + video.getType() + " " + video.toString());
      } else {
        log.info("XML None ->" + video.getType() + " " + video.toString());
      }
    }
    return videos;
  }

  /**
   * 24時間以内に公開された動画類でXMLに無いもの（ライブ終了直後でXMLに反映されてないもの）
   *
   * @param videoIds
   * @return
   */
  private List<VideoEntity> updateNoXmlTodayVideos(List<String> videoIds) {
    var videos = videoRepository.findByIdNotInAndTodayVideos(videoIds);
    for (var video : videos) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      log.info("Private ->" + video.getType() + " " + video.toString());
    }
    return videos;
  }

  private List<VideoEntity> updateOtherVideos(List<String> videoIds) {
    var videos = videoRepository.findByEnabledTrueAndIdNotIn(videoIds);
    for (var video : videos) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      log.info("Other ->" + video.getType() + " " + video.toString());
    }
    return videos;
  }

  private VideoEntity createNewVideo(Element element) {
    var video = videoFactory.createViaXmlElement(element);

    videoThumbnailService.downloadThumbnails(video);
    videoApi.updateEntity(video);
    if (video.isPremierLive() || video.isLiveLive()) {
      tweet(video);
    }
    log.info("API New -> " + video.getType() + " " + video.toString());

    return video;
  }

  private void updateUploadVideo(Element element, VideoEntity video) {
    videoFactory.updateViaXmlElement(element, video);

    if ((new Date().getTime() - video.getUploadDate().getTime()) < 1000 * 60 * 60 * 24) {
      // 公開して24時間以内の動画はサムネイルを更新する
      boolean success = videoThumbnailService.downloadThumbnails(video);
      // サムネ更新が出来ない == 動画が非公開
      video.setEnabled(success);
      log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
    }
  }

  private void updateLiveArchiveVideo(Element element, VideoEntity video) {

    videoFactory.updateViaXmlElement(element, video);

    if ((new Date().getTime() - video.getLiveStart().getTime()) < 1000 * 60 * 60 * 24) {
      // 配信して24時間以内の動画はサムネイルを更新する
      boolean success = videoThumbnailService.downloadThumbnails(video);
      // サムネ更新が出来ない == 動画が非公開
      video.setEnabled(success);
      log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
    }
  }

  private void updateLiveVideo(Element element, VideoEntity video, boolean isUpdateLiveVideos) {

    videoFactory.updateViaXmlElement(element, video);
    videoThumbnailService.downloadThumbnails(video);

    if (isUpdateLiveVideos) {
      videoApi.updateLiveInfoViaApi(video);
      if (video.isPremierUpload() || video.isLiveArchive()) {
        videoApi.updateLiveToArchiveInfoViaApi(video);
      }
      log.info("API Live -> " + video.getType() + " " + video.toString());
    } else {
      // log.info("XML Live -> " + video.getType() + " " + video.toString());
    }
  }

  private void updateReserveVideo(Element element, VideoEntity video) {

    videoFactory.updateViaXmlElement(element, video);
    boolean success = videoThumbnailService.downloadThumbnails(video);
    video.setEnabled(success);

    // 無効な動画は除外
    if (Boolean.FALSE.equals(video.getEnabled())) return;
    // 配信予定日時を過ぎてない動画は除外
    if (new Date().getTime() < video.getLiveSchedule().getTime()) return;
    // 配信予定日時が24時間を超えた動画は除外
    if ((new Date().getTime() - video.getLiveSchedule().getTime()) > 1000 * 60 * 60 * 24) return;

    videoApi.updateReserveInfoViaApi(video);
    if (video.isPremierUpload() || video.isLiveArchive()) {
      videoApi.updateLiveToArchiveInfoViaApi(video);
    }
    if (video.isPremierLive() || video.isLiveLive()) {
      tweet(video);
    }
    log.info("API Reserve -> " + video.getType() + " " + video.toString());
  }

  private void updateUnknownVideo(Element element, VideoEntity video) {
    videoFactory.updateViaXmlElement(element, video);
    videoThumbnailService.downloadThumbnails(video);
    videoApi.updateEntity(video);
    log.info("API Unknown -> " + video.getType() + " " + video.toString());
  }

  public void tweet(String id) {
    var opt = videoRepository.findById(id);
    if (opt.isPresent()) {
      tweet(opt.get());
    } else {
      log.warn("Videoがありません。：" + id);
    }
  }

  private void tweet(VideoEntity video) {
    String msg = getTweetMessage(video, true, true);
    if (TwitterTextParser.parseTweet(msg).isValid) {
      twitterApi.tweet(msg);
      return;
    }

    msg = getTweetMessage(video, false, true);
    if (TwitterTextParser.parseTweet(msg).isValid) {
      twitterApi.tweet(msg);
      return;
    }

    msg = getTweetMessage(video, false, false);
    twitterApi.tweet(msg);
  }

  private String getTweetMessage(VideoEntity video, boolean useChannel, boolean use2j3jInfo) {
    var result = new StringBuilder();
    result.append("～配信開始～").append("\r\n");

    if (useChannel) {
      var channel = channelRepository.findById(video.getChannelId());
      if (channel.isPresent()) {
        result.append(channel.get().getTitle()).append("\r\n");
      }
    }

    result.append(video.getTitle()).append("\r\n");
    result.append("https://www.youtube.com/watch?v=" + video.getId());

    if (use2j3jInfo) {
      result.append("\r\n");
      result.append("\r\n");
      result.append("～その他の配信情報はこちら～\r\n");
      result.append("にじさんじライブ新着 http://nijisanji-live.com");
    }

    return result.toString();
  }
}
