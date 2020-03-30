package com.iskwhdys.project.infra.youtube;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelFeedXml {

  private static final String ATTRIBUTE_CACHED = "cached";
  private static final String FEEDS_URL = "https://www.youtube.com/feeds/videos.xml?channel_id=";
  private static RestTemplate restTemplate = new RestTemplate();
  private static Map<String, ResponseEntity<byte[]>> feedHistory = new HashMap<>();
  private static Map<String, Map<String, Element>> elementHistory = new HashMap<>();

  private ChannelFeedXml() {}

  public static Map<String, Element> getVideoElement(List<String> channelIdList) {
    var result = new HashMap<String, Element>();

    for (var channelId : channelIdList) {

      Map<String, Element> elements;
      ResponseEntity<byte[]> bytes;

      if (isCachedFeed(channelId)) {
        bytes = feedHistory.get(channelId);
        elements = elementHistory.get(channelId);
      } else {
        bytes = restTemplate.getForEntity(FEEDS_URL + channelId, byte[].class);
        elements = bytesToIdAndElementMap(bytes.getBody());
        if (elements == null) continue;
      }

      String cached = String.valueOf(isCachedFeed(channelId));
      for (Element element : elements.values()) {
        element.setAttribute(ATTRIBUTE_CACHED, cached);
      }

      feedHistory.put(channelId, bytes);
      elementHistory.put(channelId, elements);

      result.putAll(elements);
    }
    return result;
  }

  public static boolean isUncachedElement(Element element) {
    try {
      return !element.getAttribute(ATTRIBUTE_CACHED).getBooleanValue();
    } catch (DataConversionException e) {
      log.error(e.getMessage(), e);
      return true;
    }
  }

  private static boolean isCachedFeed(String channelId) {
    if (feedHistory.containsKey(channelId)) {
      return (new Date().getTime() < feedHistory.get(channelId).getHeaders().getExpires());
    } else {
      return false;
    }
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
