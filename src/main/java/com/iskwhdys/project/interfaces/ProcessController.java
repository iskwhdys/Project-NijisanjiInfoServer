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
import com.iskwhdys.project.application.ChannelService;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;

@Controller
public class ProcessController {

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	ChannelService channelService;

	@ResponseBody
	@RequestMapping(value = "/process", method = RequestMethod.GET)
	public String process(@RequestParam("name") String name) {
		System.out.println("process-start:" + name);

		String response = "";

		switch (name) {
		case "transportChannelJsonServerToPostgreSql":
			transportChannelJsonServerToPostgreSql();
			break;

		case "updateVideos":
			response = updateVideos();
			break;

		}

		System.out.println(response);
		System.out.println("process-end:" + name);
		return "Complate:" + name + "<br>" + response;
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

	private String updateVideos() {
		var res = new StringBuilder();

		for (ChannelEntity channel : channelRepository.findAll()) {
			var newVideos = channelService.createNewVideos(channel);
			newVideos.forEach(v -> res.append(v.getTitle()).append("<br>"));
		}

		for (ChannelEntity channel : channelRepository.findAll()) {
			var newVideos = channelService.updateTodayUploadVideo(channel);
			newVideos.forEach(v -> res.append(v.getTitle()).append("<br>"));

			// IskDebug
			break;
		}

		return res.toString();
	}

}
