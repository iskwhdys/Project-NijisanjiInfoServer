package com.iskwhdys.project.interfaces;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.iskwhdys.project.application.ChannelService;
import com.iskwhdys.project.application.SitemapService;
import com.iskwhdys.project.application.TweetService;
import com.iskwhdys.project.application.VideoService;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.infra.youtube.YoutubeApi;
import lombok.extern.slf4j.Slf4j;

@Controller
@CrossOrigin
@EnableScheduling
@Slf4j
public class ProcessController {
  @Autowired ChannelService channelService;
  @Autowired VideoService videoService;
  @Autowired TweetService tweetService;
  @Autowired SitemapService sitemapService;

  @Autowired YoutubeApi youtubeApi;

  @Autowired VideoRepository videoRepository;


  String runningProcess = "";

  @Scheduled(cron = "0 * * * * *", zone = "Asia/Tokyo")
  public void cronPerMinute() {

    if (!runningProcess.equals("")) {
      log.info("process runnning:" + runningProcess);
      return;
    }

    if (!youtubeApi.enabled()) {
      return;
    }

    runningProcess = new Date().toString();

    try {
      int min = new Date().getMinutes();
      if (min == 0) {
        log.info("cronPerMinute 60 start");
        videoService.update(60, false);
        sitemapService.update();
        log.info("cronPerMinute 60 end");
      } else if (min % 20 == 0) {
        log.info("cronPerMinute 20 start");
        videoService.update(20, false);
        log.info("cronPerMinute 20 end");
      } else if (min % 5 == 0) {
        log.info("cronPerMinute 5  start");
        videoService.update(5, false);
        log.info("cronPerMinute 5  end");
      } else {
        log.info("cronPerMinute 1  start");
        videoService.update(1, false);
        log.info("cronPerMinute 1  end");
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    try {
      tweetService.tweetReserves();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    runningProcess = "";
  }

  @Scheduled(cron = "0 45 16 * * *", zone = "Asia/Tokyo")
  public void cron1day() {

    if (youtubeApi.enabled()) {
      log.info("cron1day start");
      videoService.update(60 * 24, false);
      videoService.updateAllReserve();
      channelService.updateAll();
      log.info("cron1day end");
    } else {
      log.info("cron1day Disabled");
    }
  }

  @Value("${spring.datasource.password}")
  String password;

  @ResponseBody
  @GetMapping("/batch")
  public String batch(
      @RequestParam String name,
      @RequestParam String pass,
      @RequestParam(required = false) String value) {

    if (!runningProcess.equals("")) {
      String msg = "process runnning:" + runningProcess;
      log.info(msg);
      return msg;
    }

    log.info("process-start:" + name);

    runningProcess = "batch:name=" + name + ":pass=" + pass + "value=" + value;

    if (!password.equals(pass)) {
      runningProcess = "";
      return "Complate:" + name;
    }

    try {
      switch (name) {
        case "test":
          break;

        case "update":
          videoService.update(Integer.parseInt(value), false);
          break;

        case "update1day":
          videoService.update(60 * 24, false);
          channelService.updateAll();
          break;
        case "updateChannel":
          if (value != null && !value.equals("")) {
            channelService.createOrUpdate(value);
          } else {
            channelService.updateAll();
          }
          break;

        case "videoMaintenance":
          videoService.update(60, true);
          break;

        case "updateAllReserve":
          videoService.updateAllReserve();
          break;

        case "tweet":
          tweetService.tweet(value);
          break;

        case "tweetReserve":
          if (value != null && !value.equals("")) {
            videoRepository.findById(value).ifPresent(video -> tweetService.tweetReserve(video));
          } else {
            tweetService.tweetReserves();
          }
          break;

        case "xmlUpdate":
          channelService.xmlUpdate();
          videoService.xmlUpdate();
          break;

        default:
          runningProcess = "";
          return name;
      }
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }

    log.info("process-end:" + name);

    runningProcess = "";

    return "Complate:" + name;
  }
}
