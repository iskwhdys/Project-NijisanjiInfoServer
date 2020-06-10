package com.iskwhdys.project.infra.youtube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import com.iskwhdys.project.domain.video.VideoRepository;

@Component
public class ChannelLive {

  static final String BASE_URL = "https://www.youtube.com/channel/%s/live";

  static final String REGEX_LIVE_STARTED =
      "<strong class=\"watch-time-text metadata-updateable-date-text\">.*ライブ配信開始</strong>";
  // static final String REGEX_LIVE_STARTED = "<strong class=\"watch-time-text
  // metadata-updateable-date-text\">.*</strong>";
  // static final String REGEX_LIVE_STARTED = ".*ライブ開始.*";
  static final String REGEX_VIDEO_ID = "'VIDEO_ID': \".*\"";

  static final String REGEX_NO_CONTENT_TITLE = "<title>YouTube</title>";

  @Autowired VideoRepository videoRepository;

  RestTemplate restTemplate = new RestTemplate();

  public String getId(String channelId) {

    ResponseEntity<String> obj;
    Pattern titlePattern = Pattern.compile(REGEX_NO_CONTENT_TITLE);
    while (true) {

      obj = restTemplate.getForEntity(String.format(BASE_URL, channelId), String.class);

      if (!titlePattern.matcher(obj.getBody()).find()) {
        break;
      }
    }
    Matcher liveMatch = Pattern.compile(REGEX_LIVE_STARTED).matcher(obj.getBody());

    if (liveMatch.find()) {

      Matcher videoId = Pattern.compile(REGEX_VIDEO_ID).matcher(obj.getBody());

      if (videoId.find()) {
        return videoId.group().split(":")[1].replace("\"", "").trim();
      } else {
        System.out.println("node videoid ");
      }
    }
    return null;
  }
}
