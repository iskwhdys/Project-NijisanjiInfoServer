package com.iskwhdys.project.application;

import java.io.IOException;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.infra.util.CacheImage;
import com.iskwhdys.project.infra.util.CacheObject;
import com.iskwhdys.project.infra.util.ImageEditor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChannelImageService {

  @Autowired ChannelRepository cr;

  @Value("${nis.path.image.channel}")
  String imageDirectory;

  CacheImage cacheImage;

  @PostConstruct
  public void init() {
    cacheImage = new CacheImage(imageDirectory, ".jpg", this::resize, true);

    cr.findAll().forEach(this::loadImage);

    log.info("Complate ChannelImageService Init()");
  }

  private void loadImage(ChannelEntity c) {
    try {
      getThumbnail(c.getId());
      getThumbnailMini(c.getId());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public CacheObject getThumbnailMini(String channelId) {
    return getThumbnails(channelId, true);
  }

  public CacheObject getThumbnail(String channelId) {
    return getThumbnails(channelId, false);
  }

  private CacheObject getThumbnails(String channelId, boolean mini) {
    CacheObject obj = cacheImage.read(channelId, mini);
    if (obj != null) return obj;

    var entity = cr.findById(channelId);
    if (entity.isEmpty()) {
      throw new ResourceAccessException("Not found channel id");
    }
    if (!cacheImage.download(channelId, entity.get().getThumbnailUrl())) {
      throw new ResourceAccessException("Download error");
    }

    return cacheImage.read(channelId, mini);
  }

  public boolean downloadThumbnail(ChannelEntity entity) {
    return cacheImage.download(entity.getId(), entity.getThumbnailUrl());
  }

  private byte[] resize(byte[] bytes) {
    try {
      bytes = ImageEditor.resize(bytes, 30, 30, 1.0f);
    } catch (IOException e) {
      //
    }
    return bytes;
  }
}
