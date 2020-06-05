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
import com.iskwhdys.project.application.VideoDeliveryService;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;

@CrossOrigin
@RestController
@RequestMapping("/api/video")
public class VideoController {

  private @Autowired VideoDeliveryService vds;
  private @Autowired VideoRepository vr;

  @GetMapping("/live")
  public List<VideoEntity> getsLive(@RequestParam String mode) {

    if ("new".equals(mode)) return vds.live();
    return new ArrayList<>();
  }

  @GetMapping("/upload")
  public List<VideoEntity> getUpload(
      @RequestParam String mode, @RequestParam(required = false) String from) {

    if ("new".equals(mode)) return vds.upload();
    if ("get".equals(mode)) return vds.upload(from);
    return new ArrayList<>();
  }

  @GetMapping("/archive")
  public List<VideoEntity> getArchive(
      @RequestParam String mode, @RequestParam(required = false) String from) {

    if ("new".equals(mode)) return vds.archive();
    if ("get".equals(mode)) return vds.archive(from);
    return new ArrayList<>();
  }

  @GetMapping("/premier")
  public List<VideoEntity> getPremier(
      @RequestParam String mode, @RequestParam(required = false) String from) {

    if ("new".equals(mode)) return vds.premier();
    if ("get".equals(mode)) return vds.premier(from);
    return new ArrayList<>();
  }

  @GetMapping("/schedule")
  public List<VideoEntity> getSchedule(
      @RequestParam String mode, @RequestParam(required = false) String from) {

    if ("new".equals(mode)) return vds.schedule();
    if ("get".equals(mode)) return vds.schedule(from);
    return new ArrayList<>();
  }

  @GetMapping("/channel/{channelId}")
  public List<VideoEntity> getChannel(
      @PathVariable String channelId,
      @RequestParam String mode,
      @RequestParam(required = false) String from) {

    if ("new".equals(mode)) {
      return vr.findTop10ByEnabledTrueAndChannelIdEqualsAndUploadDateBeforeOrderByUploadDateDescIdAsc(
          channelId, new Date());
    } else if ("get".equals(mode)) {
      return vr.findTop10ByEnabledTrueAndChannelIdEqualsAndUploadDateBeforeOrderByUploadDateDescIdAsc(
          channelId, Common.toDate(from));
    }

    return new ArrayList<>();
  }
}
