package com.iskwhdys.project.interfaces;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.iskwhdys.project.Common;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/video")
public class VideoController {

  @Autowired
  VideoRepository vr;

  @GetMapping(value = "/live")
  public List<VideoEntity> getsLive(@RequestParam(name = "mode") String mode) {
    if ("new".equals(mode)) {
      return vr.findByEnabledTrueAndTypeInOrderByLiveStartDesc(VideoEntity.TYPE_LIVES);
    }
    return new ArrayList<>();
  }

  @GetMapping(value = "/upload")
  public List<VideoEntity> getUpload(@RequestParam(name = "mode") String mode,
      @RequestParam(name = "from", required = false) String from) {

    if ("new".equals(mode)) {
      return vr.findByEnabledTrueAndTypeInAndUploadDateBetweenOrderByUploadDateDesc(
          VideoEntity.TYPE_UPLOADS, new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 2)),
          new Date());
    } else if ("get".equals(mode)) {
      return vr.findTop10ByEnabledTrueAndTypeInAndUploadDateBeforeOrderByUploadDateDesc(
          VideoEntity.TYPE_UPLOADS, Common.toDate(from));
    }
    return new ArrayList<>();
  }

  @GetMapping(value = "/archive")
  public List<VideoEntity> getArchive(@RequestParam(name = "mode") String mode,
      @RequestParam(name = "from", required = false) String from) {

    if ("new".equals(mode)) {
      return vr.findByEnabledTrueAndTypeEqualsAndLiveStartBetweenOrderByLiveStartDesc(
          VideoEntity.TYPE_LIVE_ARCHIVE, new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)),
          new Date());
    } else if ("get".equals(mode)) {
      return vr.findTop30ByEnabledTrueAndTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(
          VideoEntity.TYPE_LIVE_ARCHIVE, Common.toDate(from));
    }
    return new ArrayList<>();
  }

  @GetMapping(value = "/premier")
  public List<VideoEntity> getPremier(@RequestParam(name = "mode") String mode,
      @RequestParam(name = "from", required = false) String from) {

    if ("new".equals(mode)) {
      return vr.findByEnabledTrueAndTypeEqualsAndLiveScheduleBetweenOrderByLiveSchedule(
          VideoEntity.TYPE_PREMIER_RESERVE,
          new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)),
          new Date(new Date().getTime() + (1000 * 60 * 60 * 24 * 2)));
    } else if ("get".equals(mode)) {
      return vr.findTop10ByEnabledTrueAndTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(
          VideoEntity.TYPE_PREMIER_RESERVE, Common.toDate(from));
    }
    return new ArrayList<>();
  }

  @GetMapping(value = "/schedule")
  public List<VideoEntity> getSchedule(@RequestParam(name = "mode") String mode,
      @RequestParam(name = "from", required = false) String from) {

    if ("new".equals(mode)) {
      return vr.findByEnabledTrueAndTypeEqualsAndLiveScheduleBetweenOrderByLiveSchedule(
          VideoEntity.TYPE_LIVE_RESERVE, new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)),
          new Date(new Date().getTime() + (1000 * 60 * 60 * 24 * 2)));
    } else if ("get".equals(mode)) {
      return vr.findTop30ByEnabledTrueAndTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(
          VideoEntity.TYPE_LIVE_RESERVE, Common.toDate(from));
    }
    return new ArrayList<>();
  }

  @GetMapping(value = "/channel/{channelId}")
  public List<VideoEntity> getChannel(@PathVariable("channelId") String channelId) {
    return vr.findTop10ByEnabledTrueAndChannelIdEqualsAndUploadDateBeforeOrderByUploadDateDesc(
        channelId, new Date());
  }

}
