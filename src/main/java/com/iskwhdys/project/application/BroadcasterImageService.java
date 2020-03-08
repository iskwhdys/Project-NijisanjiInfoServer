package com.iskwhdys.project.application;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import com.iskwhdys.project.domain.broadcaster.BroadcasterRepository;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.infra.util.CacheImage;

@Service
public class BroadcasterImageService {

  @Autowired BroadcasterRepository br;

  @Value("${nis.path.image.broadcaster}")
  String imageDirectory;
  CacheImage cacheImage;

  @PostConstruct
  public void init() {
    cacheImage = new CacheImage(imageDirectory, ".png");
  }

  public byte[] getThumbnail(String broadcasterId) {
    return getThumbnails(broadcasterId, false);
  }

  private byte[] getThumbnails(String broadcasterId, boolean mini) {
    byte[] bytes = cacheImage.read(broadcasterId, mini);
    if (bytes.length != 0) return bytes;

    var entity = br.findById(broadcasterId);
    if (entity.isEmpty()) {
      throw new ResourceAccessException("Not found channel id");
    }
    if (!cacheImage.download(broadcasterId, entity.get().getIcon())) {
      throw new ResourceAccessException("Download error");
    }

    return cacheImage.read(broadcasterId, mini);
  }

  public boolean downloadThumbnail(ChannelEntity entity) {
    return cacheImage.download(entity.getId(), entity.getThumbnailUrl());
  }
}
