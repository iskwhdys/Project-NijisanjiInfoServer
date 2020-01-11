package com.iskwhdys.project.interfaces;

import org.springframework.beans.factory.annotation.Autowired;
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



		// メンテナンス用：動画タイプの更新
		case "updateVideoType": {
			var videos = videoRepository.findAll();
			for (var video : videos) {
				video.setType(VideoSpecification.getType(video));
			}
			videoRepository.saveAll(videos);
			break;

		}
		}

		System.out.println("process-end:" + name);
		return "Complate:" + name;

	}

}
