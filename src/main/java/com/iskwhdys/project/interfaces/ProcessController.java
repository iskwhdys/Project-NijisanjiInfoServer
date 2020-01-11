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
import com.iskwhdys.project.domain.channel.ChannelEntity;
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

	@ResponseBody
	@RequestMapping(value = "/batch", method = RequestMethod.GET)
	public String batch(@RequestParam("name") String name) {
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

		case "updateNullEtagDataViaApi": {
			var videos = videoRepository.findNewData();
			for (var video : videos) {
				VideoSpecification.updateViaApi(video, restTemplate);
				System.out.println("Channel:[" + video.getChannelId() + "] Video:[" + video.getId() + "] ["
						+ video.getTitle() + "]");
				videoRepository.save(video);
			}
			break;
		}

		// 定期ジョブ
		case "update10min": {
			channelService.update10min();
			break;
		}

		// 定期ジョブ
		case "update1day": {
			channelService.update1day();
			break;
		}

		// 全てのXMLの取得更新 + 新規動画のみAPIでデータ取得
		case "updateRealTime": {
			for (ChannelEntity channel : channelRepository.findAll()) {
				channelService.updateRealTime(channel);
			}
			break;
		}

		// ライブ状態の更新
		case "updateLiveVideo": {
			var videos = videoRepository.findLive();
			for (var video : videos) {
				VideoSpecification.updateViaApi(video, restTemplate);
				System.out.println("Live - Channel:[" + video.getChannelId() + "] Video:[" + video.getId() + "] ["
						+ video.getTitle() + "]");
			}
			videoRepository.saveAll(videos);
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
