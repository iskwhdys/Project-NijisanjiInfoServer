package com.iskwhdys.project.application;

import java.util.ArrayList;
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
import com.iskwhdys.project.infra.youtube.ChannelFeedXml;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class VideoService {

  @Autowired ChannelRepository channelRepository;
  @Autowired VideoRepository videoRepository;

  @Autowired VideoSpecification videoApi;
  @Autowired VideoFactory videoFactory;
  @Autowired VideoThumbnailService videoThumbnailService;
  @Autowired TweetService tweetService;

  public void update(int intervalMinute, boolean isAllThumbnailUopdate) {

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
        updateUploadVideo(element, video, intervalMinute);
      } else if (video.isLiveArchive()) {
        updateLiveArchiveVideo(element, video, intervalMinute);
      } else if (video.isPremierLive() || video.isLiveLive()) {
        updateLiveVideo(element, video, intervalMinute);
      } else if (video.isPremierReserve() || video.isLiveReserve()) {
        updateReserveVideo(element, video, intervalMinute);
      } else if (video.isUnknown()) {
        updateUnknownVideo(element, video);
      }
      videos.add(video);
    }

    var videoIds = videos.stream().map(VideoEntity::getId).collect(Collectors.toList());
    videos.addAll(updateNoXmlLives(videoIds, intervalMinute));
    videos.addAll(updateNoXmlTodayVideos(videoIds, intervalMinute));
    videos.addAll(updateAllReserveVideos(videoIds, intervalMinute));

    if (isAllThumbnailUopdate) {
      videoIds = videos.stream().map(VideoEntity::getId).collect(Collectors.toList());
      videos.addAll(updateOtherVideos(videoIds));
    }

    videoRepository.saveAll(videos);
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
  private List<VideoEntity> updateNoXmlLives(List<String> videoIds, int intervalMinute) {
    var videos =
        videoRepository.findByTypeInAndEnabledTrueAndIdNotInOrderByLiveStartDesc(
            VideoEntity.TYPE_LIVES, videoIds);

    for (var video : videos) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      if (Boolean.FALSE.equals(video.getEnabled())) continue;
      if (intervalMinute == 1) {
        log.info("XML None ->" + video.getType() + " " + video.toString());
      } else {
        videoApi.updateEntity(video);
        log.info("API None ->" + video.getType() + " " + video.toString());
      }
    }
    return videos;
  }

  /**
   * 24時間以内に公開された動画類でXMLに無いもの（ライブ終了直後でXMLに反映されてないもの or 限定公開）
   *
   * @param videoIds
   * @return
   */
  private List<VideoEntity> updateNoXmlTodayVideos(List<String> videoIds, int intervalMinute) {
    if (intervalMinute < 20) return new ArrayList<>();

    var videos = videoRepository.findByIdNotInAndTodayVideos(videoIds);
    for (var video : videos) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      log.info("DB  Private ->" + video.getType() + " " + video.toString());
    }
    return videos;
  }


  /**
   * 全予約動画の更新
   *
   * @param videoIds
   * @return
   */
  private List<VideoEntity> updateAllReserveVideos(List<String> videoIds, int intervalMinute) {
    if (intervalMinute < 60 * 24) return new ArrayList<>();

    var videos = videoRepository.findByIdNotInAndTypeAllReserve(videoIds);
    for (var video : videos) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      if(Boolean.FALSE.equals(video.getEnabled())) {
        log.info("DB  Private ->" + video.getType() + " " + video.toString());
      }
      else {
        videoApi.updateEntity(video);
        log.info("API Private ->" + video.getType() + " " + video.toString());
      }
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
      tweetService.tweet(video);
    }
    log.info("API New -> " + video.getType() + " " + video.toString());

    return video;
  }

  private void updateUploadVideo(Element element, VideoEntity video, int intervalMinute) {
    videoFactory.updateViaXmlElement(element, video);

    if (video.uploadElapsedMinute() < 60 * 24 && intervalMinute >= 60) {
      // 公開して24時間以内の動画は、1時間おきにサムネイルを更新する
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
    }
  }

  private void updateLiveArchiveVideo(Element element, VideoEntity video, int intervalMinute) {

    videoFactory.updateViaXmlElement(element, video);

    if (video.liveElapsedMinute() < 60 * 24 && intervalMinute >= 60) {
      // 配信から24時間以内の動画は、1時間おきにサムネイルを更新する
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
    }
  }

  private void updateLiveVideo(Element element, VideoEntity video, int intervalMinute) {

    videoFactory.updateViaXmlElement(element, video);

    if (intervalMinute >= 20 || (intervalMinute >= 5 && (video.liveElapsedMinute() < 20))) {
      // 20分間隔以上は常時、5分間隔の場合はライブ開始20分以内なら、情報更新
      videoThumbnailService.downloadThumbnails(video);
      videoApi.updateLiveInfoViaApi(video);
      if (video.isPremierUpload() || video.isLiveArchive()) {
        videoApi.updateLiveToArchiveInfoViaApi(video);
      }
      log.info("API Live -> " + video.getType() + " " + video.toString());
    } else {
      log.debug("XML Live -> " + video.getType() + " " + video.toString());
    }
  }

  private void updateReserveVideo(Element element, VideoEntity video, int intervalMinute) {

    videoFactory.updateViaXmlElement(element, video);
    if (intervalMinute >= 60) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
    }

    long min = video.scheduleElapsedMinute();

    // 無効な動画は除外
    if (Boolean.FALSE.equals(video.getEnabled())) return;
    // 配信予定日時が24時間を超えた動画は除外
    if (video.scheduleElapsedMinute() > 60 * 24) return;
    // 配信開始一時間前より古い動画は除外
    if (video.scheduleElapsedMinute() <= -60) return;

    //  5分更新　：ライブ開始一時間前
    boolean i0 = min < 0 && intervalMinute >= 5;
    // 無条件更新：ライブ開始から20分以内
    boolean i1 = min >= 0 && min < 20;
    //  5分更新　：ライブ開始から1時間以内
    boolean i2 = min >= 0 && min < 60 && intervalMinute >= 5;
    // 20分更新　：ライブ開始から2時間以内
    boolean i3 = min >= 0 && min < 60 * 2 && intervalMinute >= 20;
    // 60分更新　：ライブ開始から24時間以内
    boolean i4 = min >= 0 && min < 60 * 24 && intervalMinute >= 60;
    if (i0 || i1 || i2 || i3 || i4) {
      videoApi.updateReserveInfoViaApi(video);
      if (video.isPremierUpload() || video.isLiveArchive()) {
        videoApi.updateLiveToArchiveInfoViaApi(video);
      }
      if (video.isPremierLive() || video.isLiveLive()) {
        tweetService.tweet(video);
      }
      log.info("API Reserve -> " + video.getType() + " " + video.toString());
    }
  }

  private void updateUnknownVideo(Element element, VideoEntity video) {
    videoFactory.updateViaXmlElement(element, video);
    videoThumbnailService.downloadThumbnails(video);
    videoApi.updateEntity(video);
    log.info("API Unknown -> " + video.getType() + " " + video.toString());
  }
}
