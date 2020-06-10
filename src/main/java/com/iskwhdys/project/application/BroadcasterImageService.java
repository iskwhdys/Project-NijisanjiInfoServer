package com.iskwhdys.project.application;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import com.iskwhdys.project.domain.broadcaster.BroadcasterEntity;
import com.iskwhdys.project.domain.broadcaster.BroadcasterRepository;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.infra.util.CacheImage;
import com.iskwhdys.project.infra.util.CacheObject;
import com.iskwhdys.project.infra.youtube.YoutubeApi;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class BroadcasterImageService {

  private @Autowired BroadcasterRepository br;
  private @Autowired YoutubeApi youtubeApi;

  @Value("${nis.path.image.broadcaster}")
  String imageDirectory;

  CacheImage cacheImage;

  @PostConstruct
  public void init() {
    cacheImage = new CacheImage(imageDirectory, ".jpg");

    if (youtubeApi.enabled()) {
      br.findAll().forEach(this::loadImage);
    }

    log.info("Complate ChannelImageService Init()");
  }

  private void loadImage(BroadcasterEntity b) {
    try {
      getThumbnail(b.getId());
    } catch (Exception e) {
      // log.error(e.getMessage(), e);
    }
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
      throw new ResourceAccessException("Download error:" + entity.get().getName());
    }

    return cacheImage.read(broadcasterId, mini);
  }

  public boolean downloadThumbnail(ChannelEntity entity) {
    return cacheImage.download(entity.getId(), entity.getThumbnailUrl());
  }
}
