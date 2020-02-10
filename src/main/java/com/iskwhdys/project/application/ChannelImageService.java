package com.iskwhdys.project.application;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.infra.util.ImageEditor;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ChannelImageService {

	@Autowired
	ChannelRepository cr;

	@Value("${nis.path.image.channel}")
	String thumbnailPath;

	private Map<String, byte[]> cache = new HashMap<>();
	private RestTemplate restTemplate = new RestTemplate();

	public byte[] getThumbnailMini(String channelID) {
		return getThumbnails(channelID, "_mini.jpg");
	}

	public byte[] getThumbnail(String channelID) {
		return getThumbnails(channelID, ".jpg");
	}

	private  byte[] getThumbnails(String channelID, String suffix) {
		String key = channelID + suffix;

		if (cache.containsKey(key)) {
			log.trace("Thumbnail-Cache:" + key);
			return cache.get(key);
		}

		Path resizePath = Paths.get(thumbnailPath, channelID + suffix);
		if (Files.exists(resizePath)) {
			try {
				cache.put(key, Files.readAllBytes(resizePath));
				log.info("Thumbnail-Read:" + key);
			} catch (IOException e) {
				throw new ResourceAccessException("File read error", e);
			}
			return cache.get(key);
		}

		var entity = cr.findById(channelID);
		if (entity.isEmpty()) {
			throw new ResourceAccessException("Not found video id");
		}

		log.info("Thumbnail-Dowmload:" + entity.get().getTitle());
		boolean success = downloadThumbnail(entity.get());
		if (!success) {
			throw new ResourceAccessException("Download error");
		}

		try {
			cache.put(key, Files.readAllBytes(resizePath));
		} catch (IOException e) {
			throw new ResourceAccessException("File read error", e);
		}
		return cache.get(key);
	}


	public boolean downloadThumbnail(ChannelEntity entity) {
		try {
			var dirPath = Paths.get(thumbnailPath);

			Path orginPath = Paths.get(dirPath.toString(), entity.getId() + ".jpg");
			Path resizePath = Paths.get(dirPath.toString(), entity.getId() + "_mini.jpg");

			byte[] bytes = restTemplate.getForObject(entity.getThumbnailUrl(), byte[].class);

			Files.createDirectories(dirPath);
			Files.write(orginPath, bytes, StandardOpenOption.CREATE);

			bytes = ImageEditor.resize(bytes, 30, 30, 1.0f);
			Files.write(resizePath, bytes, StandardOpenOption.CREATE);
		} catch (Exception e) {
			log.error(entity.getThumbnailUrl());
			return false;
		}
		return true;
	}


}
