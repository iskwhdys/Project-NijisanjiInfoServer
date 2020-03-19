package com.iskwhdys.project.infra.youtube;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelFeedXml {

  private static final String FEEDS_URL = "https://www.youtube.com/feeds/videos.xml";
  private static RestTemplate restTemplate = new RestTemplate();
  private ChannelFeedXml() {}

  @SuppressWarnings("deprecation")
  public static Date getExpires(String id) {
    String url = FEEDS_URL + "?channel_id=" + id;
    HttpHeaders header = restTemplate.headForHeaders(url);

    return new Date(header.get("Expires").get(0));
  }

  public static Map<String, Element> getVideoElement(List<String> channelIdList) {
    var result = new HashMap<String, Element>();

    for (var channelId : channelIdList) {

      String url = FEEDS_URL + "?channel_id=" + channelId;

      byte[] bytes = restTemplate.getForObject(url, byte[].class);

      var map = bytesToIdAndElementMap(bytes);

      if (map != null) {
        result.putAll(map);
      }
    }

    return result;
  }

  private static Map<String, Element> bytesToIdAndElementMap(byte[] xmlBytes) {

    var is = new ByteArrayInputStream(xmlBytes);
    Element root = null;
    try {
      root = new SAXBuilder().build(is).getRootElement();
    } catch (JDOMException | IOException e) {
      log.error(e.toString(), e);
      return null;
    }

    var entries =
        root.getChildren()
            .stream()
            .filter(p -> p.getName().contains("entry"))
            .collect(Collectors.toList());

    var map = new HashMap<String, Element>();
    for (var element : entries) {
      var optElm =
          element.getChildren().stream().filter(e -> e.getName().equals("videoId")).findFirst();
      if (optElm.isPresent()) {
        String id = optElm.get().getValue();
        map.put(id, element);
      }
    }
    return map;
  }
}
