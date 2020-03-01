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
import com.iskwhdys.project.application.TweetService;
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
  @Autowired TweetService tweetService;

  @Autowired YoutubeApi youtubeApi;

  @Scheduled(cron = "0 * * * * *", zone = "Asia/Tokyo")
  public void cronPerMinute() {
    if (!youtubeApi.enabled()) {
      return;
    }

    int min = new Date().getMinutes();
    if (min == 0) {
      log.info("cronPerMinute 60 start");
      videoService.update(60, false);
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
  }

  @Scheduled(cron = "0 45 16 * * *", zone = "Asia/Tokyo")
  public void cron1day() {
    if (youtubeApi.enabled()) {
      log.info("cron1day start");
      videoService.update(60 * 24, false);
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
    log.info("process-start:" + name);

    if (!password.equals(pass)) {
      return "Complate:" + name;
    }

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

      case "videoMaintenance":
        videoService.update(60, true);
        break;

      case "tweet":
        tweetService.tweet(value);
        break;

      default:
        return name;
    }

    log.info("process-end:" + name);
    return "Complate:" + name;
  }
}
