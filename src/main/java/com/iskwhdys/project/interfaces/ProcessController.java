package com.iskwhdys.project.interfaces;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
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
import lombok.extern.slf4j.Slf4j;

@Controller
@CrossOrigin
@EnableScheduling
@Slf4j
public class ProcessController {

  RestTemplate restTemplate = new RestTemplate();

  @Autowired
  ChannelRepository channelRepository;
  @Autowired
  VideoRepository videoRepository;

  @Autowired
  ChannelService channelService;
  @Autowired
  VideoService videoService;

  @Scheduled(cron = "0 3,13,23,33,43,53 * * * *", zone = "Asia/Tokyo")
  public void cron10min() {
    log.info("cron10min " + new Date());
    videoService.update10min();
  }

  @Scheduled(cron = "0 45 16 * * *", zone = "Asia/Tokyo")
  public void cron1day() {
    log.info("cron1day " + new Date());
    channelService.updateAll();
  }

  @ResponseBody
  @GetMapping(value = "/batch")
  public String batch(@RequestParam("name") String name) {
    log.info("process-start:" + name);

    switch (name) {

      // 定期ジョブ
      case "update10min":
        videoService.update10min();
        break;

      case "update1day":
        videoService.update10min();
        channelService.updateAll();
        break;

      default:
        return name;
    }

    log.info("process-end:" + name);
    return "Complate:" + name;
  }

}
