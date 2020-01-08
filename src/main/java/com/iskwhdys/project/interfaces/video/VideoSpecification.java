package com.iskwhdys.project.interfaces.video;

import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.video.VideoEntity;

public class VideoSpecification {

	public static void setThumbnail(VideoEntity entity, RestTemplate restTemplate) {

		try {
			byte[] buf = restTemplate.getForObject(entity.getThumbnail(), byte[].class);
			String base64 = Base64.getEncoder().encodeToString(buf);
			entity.setThumbnail("data:image/jpeg;base64," + base64);
		} catch (Exception e) {
			System.out.println(e);
			System.out.println(entity.getThumbnail());
		}
	}


	public static VideoEntity updateViaApi(VideoEntity entity, RestTemplate restTemplate) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("part", "snippet,statistics,contentDetails,liveStreamingDetails");
		params.put("id", entity.getId());
		params.put("key", Constans.YOUTUBE_API_KEY);

		String url = Constans.YOUTUBE_API_URL + "/videos" +
				"?part=" + params.get("part") +
				"&id=" + params.get("id") +
				"&key=" + params.get("key");

		Map<String, ?> json = restTemplate.getForObject(url, Map.class);

		if (((List) json.get("items")).size() == 0) {
			entity.setEnabled(false);
			return entity;

		}

		var items = (Map<String, ?>) ((List) json.get("items")).get(0);
		entity.setEtag(items.get("etag").toString());

		var snippet = (Map<String, ?>) items.get("snippet");
		if (snippet.containsKey("title"))
			entity.setTitle(snippet.get("title").toString());
		if (snippet.containsKey("description"))
			entity.setDescription(snippet.get("description").toString());

		var statistics = (Map<String, ?>) items.get("statistics");

		if (statistics.containsKey("viewCount"))
			entity.setViews(Integer.parseInt(statistics.get("viewCount").toString()));
		if (statistics.containsKey("likeCount"))
			entity.setLikes(Integer.parseInt(statistics.get("likeCount").toString()));
		if (statistics.containsKey("dislikeCount"))
			entity.setDislikes(Integer.parseInt(statistics.get("dislikeCount").toString()));
		if (statistics.containsKey("favoriteCount"))
			entity.setFavorites(Integer.parseInt(statistics.get("favoriteCount").toString()));
		if (statistics.containsKey("commentCount"))
			entity.setComments(Integer.parseInt(statistics.get("commentCount").toString()));

		var contentDetails = (Map<String, ?>) items.get("contentDetails");
		String duration = contentDetails.get("duration").toString();
		entity.setDuration((int) Duration.parse(duration).toSeconds());

		var liveStreamingDetails = (Map<String, ?>) items.get("liveStreamingDetails");
		if (liveStreamingDetails != null) {
			if (liveStreamingDetails.containsKey("actualStartTime"))
				entity.setLiveStart(Common.youtubeTimeToDate(liveStreamingDetails.get("actualStartTime").toString()));
			if (liveStreamingDetails.containsKey("actualEndTime"))
				entity.setLiveEnd(Common.youtubeTimeToDate(liveStreamingDetails.get("actualEndTime").toString()));
			if (liveStreamingDetails.containsKey("scheduledStartTime"))
				entity.setLiveSchedule(Common.youtubeTimeToDate(liveStreamingDetails.get("scheduledStartTime").toString()));
			if (liveStreamingDetails.containsKey("concurrentViewers"))
				entity.setLiveViews(Integer.parseInt(liveStreamingDetails.get("concurrentViewers").toString()));
		}

		entity.setEnabled(true);
		return entity;

	}

	public static int getLikeCount(int count, String strStarAve) {
		int starAve = Integer.parseInt( strStarAve.replace(".", ""));

		for (int i = count; i > 0; i--) {
			double like = Constans.YOUTUBE_LIKE_VALUE * i;
			double dislike = Constans.YOUTUBE_DISLIKE_VALUE * (count - i);
			double ave = (like + dislike)  / (double)count;
			int num = (int)(ave * 100);

			if (num <= starAve) {
				return i;
			}
		}

		return 0;
	}



}
