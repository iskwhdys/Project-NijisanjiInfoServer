package com.iskwhdys.project.interfaces;

import java.util.Date;

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
		System.out.println("cron10min " + new Date());
		videoService.update10min();
	}

	//
	@Scheduled(cron = "0 45 16 * * *", zone = "Asia/Tokyo")
	public void cron1day() {
		System.out.println("cron1day " + new Date());

	}
	
	@ResponseBody
	@RequestMapping(value = "/batch", method = RequestMethod.GET)
	public String batch(@RequestParam("name") String name) {
		System.out.println("process-start:" + name);

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

		case "resizeVideoThumbnail": {
//
//			var videos = videoRepository.findAll();
//			for (var video : videos) {
//
//				if(video.getThumbnail().startsWith(Constans.BESE64_IMAGE) == false) continue;
//				String base64 = video.getThumbnail().substring(Constans.BESE64_IMAGE.length());
//				byte[] bytes = Base64.getDecoder().decode(base64);
//				try {
//					bytes = Common.scaleImage(bytes, 176, 132, 0.9f);
//				} catch (IOException e) {
//					// TODO 自動生成された catch ブロック
//					e.printStackTrace();
//				}
//				base64 = Base64.getEncoder().encodeToString(bytes);
//				video.setThumbnail(Constans.BESE64_IMAGE + base64);
//			}
//			videoRepository.saveAll(videos);
			break;
		}


		}

		System.out.println("process-end:" + name);
		return "Complate:" + name;

	}

}
