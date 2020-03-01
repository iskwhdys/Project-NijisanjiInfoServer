package com.iskwhdys.project.domain.video;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.iskwhdys.project.Common;
import com.iskwhdys.project.infra.youtube.YoutubeApi;
import lombok.extern.slf4j.Slf4j;

@Component
@SuppressWarnings("squid:S1192")
@Slf4j
public class VideoSpecification {

  @Autowired YoutubeApi youtubeApi;

  public VideoEntity updateEntity(VideoEntity entity) {
    return updateBaseFunction(
        entity, "snippet", "statistics", "contentDetails", "liveStreamingDetails", "status");
  }

  public VideoEntity updateLiveInfoViaApi(VideoEntity entity) {
    return updateBaseFunction(entity, "liveStreamingDetails");
  }

  public VideoEntity updateLiveToArchiveInfoViaApi(VideoEntity entity) {
    return updateBaseFunction(entity, "contentDetails");
  }

  public VideoEntity updateReserveInfoViaApi(VideoEntity entity) {
    return updateBaseFunction(entity, "liveStreamingDetails");
  }

  @SuppressWarnings("unchecked")
  private VideoEntity updateBaseFunction(VideoEntity entity, String... parts) {
    var items = youtubeApi.downloadVideoItems(entity.getId(), parts);
    if (items.isEmpty()) {
      entity.setEnabled(false);
      return entity;
    }
    Map<String, ?> item = items.get(0);

    entity.setEtag(item.get("etag").toString());
    for (String part : parts) {
      var map = (Map<String, ?>) item.get(part);
      if (part.equals("snippet")) setSnippet(entity, map);
      else if (part.equals("statistics")) setStatistics(entity, map);
      else if (part.equals("contentDetails")) setContentDetails(entity, map);
      else if (part.equals("liveStreamingDetails")) setLiveStreamingDetails(entity, map);
      else if (part.equals("status")) setStatus(entity, map);
    }
    entity.setType(getType(entity));
    entity.setEnabled(true);

    return entity;
  }

  private VideoEntity setSnippet(VideoEntity video, Map<String, ?> map) {
    if (map == null) return video;
    if (map.containsKey("title")) video.setTitle(map.get("title").toString());
    if (map.containsKey("description")) video.setDescription(map.get("description").toString());
    return video;
  }

  private VideoEntity setStatistics(VideoEntity video, Map<String, ?> map) {
    if (map == null) return video;
    if (map.containsKey("viewCount")) video.setViews(toInteger(map, "viewCount"));
    if (map.containsKey("likeCount")) video.setLikes(toInteger(map, "likeCount"));
    if (map.containsKey("dislikeCount")) video.setDislikes(toInteger(map, "dislikeCount"));
    if (map.containsKey("favoriteCount")) video.setFavorites(toInteger(map, "favoriteCount"));
    if (map.containsKey("commentCount")) video.setComments(toInteger(map, "commentCount"));
    return video;
  }

  private VideoEntity setContentDetails(VideoEntity video, Map<String, ?> map) {
    if (map == null) return video;
    String duration = map.get("duration").toString();
    video.setDuration((int) Duration.parse(duration).toSeconds());
    return video;
  }

  private VideoEntity setLiveStreamingDetails(VideoEntity video, Map<String, ?> map) {
    if (map == null) return video;
    if (map.containsKey("actualStartTime")) video.setLiveStart(toDate(map, "actualStartTime"));
    if (map.containsKey("actualEndTime")) video.setLiveEnd(toDate(map, "actualEndTime"));
    if (map.containsKey("scheduledStartTime"))
      video.setLiveSchedule(toDate(map, "scheduledStartTime"));
    if (map.containsKey("concurrentViewers"))
      video.setLiveViews(toInteger(map, "concurrentViewers"));
    return video;
  }

  private VideoEntity setStatus(VideoEntity video, Map<String, ?> map) {
    if (map == null) return video;
    if (map.containsKey("uploadStatus")) video.setUploadStatus(map.get("uploadStatus").toString());
    return video;
  }

  private Date toDate(Map<String, ?> map, String key) {
    return Common.youtubeTimeToDate(map.get(key).toString());
  }

  private Integer toInteger(Map<String, ?> map, String key) {
    return Integer.parseInt(map.get(key).toString());
  }

  @SuppressWarnings("squid:S3776")
  public String getType(VideoEntity video) {
    if (video.getType() == null || video.isUnknown()) {
      // 初回
      if ("processed".equals(video.getUploadStatus())) {
        if (video.getLiveSchedule() == null) {
          if (video.getLiveStart() != null || video.getLiveEnd() != null)
            return VideoEntity.TYPE_LIVE_ARCHIVE;
          return VideoEntity.TYPE_UPLOAD;
        } else {
          if (video.getLiveSchedule().getTime() < video.getCreateDate().getTime()) {
            showDebug(video);
            if (getPremierType(video) != null) return getPremierType(video);
          }
          if (video.getLiveStart() == null && video.getLiveEnd() == null)
            return VideoEntity.TYPE_PREMIER_RESERVE;
          if (video.getLiveStart() != null && video.getLiveEnd() == null)
            return VideoEntity.TYPE_LIVE_LIVE;
          if (video.getLiveStart() != null && video.getLiveEnd() != null)
            return VideoEntity.TYPE_LIVE_ARCHIVE;
        }
      }
      if ("uploaded".equals(video.getUploadStatus()) && getLiveType(video) != null) {
        return getLiveType(video);
      }
    } else {
      // 2回目以降
      if (video.getType().startsWith("Premier") && getPremierType(video) != null) {
        return getPremierType(video);
      }
      if (video.getType().startsWith("Live") && getLiveType(video) != null) {
        return getLiveType(video);
      }

      return video.getType();
    }
    return VideoEntity.TYPE_UNKNOWN;
  }

  private void showDebug(VideoEntity video) {
    log.info("深海の雨森");
    log.info(video.toString());
    log.info("getCreateDate" + video.getCreateDate());
    log.info("getUpdateDate" + video.getUpdateDate());
    log.info("getUploadDate" + video.getUploadDate());
    log.info("getLiveSchedule" + video.getLiveSchedule());
    log.info("getLiveStart" + video.getLiveStart());
    log.info("getLiveEnd" + video.getLiveEnd());
  }

  private String getPremierType(VideoEntity video) {
    if (video.getLiveStart() == null && video.getLiveEnd() == null)
      return VideoEntity.TYPE_PREMIER_RESERVE;
    if (video.getLiveStart() != null && video.getLiveEnd() == null)
      return VideoEntity.TYPE_PREMIER_LIVE;
    if (video.getLiveStart() != null && video.getLiveEnd() != null)
      return VideoEntity.TYPE_PREMIER_UPLOAD;

    return null;
  }

  private String getLiveType(VideoEntity video) {
    if (video.getLiveStart() == null && video.getLiveEnd() == null)
      return VideoEntity.TYPE_LIVE_RESERVE;
    if (video.getLiveStart() != null && video.getLiveEnd() == null)
      return VideoEntity.TYPE_LIVE_LIVE;
    if (video.getLiveStart() != null && video.getLiveEnd() != null)
      return VideoEntity.TYPE_LIVE_ARCHIVE;

    return null;
  }
}
