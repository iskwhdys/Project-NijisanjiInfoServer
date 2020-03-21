package com.iskwhdys.project.application;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import com.iskwhdys.project.domain.broadcaster.BroadcasterRepository;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.infra.util.CacheImage;
import com.iskwhdys.project.infra.util.CacheObject;

@Service
public class BroadcasterImageService {

  @Autowired BroadcasterRepository br;

  @Value("${nis.path.image.broadcaster}")
  String imageDirectory;

  CacheImage cacheImage;

  @PostConstruct
  public void init() {
    cacheImage = new CacheImage(imageDirectory, ".jpg");
  }

  public CacheObject getThumbnail(String broadcasterId) {
    return getThumbnails(broadcasterId, false);
  }

  private CacheObject getThumbnails(String broadcasterId, boolean mini) {
    CacheObject obj = cacheImage.read(broadcasterId, mini);
    if (obj != null) return obj;

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
