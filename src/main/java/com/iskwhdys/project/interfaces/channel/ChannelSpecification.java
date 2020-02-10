package com.iskwhdys.project.interfaces.channel;

import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.channel.ChannelEntity;

public class ChannelSpecification {

	private ChannelSpecification() {}

	public static ChannelEntity update(ChannelEntity channel, RestTemplate restTemplate) {
		return update(channel, restTemplate, "snippet", "statistics");
	}

	@SuppressWarnings("unchecked")
	private static ChannelEntity update(ChannelEntity channel, RestTemplate restTemplate, String... parts) {
		if(Constans.YOUTUBE_API_KEY == null || "".equals(Constans.YOUTUBE_API_KEY))	return channel;

		String url = Constans.YOUTUBE_API_URL + "/channels?" + "id=" + channel.getId() + "&key="
				+ Constans.YOUTUBE_API_KEY
				+ "&part=" + String.join(",", parts);

		var items = (List<Map<String, ?>>) restTemplate.getForObject(url, Map.class).get("items");
		if (items.isEmpty()) {
			return channel;
		}
		Map<String, ?> item = items.get(0);
		for (String part : parts) {
			var map = (Map<String, ?>) item.get(part);
			if (part.equals("snippet")) setSnippet(channel, map);
			else if (part.equals("statistics")) setStatistics(channel, map);
		}

		return channel;
	}

	@SuppressWarnings("unchecked")
	private static ChannelEntity setSnippet(ChannelEntity channel, Map<String, ?> map) {
		if (map == null) return channel;
		if (map.containsKey("title")) channel.setTitle(map.get("title").toString());
		if (map.containsKey("description")) channel.setDescription(map.get("description").toString());
		if (map.containsKey("thumbnails")) {
			var thumbnails = (Map<String, ?>) map.get("thumbnails");
			for (var key : new String[] { "default", "medium", "high" }) {
				if (thumbnails.containsKey(key)) {
					var th = (Map<String, ?>) thumbnails.get(key);
					channel.setThumbnailUrl(th.get("url").toString());
					break;
				}
			}
		}
		return channel;
	}

	private static ChannelEntity setStatistics(ChannelEntity channel, Map<String, ?> map) {
		if (map == null) return channel;
		if (map.containsKey("subscriberCount")) channel.setSubscriberCount(toInteger(map, "subscriberCount"));
		return channel;
	}

	private static Integer toInteger(Map<String, ?> map, String key) {
		return Integer.parseInt(map.get(key).toString());
	}

}
