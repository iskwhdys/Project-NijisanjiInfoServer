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
import com.iskwhdys.project.infra.util.ImageEditor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ChannelImageService {

  @Autowired ChannelRepository cr;

  @Value("${nis.path.image.channel}")
  String thumbnailPath;
  CacheImage cacheImage;

  @PostConstruct
  public void init() {
    cacheImage = new CacheImage(thumbnailPath, ".jpg", this::resize);
  }

  public byte[] getThumbnailMini(String channelId) {
    return getThumbnails(channelId, true);
  }

  public byte[] getThumbnail(String channelId) {
    return getThumbnails(channelId, false);
  }

  private byte[] getThumbnails(String channelId, boolean mini) {
    byte[] bytes = cacheImage.read(channelId, mini);
    if (bytes.length != 0) return bytes;

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
