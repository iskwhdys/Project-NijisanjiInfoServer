package com.iskwhdys.project.interfaces;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.interfaces.video.VideoFactory;

@Controller
public class ProcessController {

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	VideoRepository videoRepository;

	@ResponseBody
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	public String process(@RequestParam("name") String name) {
		System.out.println("process-start:" + name);
		switch (name) {
		case "transportChannelJsonServerToPostgreSql":
			transportChannelJsonServerToPostgreSql();
			break;

		case "updateChannelVideos":
			updateChannelVideos();
			break;

		case "updateChannelVideoFromRssFeedXml":
			updateChannelVideoFromRssFeedXml();
			break;
		}

		System.out.println("process-end:" + name);
		return "Complate:" + name;
	}

	/**
	 * JSON Server上のチャンネルデータをRDBに転送する
	 */
	private void transportChannelJsonServerToPostgreSql() {
		Map<String, ?>[] array = restTemplate.getForObject(Constans.JSON_DB_URL, Map[].class);
		for (Map<String, ?> root : array) {

			var id = root.get("id").toString();
			var info = (Map<String, ?>) root.get("info");
			var snippet = (Map<String, ?>) info.get("snippet");
			var title = snippet.get("title").toString();
			var description = snippet.get("description").toString();
			var statistics = (Map<String, ?>) info.get("statistics");
			var subscriberCount = Integer.parseInt(statistics.get("subscriberCount").toString());
			var thumbnail = root.get("channelIcon").toString();

			var entity = new ChannelEntity();
			entity.setId(id);
			entity.setTitle(title);
			entity.setDescription(description);
			entity.setSubscriberCount(subscriberCount);
			entity.setThumbnail(thumbnail);

			channelRepository.save(entity);
		}
	}

	/**
	 *
	 */
	private void updateChannelVideoFromRssFeedXml() {
		for (ChannelEntity entity : channelRepository.findAll()) {
			if (entity.getId().contains("UCwokZsOK_uEre70XayaFnzA") == false)
				continue;

			System.out.println("Channel:[" + entity.getId() + "] [" + entity.getTitle() + "]");
			var entities = VideoFactory.getChannelVideosFromRssFeedXml(entity.getId());
			videoRepository.saveAll(entities);
		}

		for (ChannelEntity entity : channelRepository.findAll()) {
			if (entity.getId().contains("UCwokZsOK_uEre70XayaFnzA") == false)
				continue;
			System.out.println("Channel:[" + entity.getId() + "] [" + entity.getTitle() + "]");

			var entities = videoRepository.findChannelId(entity.getId());
			for (VideoEntity videoEntity : entities) {
				VideoFactory.updateVideoEntity(videoEntity);
			}

			videoRepository.saveAll(entities);
		}
	}

	private void updateChannelVideos() {
		for (ChannelEntity entity : channelRepository.findAll()) {
			var entities = VideoFactory.getChannelVideosFromRssFeedXml(entity.getId());
			videoRepository.saveAll(entities);
		}

		for (ChannelEntity entity : channelRepository.findAll()) {
			var entities = videoRepository.findNewData(entity.getId());

			for (VideoEntity videoEntity : entities) {
				if (videoEntity.getEtag() == null) {
					VideoFactory.updateVideoEntity(videoEntity);
				}
			}

			videoRepository.saveAll(entities);
		}
	}

}
