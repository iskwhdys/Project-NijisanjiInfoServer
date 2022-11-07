package com.iskwhdys.project.domain.channel;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.iskwhdys.project.infra.youtube.YoutubeApi;

@Component
public class ChannelSpecification {

  @Autowired YoutubeApi youtubeApi;

  public ChannelEntity update(ChannelEntity channel) {
    return update(channel, "snippet", "statistics");
  }

  @SuppressWarnings("unchecked")
  private ChannelEntity update(ChannelEntity channel, String... parts) {
    var items = youtubeApi.downloadChannelItems(channel.getId(), parts);
    if (items == null || items.isEmpty()) {
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
  private ChannelEntity setSnippet(ChannelEntity channel, Map<String, ?> map) {
    if (map == null) return channel;
    if (map.containsKey("title")) channel.setTitle(map.get("title").toString());
    if (map.containsKey("description")) channel.setDescription(map.get("description").toString());
    if (map.containsKey("thumbnails")) {
      var thumbnails = (Map<String, ?>) map.get("thumbnails");
      for (var key : new String[] {"default", "medium", "high"}) {
        if (thumbnails.containsKey(key)) {
          var th = (Map<String, ?>) thumbnails.get(key);
          channel.setThumbnailUrl(th.get("url").toString());
          break;
        }
      }
    }
    return channel;
  }

  private ChannelEntity setStatistics(ChannelEntity channel, Map<String, ?> map) {
    if (map == null) return channel;
    if (map.containsKey("subscriberCount"))
      channel.setSubscriberCount(toInteger(map, "subscriberCount"));
    return channel;
  }

  private Integer toInteger(Map<String, ?> map, String key) {
    return Integer.parseInt(map.get(key).toString());
  }
}
