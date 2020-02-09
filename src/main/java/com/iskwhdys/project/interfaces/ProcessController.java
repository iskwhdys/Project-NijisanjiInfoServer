package com.iskwhdys.project.interfaces;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.application.ChannelService;
import com.iskwhdys.project.application.VideoService;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.interfaces.video.VideoSpecification;

@Controller
@CrossOrigin
@EnableScheduling
public class ProcessController {

	Logger logger = LogManager.getLogger(ProcessController.class);

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	VideoRepository videoRepository;

	@Autowired
	ChannelService channelService;
	@Autowired
	VideoService videoService;

	 //
	@Scheduled(cron = "0 */10 * * * *", zone = "Asia/Tokyo")
	public void cron10min() {
		logger.info("cron10min " + new Date());
		videoService.update10min();
	}

	//
	@Scheduled(cron = "0 45 16 * * *", zone = "Asia/Tokyo")
	public void cron1day() {
		logger.info("cron1day " + new Date());
		channelService.updateAll();
	}

	@ResponseBody
	@RequestMapping(value = "/batch", method = RequestMethod.GET)
	public String batch(@RequestParam("name") String name) {
		logger.info("process-start:" + name);

		switch (name) {

		// 定期ジョブ
		case "update10min": {
			videoService.update10min();
			break;
		}

		case "update1day": {
			videoService.update10min();
			channelService.updateAll();
			break;
		}

		case "updateVideoType": {
			var videos = videoRepository.findAll();
			for (var video : videos) {
				video.setType(VideoSpecification.getType(video));
			}
			videoRepository.saveAll(videos);
			break;
		}

//		case "updateThumbnail": {
//			var videos = videoRepository.findAll();
//			for (var video : videos) {
//				video.setThumbnailUrl("https://i4.ytimg.com/vi/" +  video.getId() +  "/hqdefault.jpg");
//				boolean success = VideoSpecification.setThumbnail(video, restTemplate);
//				video.setEnabled(success);
//			}
//			videoRepository.saveAll(videos);
//			break;
//		}


		}

		logger.info("process-end:" + name);
		return "Complate:" + name;

	}

}
