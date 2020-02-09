package com.iskwhdys.project.interfaces.video;

import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.video.VideoEntity;

public class VideoSpecification {

	private static Logger logger = LogManager.getLogger(VideoSpecification.class);

	public static VideoEntity updateViaApi(VideoEntity entity, RestTemplate restTemplate) {
		return updateBaseFunction(entity, restTemplate,
				"snippet", "statistics", "contentDetails", "liveStreamingDetails", "status");
	}

	public static VideoEntity updateLiveInfoViaApi(VideoEntity entity, RestTemplate restTemplate) {
		return updateBaseFunction(entity, restTemplate, "liveStreamingDetails");
	}

	public static VideoEntity updateLiveToArchiveInfoViaApi(VideoEntity entity, RestTemplate restTemplate) {
		return updateBaseFunction(entity, restTemplate, "contentDetails");
	}

	public static VideoEntity updateReserveInfoViaApi(VideoEntity entity, RestTemplate restTemplate) {
		return updateBaseFunction(entity, restTemplate, "liveStreamingDetails");
	}

	@SuppressWarnings("unchecked")
	private static VideoEntity updateBaseFunction(VideoEntity entity, RestTemplate restTemplate, String... parts) {
		if(Constans.YOUTUBE_API_KEY == null || "".equals(Constans.YOUTUBE_API_KEY))	return entity;

		String url = Constans.YOUTUBE_API_URL + "/videos?" + "id=" + entity.getId() + "&key=" + Constans.YOUTUBE_API_KEY
				+ "&part=" + String.join(",", parts);

		var items = (List<Map<String, ?>>) restTemplate.getForObject(url, Map.class).get("items");
		if (items.size() == 0) {
			entity.setEnabled(false);
			return entity;
		}
		var item = (Map<String, ?>) items.get(0);

		entity.setEtag(item.get("etag").toString());
		for (String part : parts) {
			var map = (Map<String, ?>) item.get(part);
			if (part.equals("snippet")) setSnippet(entity, map);
			else if (part.equals("statistics")) setStatistics(entity, map);
			else if (part.equals("contentDetails")) setContentDetails(entity, map);
			else if (part.equals("liveStreamingDetails")) setLiveStreamingDetails(entity, map);
			else if (part.equals("status")) setStatus(entity, map);
		}
		entity.setType(getType(entity));
		entity.setEnabled(true);

		return entity;
	}

	private static VideoEntity setSnippet(VideoEntity video, Map<String, ?> map) {
		if (map == null) return video;
		if (map.containsKey("title")) video.setTitle(map.get("title").toString());
		if (map.containsKey("description")) video.setDescription(map.get("description").toString());
		return video;
	}

	private static VideoEntity setStatistics(VideoEntity video, Map<String, ?> map) {
		if (map == null) return video;
		if (map.containsKey("viewCount")) video.setViews(toInteger(map, "viewCount"));
		if (map.containsKey("likeCount")) video.setLikes(toInteger(map, "likeCount"));
		if (map.containsKey("dislikeCount")) video.setDislikes(toInteger(map, "dislikeCount"));
		if (map.containsKey("favoriteCount")) video.setFavorites(toInteger(map, "favoriteCount"));
		if (map.containsKey("commentCount")) video.setComments(toInteger(map, "commentCount"));
		return video;
	}

	private static VideoEntity setContentDetails(VideoEntity video, Map<String, ?> map) {
		if (map == null) return video;
		String duration = map.get("duration").toString();
		video.setDuration((int) Duration.parse(duration).toSeconds());
		return video;
	}

	private static VideoEntity setLiveStreamingDetails(VideoEntity video, Map<String, ?> map) {
		if (map == null) return video;
		if (map.containsKey("actualStartTime")) video.setLiveStart(toDate(map, "actualStartTime"));
		if (map.containsKey("actualEndTime")) video.setLiveEnd(toDate(map, "actualEndTime"));
		if (map.containsKey("scheduledStartTime")) video.setLiveSchedule(toDate(map, "scheduledStartTime"));
		if (map.containsKey("concurrentViewers")) video.setLiveViews(toInteger(map, "concurrentViewers"));
		return video;
	}

	private static VideoEntity setStatus(VideoEntity video, Map<String, ?> map) {
		if (map == null) return video;
		if (map.containsKey("uploadStatus")) video.setUploadStatus(map.get("uploadStatus").toString());
		return video;
	}

	private static Date toDate(Map<String, ?> map, String key) {
		return Common.youtubeTimeToDate(map.get(key).toString());
	}

	private static Integer toInteger(Map<String, ?> map, String key) {
		return Integer.parseInt(map.get(key).toString());
	}

	public static int getLikeCount(int count, String strStarAve) {
		int starAve = Integer.parseInt(strStarAve.replace(".", ""));

		for (int i = count; i > 0; i--) {
			double like = Constans.YOUTUBE_LIKE_VALUE * i;
			double dislike = Constans.YOUTUBE_DISLIKE_VALUE * (count - i);
			double ave = (like + dislike) / (double) count;
			int num = (int) (ave * 100);

			if (num <= starAve) {
				return i;
			}
		}

		return 0;
	}

	public static String getType(VideoEntity video) {
		if (video.getType() == null || video.isUnknown()) {
			// 初回
			if ("processed".equals(video.getUploadStatus())) {
				if (video.getLiveSchedule() == null) {
					return VideoEntity.TYPE_UPLOAD;
				} else {
					if (video.getLiveStart() == null && video.getLiveEnd() == null) return VideoEntity.TYPE_PREMIER_RESERVE;
					if (video.getLiveStart() != null && video.getLiveEnd() == null) return VideoEntity.TYPE_LIVE_LIVE;
					if (video.getLiveStart() != null && video.getLiveEnd() != null) return VideoEntity.TYPE_LIVE_ARCHIVE;
				}
			}
			if ("uploaded".equals(video.getUploadStatus())) {
				if (video.getLiveStart() == null && video.getLiveEnd() == null) return VideoEntity.TYPE_LIVE_RESERVE;
				if (video.getLiveStart() != null && video.getLiveEnd() == null) return VideoEntity.TYPE_LIVE_LIVE;
				if (video.getLiveStart() != null && video.getLiveEnd() != null) return VideoEntity.TYPE_LIVE_ARCHIVE;
			}
		} else {
			// 2回目以降
			if (video.getType().startsWith("Premier")) {
				if (video.getLiveStart() == null && video.getLiveEnd() == null) return VideoEntity.TYPE_PREMIER_RESERVE;
				if (video.getLiveStart() != null && video.getLiveEnd() == null) return VideoEntity.TYPE_PREMIER_LIVE;
				if (video.getLiveStart() != null && video.getLiveEnd() != null) return VideoEntity.TYPE_PREMIER_UPLOAD;
			}
			if (video.getType().startsWith("Live")) {
				if (video.getLiveStart() == null && video.getLiveEnd() == null) return VideoEntity.TYPE_LIVE_RESERVE;
				if (video.getLiveStart() != null && video.getLiveEnd() == null) return VideoEntity.TYPE_LIVE_LIVE;
				if (video.getLiveStart() != null && video.getLiveEnd() != null) return VideoEntity.TYPE_LIVE_ARCHIVE;
			}

			return video.getType();
		}
		return VideoEntity.TYPE_UNKNOWN;
	}
}
