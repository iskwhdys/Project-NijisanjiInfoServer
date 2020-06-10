package com.iskwhdys.project.application;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.infra.util.CacheImage;
import com.iskwhdys.project.infra.util.CacheObject;
import com.iskwhdys.project.infra.util.ImageEditor;
import com.iskwhdys.project.infra.youtube.YoutubeApi;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class VideoThumbnailService {

  private @Autowired VideoRepository vr;
  private @Autowired VideoDeliveryService vds;
  private @Autowired YoutubeApi youtubeApi;

  @Value("${nis.path.image.thunbnail}")
  String imageDirectory;

  CacheImage cacheImage;

  @PostConstruct
  public void init() {
    cacheImage = new CacheImage(imageDirectory, ".jpg", this::resize, false);
    if (youtubeApi.enabled()) {
      vds.live().forEach(this::loadImage);
      vds.upload().forEach(this::loadImage);
      vds.archive().forEach(this::loadImage);
      vds.premier().forEach(this::loadImage);
      vds.schedule().forEach(this::loadImage);
    }
    log.info("Complate VideoThumbnailService Init()");
  }

  private void loadImage(VideoEntity v) {
    try {
      getThumbnailMini(v.getId());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public CacheObject getThumbnailMini(String videoId) {
    return getThumbnails(videoId, true);
  }

  public CacheObject getThumbnail(String videoId) {
    return getThumbnails(videoId, false);
  }

  private CacheObject getThumbnails(String videoId, boolean mini) {
    CacheObject obj = cacheImage.read(videoId, mini);
    if (obj != null) return obj;

    var entity = vr.findById(videoId);
    if (entity.isEmpty()) {
      throw new ResourceAccessException("Not found channel id");
    }
    if (!cacheImage.download(videoId, entity.get().getThumbnailUrl())) {
      throw new ResourceAccessException("Download error");
    }

    return cacheImage.read(videoId, mini);
  }

  public boolean downloadThumbnails(VideoEntity entity) {
    return cacheImage.download(entity.getId(), entity.getThumbnailUrl());
  }

  private byte[] resize(byte[] bytes) {
    try {
      bytes = ImageEditor.resize(bytes, 176, 132, 1.0f);
      bytes = ImageEditor.trim(bytes, 176, 98, 1.0f);
    } catch (IOException e) {
      //
    }
    return bytes;
  }
}
