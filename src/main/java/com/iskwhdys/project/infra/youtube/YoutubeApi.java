package com.iskwhdys.project.infra.youtube;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class YoutubeApi {

  static final int YOUTUBE_LIKE_VALUE = 5;
  static final int YOUTUBE_DISLIKE_VALUE = 1;
  static final String URL_BASE = "https://www.googleapis.com/youtube/v3";

  RestTemplate restTemplate = new RestTemplate();

  @Value("${nis.api.youtube.videoKey}")
  String videoKey;

  public boolean enabled() {
    return !videoKey.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> downloadChannelItems(String channelId, String... parts) {
    if (videoKey.isEmpty()) return new ArrayList<>();

    String format = "%s/channels?id=%s&key=%s&part=%s";
    String url = String.format(format, URL_BASE, channelId, videoKey, String.join(",", parts));

    var items = restTemplate.getForObject(url, Map.class).get("items");
    return (List<Map<String, Object>>) items;
  }

  @SuppressWarnings("unchecked")
  public List<Map<String, Object>> downloadVideoItems(String videoId, String... parts) {
    if (videoKey.isEmpty()) return new ArrayList<>();

    String format = "%s/videos?id=%s&key=%s&part=%s";
    String url = String.format(format, URL_BASE, videoId, videoKey, String.join(",", parts));

    var items = restTemplate.getForObject(url, Map.class).get("items");
    return (List<Map<String, Object>>) items;
  }

  public int getLikeCount(int count, String strStarAve) {
    int starAve = Integer.parseInt(strStarAve.replace(".", ""));

    for (int i = count; i > 0; i--) {
      int like = YOUTUBE_LIKE_VALUE * i;
      int dislike = YOUTUBE_DISLIKE_VALUE * (count - i);
      double ave = (like + dislike) / (double) count;
      int num = (int) (ave * 100);

      if (num <= starAve) {
        return i;
      }
    }

    return 0;
  }
}
