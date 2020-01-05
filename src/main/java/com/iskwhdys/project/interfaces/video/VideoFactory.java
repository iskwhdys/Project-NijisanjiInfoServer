package com.iskwhdys.project.interfaces.video;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.video.VideoEntity;

public class VideoFactory {

	private static RestTemplate restTemplate = new RestTemplate();

	public static List<VideoEntity> getChannelVideosFromRssFeedXml(String channelId) {

		byte[] bytes = restTemplate.getForObject(Constans.FEEDS_URL + "?channel_id=" + channelId, byte[].class);
		var elements = bytesToElementList(bytes, "entry");
		var videos = new ArrayList<VideoEntity>();
		for (Element entry : elements) {
			var entity = createVideoEntity(entry);
			videos.add(entity);
		}

		return videos;
	}



	public static List<VideoEntity> getChannelVideosFromApi(String channelId) {

		byte[] bytes = restTemplate.getForObject(Constans.FEEDS_URL + "?channel_id=" + channelId, byte[].class);
		var elements = bytesToElementList(bytes, "entry");
		var videos = new ArrayList<VideoEntity>();
		for (Element entry : elements) {
			var entity = createVideoEntity(entry);

			if(entity.getEtag() == null) {
				System.out.println(entity.getTitle());
				updateVideoEntity(entity);
			}

			videos.add(entity);
		}

		return videos;
	}







	private static List<Element> bytesToElementList(byte[] xmlBytes, String name) {

		var is = new ByteArrayInputStream(xmlBytes);
		Element root = null;
		try {
			root = new SAXBuilder().build(is).getRootElement();
		} catch (Exception e) {
			System.out.println(e);
		}
		var list = root.getChildren().stream().filter(p -> p.getName().contains(name)).collect(Collectors.toList());

		return list;
	}

	private static VideoEntity createVideoEntity(Element entry) {
		var entity = new VideoEntity();
		Element group = null;

		for (Element element : entry.getChildren()) {
			switch (element.getName()) {
			case "videoId":
				entity.setId(element.getValue());
				break;
			case "channelId":
				entity.setChannelId(element.getValue());
				break;
			case "title":
				entity.setTitle(element.getValue());
				break;
			case "published":
				entity.setUploadDate(youtubeTimeToDate(element.getValue().toString()));
				break;
			case "group":
				group = element;
				break;
			}
		}

		for (Element element : group.getChildren()) {
			switch (element.getName()) {
			case "description":
				entity.setDescription(element.getValue());
				break;
			case "thumbnail":
				try {
					byte[] buf = restTemplate.getForObject(element.getAttributeValue("url"), byte[].class);
					String base64 = Base64.getEncoder().encodeToString(buf);
					entity.setThumbnail("data:image/jpeg;base64," + base64);
				} catch (Exception e) {
					System.out.println(element.getAttributeValue("url"));
				}
				break;
			}
		}

		return entity;
	}

	public static VideoEntity updateVideoEntity(VideoEntity entity) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("part", "statistics,contentDetails,liveStreamingDetails");
		params.put("id", entity.getId());
		params.put("key", Constans.YOUTUBE_API_KEY);

		String url = Constans.YOUTUBE_API_URL + "/videos" +
				"?part=" + params.get("part") +
				"&id=" + params.get("id") +
				"&key=" + params.get("key");

		Map<String, ?> json = restTemplate.getForObject(url, Map.class);

		if( ((List) json.get("items")).size() == 0) {
			entity.setEnabled(false);
			return entity;

		}

		var items = (Map<String, ?>) ((List) json.get("items")).get(0);
		entity.setEtag(items.get("etag").toString());

		var statistics = (Map<String, ?>) items.get("statistics");
		entity.setViews(Integer.parseInt(statistics.get("viewCount").toString()));
		entity.setLikes(Integer.parseInt(statistics.get("likeCount").toString()));
		entity.setDislikes(Integer.parseInt(statistics.get("dislikeCount").toString()));
		entity.setFavorites(Integer.parseInt(statistics.get("favoriteCount").toString()));
		entity.setComments(Integer.parseInt(statistics.get("commentCount").toString()));

		var contentDetails = (Map<String, ?>) items.get("contentDetails");
		String duration = contentDetails.get("duration").toString();
		entity.setDuration((int) Duration.parse(duration).toSeconds());

		var liveStreamingDetails = (Map<String, ?>) items.get("liveStreamingDetails");
		if (liveStreamingDetails != null) {
			if (liveStreamingDetails.containsKey("actualStartTime"))
				entity.setLiveStart(youtubeTimeToDate(liveStreamingDetails.get("actualStartTime").toString()));
			if (liveStreamingDetails.containsKey("actualEndTime"))
				entity.setLiveEnd(youtubeTimeToDate(liveStreamingDetails.get("actualEndTime").toString()));
			if (liveStreamingDetails.containsKey("scheduledStartTime"))
				entity.setLiveSchedule(youtubeTimeToDate(liveStreamingDetails.get("scheduledStartTime").toString()));
			if (liveStreamingDetails.containsKey("concurrentViewers"))
				entity.setLiveViews(Integer.parseInt(liveStreamingDetails.get("concurrentViewers").toString()));
		}

		entity.setEnabled(true);
		return entity;

	}

	private static Date youtubeTimeToDate(String text) {
		try {
			String datetime = text.substring(0, 10) + " " + text.substring(11, 19);
			var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			return sdf.parse(datetime);
		} catch (ParseException e) {
		}
		return null;
	}

}
