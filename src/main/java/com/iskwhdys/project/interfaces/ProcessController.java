package com.iskwhdys.project.interfaces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.application.ChannelService;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoRepository;

@Controller
public class ProcessController {

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	VideoRepository videoRepository;

	@Autowired
	ChannelService channelService;

	@ResponseBody
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	public String process(@RequestParam("name") String name) {
		System.out.println("process-start:" + name);

		switch (name) {

		case "updateAllVideosViaXml":
			for (ChannelEntity channel : channelRepository.findAll()) {
				channelService.updateAllVideosViaXml(channel);
			}
			break;

		case "updateTodayUploadVideos":
			for (ChannelEntity channel : channelRepository.findAll()) {
				channelService.updateTodayUploadVideos(channel);
			}
			break;
		case "updateRealTime":
			for (ChannelEntity channel : channelRepository.findAll()) {
				channelService.updateRealTime(channel);
			}
			break;
		case "updateLiveVideo":
			for (ChannelEntity channel : channelRepository.findAll()) {
				channelService.updateLiveVideo(channel);
			}
			break;
		}

		System.out.println("process-end:" + name);
		return "Complate:" + name;

	}

}
