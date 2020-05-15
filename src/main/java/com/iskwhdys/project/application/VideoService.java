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

  @Autowired VideoRepository videoRepository;

  @Autowired VideoSpecification videoSpecification;
  @Autowired VideoFactory videoFactory;
  @Autowired VideoThumbnailService videoThumbnailService;
  @Autowired TweetService tweetService;
  @Autowired ChannelService channelService;

  public void xmlUpdate() {

    Map<String, Element> elements = ChannelFeedXml.getVideoElement(channelService.getIds());
    List<VideoEntity> videos = new ArrayList<>();

    for (var set : elements.entrySet()) {
      Element element = set.getValue();
      var video = videoRepository.findById(set.getKey()).orElse(null);

      if (video == null) {
        video = videoFactory.createViaXmlElement(element);
        videoThumbnailService.downloadThumbnails(video);
      } else {
        videoFactory.updateViaXmlElement(element, video);
      }

      video.setUpdateDate(new Date());
      video.setEnabled(true);
      video.setType(VideoEntity.TYPE_UPLOAD);
      videos.add(video);
    }

    videoRepository.saveAll(videos);
  }

  public void update(int intervalMinute, boolean isAllThumbnailUpdate) {

    Map<String, Element> elements = ChannelFeedXml.getVideoElement(channelService.getIds());
    List<VideoEntity> videos = new ArrayList<>();

    for (var set : elements.entrySet()) {
      Element element = set.getValue();
      var video = videoRepository.findById(set.getKey()).orElse(null);

      boolean added = false;
      if (video == null) {
        video = createNewVideo(element);
      } else {
        if (ChannelFeedXml.isUncachedElement(element)) {
          videoFactory.updateViaXmlElement(element, video);
          videos.add(video);
          added = true;
        }
        video = updateVideo(video, intervalMinute);
      }

      if (video != null && !added) {
        video.setUpdateDate(new Date());
        videos.add(video);
      }
    }

    int xmlSize = videos.size();
    var videoIds = videos.stream().map(VideoEntity::getId).collect(Collectors.toList());
    videos.addAll(updateNoXmlVideos(videoIds, intervalMinute, isAllThumbnailUpdate));

    log.info("VideoCount:" + xmlSize + "/" + videos.size());

    videoRepository.saveAll(videos);
  }

  public void updateAllReserve() {

    var videos = videoRepository.findByEnabledTrueAndTypeIn(VideoEntity.TYPE_RESERVES);

    for (VideoEntity video : videos) {
      try {
        videoSpecification.updateReserveInfoViaApi(video);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }

    videoRepository.saveAll(videos);
  }

  private VideoEntity updateVideo(VideoEntity video, int intervalMinute) {
    if (video.isUpload() || video.isPremierUpload()) {
      return updateUploadVideo(video, intervalMinute);
    } else if (video.isLiveArchive()) {
      return updateLiveArchiveVideo(video, intervalMinute);
    } else if (video.isPremierLive() || video.isLiveLive()) {
      return updateLiveVideo(video, intervalMinute);
    } else if (video.isPremierReserve() || video.isLiveReserve()) {
      return updateReserveVideo(video, intervalMinute);
    } else if (video.isUnknown()) {
      return updateUnknownVideo(video);
    }
    return null;
  }

  private List<VideoEntity> updateNoXmlVideos(
      List<String> xmlIds, int intervalMinute, boolean isAllThumbnailUopdate) {
    List<VideoEntity> videos = new ArrayList<>();
    videos.addAll(updateNoXmlLives(xmlIds, intervalMinute));
    videos.addAll(updateNoXmlTodayVideos(xmlIds, intervalMinute));
    videos.addAll(updateAllReserveVideos(xmlIds, intervalMinute));

    if (isAllThumbnailUopdate) {
      xmlIds = videos.stream().map(VideoEntity::getId).collect(Collectors.toList());
      videos.addAll(updateOtherVideos(xmlIds));
    }
    return videos;
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
        videoSpecification.updateEntity(video);
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
    if (intervalMinute < 60) return new ArrayList<>();

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
    if (intervalMinute < 60) return new ArrayList<>();

    var videos = videoRepository.findByIdNotInAndTypeAllReserve(videoIds);
    for (var video : videos) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      if (Boolean.FALSE.equals(video.getEnabled())) {
        log.info("DB  Private ->" + video.getType() + " " + video.toString());
      } else if (intervalMinute >= 60 * 24) {
        videoSpecification.updateEntity(video);
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
    videoSpecification.updateEntity(video);
    if (video.isPremierLive() || video.isLiveLive()) {
      tweetService.tweet(video);
    }
    log.info("API New -> " + video.getType() + " " + video.toString());

    return video;
  }

  private VideoEntity updateUploadVideo(VideoEntity video, int intervalMinute) {

    if (video.uploadElapsedMinute() < 60 * 24 && intervalMinute >= 60) {
      // 公開して24時間以内の動画は、1時間おきにサムネイルを更新する
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
      return video;
    } else {
      return null;
    }
  }

  private VideoEntity updateLiveArchiveVideo(VideoEntity video, int intervalMinute) {

    if (video.liveElapsedMinute() < 60 * 24 && intervalMinute >= 60) {
      // 配信から24時間以内の動画は、1時間おきにサムネイルを更新する
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
      log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
      return video;
    } else {
      return null;
    }
  }

  private VideoEntity updateLiveVideo(VideoEntity video, int intervalMinute) {

    if (videoSpecification.isUpdateLive(video, intervalMinute)) {
      videoThumbnailService.downloadThumbnails(video);
      videoSpecification.updateLiveInfoViaApi(video);

      if (video.isPremierUpload() || video.isLiveArchive()) {
        videoSpecification.updateLiveToArchiveInfoViaApi(video);
      }
      log.info("API Live -> " + video.getType() + " " + video.toString());
    } else {
      log.debug("XML Live -> " + video.getType() + " " + video.toString());
    }
    return video;
  }

  private VideoEntity updateReserveVideo(VideoEntity video, int intervalMinute) {

    if (intervalMinute >= 60) {
      video.setEnabled(videoThumbnailService.downloadThumbnails(video));
    }

    if (videoSpecification.isUpdateReserve(video, intervalMinute)) {
      videoSpecification.updateReserveInfoViaApi(video);
      if (video.isPremierUpload() || video.isLiveArchive()) {
        videoSpecification.updateLiveToArchiveInfoViaApi(video);
      }
      if (video.isPremierLive() || video.isLiveLive()) {
        tweetService.tweet(video);
      }
      log.info("API Reserve -> " + video.getType() + " " + video.toString());
    }
    return video;
  }

  private VideoEntity updateUnknownVideo(VideoEntity video) {
    videoThumbnailService.downloadThumbnails(video);
    videoSpecification.updateEntity(video);
    log.info("API Unknown -> " + video.getType() + " " + video.toString());
    return video;
  }
}
