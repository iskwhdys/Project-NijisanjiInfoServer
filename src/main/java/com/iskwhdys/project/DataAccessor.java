package com.iskwhdys.project;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.web.client.RestTemplate;

@Controller
public class DataAccessor {
	private final String DB_URL = "http://localhost:3000/channels";
	private final String FEEDS_URL = "https://www.youtube.com/feeds/videos.xml";

	RestTemplate restTemplate = new RestTemplate();

	@Autowired
	ResourceLoader resourceLoader;

	public String getBase64(String url) {
		byte[] buf = restTemplate.getForObject(url, byte[].class);
		String base64 = Base64.getEncoder().encodeToString(buf);
		return base64;
	}

	public String getPlaneText(String url) {
		byte[] buf = restTemplate.getForObject(url, byte[].class);
		String text = new String(buf);
		return text;
	}

	public String getYoutubeRssFeedXml(String channelId) {

		String url = FEEDS_URL + "?channel_id=" + channelId;
		byte[] buf = restTemplate.getForObject(url, byte[].class);
		String text = new String(buf);
		return text;
	}

	public String getChannelRssFeedsXml(String channelId) {
		Map<String, ?>[] array = restTemplate.getForObject(DB_URL, Map[].class);
		Map<String, ?> map = array[0];

		String url = FEEDS_URL + "?channel_id=" + map.get("id").toString();
		System.out.println(url);
		byte[] data = restTemplate.getForObject(url, byte[].class);
		Path xmlPath = Paths.get("rssfeeds.xml");
		try {
			Files.write(xmlPath, data);
		} catch (Exception e) {
			System.out.println(e);
		}

		return readable(xmlPath);

	}

	public String readable(Path xmlPath) {

		Document doc = null;
		Element root = null;

		try {
			doc = new SAXBuilder().build(xmlPath.toFile());
			root = doc.getRootElement();
		} catch (Exception e) {
			System.out.println(e);
		}

		List<Element> list = root.getChildren().stream().filter(p -> p.getName().contains("entry"))
				.collect(Collectors.toList());

		for (Element entry : list) {
			System.out.println(entry.getName());
			for (Element element : entry.getChildren()) {
				switch (element.getName()) {
				case "a": {
					break;

				}
				default:
					throw new IllegalArgumentException("Unexpected value: " + element.getName());
				}

				System.out.println(element.getName());
			}

		}

		return null;
	}

}
