package com.iskwhdys.project.infra.youtube;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ChannelFeedXml {

  private static final String FEEDS_URL = "https://www.youtube.com/feeds/videos.xml?channel_id=";
  private static RestTemplate restTemplate = new RestTemplate();

  private ChannelFeedXml() {}

  public static String getChannelTitle(String id) {
    ResponseEntity<byte[]> bytes = restTemplate.getForEntity(FEEDS_URL + id, byte[].class);
    return bytesToChannelTitle(bytes.getBody());
  }

  public static Map<String, Element> getVideoElement(List<String> channelIdList) {
    var result = new HashMap<String, Element>();

    for (var channelId : channelIdList) {

      try {

        Map<String, Element> elements;
        ResponseEntity<byte[]> bytes;

        bytes = restTemplate.getForEntity(FEEDS_URL + channelId, byte[].class);
        elements = bytesToIdAndElementMap(bytes.getBody());
        if (elements == null) continue;

        result.putAll(elements);

      } catch (Exception e) {
        log.error("チャンネル情報取得エラー：" + channelId);
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

  private static String bytesToChannelTitle(byte[] xmlBytes) {

    var is = new ByteArrayInputStream(xmlBytes);
    Element root = null;
    try {
      root = new SAXBuilder().build(is).getRootElement();
    } catch (JDOMException | IOException e) {
      log.error(e.toString(), e);
      return null;
    }

    return root.getChildren()
        .stream()
        .filter(p -> p.getName().contains("title"))
        .collect(Collectors.toList())
        .get(0)
        .getValue();
  }
}
