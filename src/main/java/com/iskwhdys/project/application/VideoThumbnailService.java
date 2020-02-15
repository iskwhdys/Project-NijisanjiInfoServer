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
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.infra.util.ImageEditor;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class VideoThumbnailService {

  @Autowired
  VideoRepository vr;

  @Value("${nis.path.image.thunbnail}")
  String thumbnailPath;

  private Map<String, byte[]> cache = new HashMap<>();
  private RestTemplate restTemplate = new RestTemplate();

  public byte[] getThumbnailMini(String videoId) {

    if (cache.containsKey(videoId)) {
      log.trace("Thumbnail-Cache:" + videoId);
      return cache.get(videoId);
    }

    Path resizePath = Paths.get(thumbnailPath, videoId + "_mini.jpg");
    if (Files.exists(resizePath)) {
      try {
        cache.put(videoId, Files.readAllBytes(resizePath));
        log.debug("Thumbnail-Read:" + videoId);
      } catch (IOException e) {
        throw new ResourceAccessException("File read error", e);
      }
      return cache.get(videoId);
    }

    var videoEntity = vr.findById(videoId);
    if (videoEntity.isEmpty()) {
      throw new ResourceAccessException("Not found video id");
    }

    log.debug("Thumbnail-Dowmload:" + videoEntity.get().getTitle());
    boolean success = downloadThumbnails(videoEntity.get());
    if (!success) {
      throw new ResourceAccessException("Download error");
    }

    try {
      cache.put(videoId, Files.readAllBytes(resizePath));
    } catch (IOException e) {
      throw new ResourceAccessException("File read error", e);
    }
    return cache.get(videoId);
  }

  public boolean downloadThumbnails(VideoEntity entity) {
    try {
      var dirPath = Paths.get(thumbnailPath);

      Path orginPath = Paths.get(dirPath.toString(), entity.getId() + ".jpg");
      Path resizePath = Paths.get(dirPath.toString(), entity.getId() + "_mini.jpg");

      byte[] bytes = restTemplate.getForObject(entity.getThumbnailUrl(), byte[].class);

      Files.createDirectories(dirPath);
      Files.write(orginPath, bytes, StandardOpenOption.CREATE);

      bytes = ImageEditor.resize(bytes, 176, 132, 1.0f);
      bytes = ImageEditor.trim(bytes, 176, 98, 1.0f);

      Files.write(resizePath, bytes, StandardOpenOption.CREATE);
    } catch (Exception e) {
      log.error(entity.getThumbnailUrl());
      return false;
    }
    return true;
  }


}
