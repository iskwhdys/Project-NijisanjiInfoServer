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
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.application.ChannelService;
import com.iskwhdys.project.application.VideoService;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.infra.youtube.YoutubeApi;

import lombok.extern.slf4j.Slf4j;

@Controller
@CrossOrigin
@EnableScheduling
@Slf4j
public class ProcessController {

  RestTemplate restTemplate = new RestTemplate();

  @Autowired ChannelRepository channelRepository;
  @Autowired VideoRepository videoRepository;

  @Autowired ChannelService channelService;
  @Autowired VideoService videoService;

  @Autowired YoutubeApi youtubeApi;

  @Scheduled(cron = "0 1,2,3,4,5,6,7,8,9,10,15,25,30,35,45,50,55 * * * *", zone = "Asia/Tokyo")
  public void cron5min() {
    if (youtubeApi.enabled()) {
      log.info("cron5min start");
      videoService.update5min();
      log.info("cron5min end");
    } else {
      log.info("cron5min Disabled");
    }
  }

  @Scheduled(cron = "0 0,20,40 * * * *", zone = "Asia/Tokyo")
  public void cron20min() {
    if (youtubeApi.enabled()) {
      log.info("cron20min start");
      videoService.update20min();
      log.info("cron20min end");
    } else {
      log.info("cron20min Disabled");
    }
  }

  @Scheduled(cron = "0 45 16 * * *", zone = "Asia/Tokyo")
  public void cron1day() {
    if (youtubeApi.enabled()) {
      log.info("cron1day " + new Date());
      channelService.updateAll();
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
    log.info("process-start:" + name);

    if (!password.equals(pass)) {
      return "error:" + name;
    }

    switch (name) {
      case "test":
        break;

      case "update5min":
        videoService.update5min();
        break;

      case "update20min":
        videoService.update20min();
        break;

      case "update1day":
        videoService.update20min();
        channelService.updateAll();
        break;

      case "tweet":
        videoService.tweet(value);
        break;

      default:
        return name;
    }

    log.info("process-end:" + name);
    return "Complate:" + name;
  }
}
