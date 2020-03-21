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

@Service
public class VideoThumbnailService {

  @Autowired VideoRepository vr;

  @Value("${nis.path.image.thunbnail}")
  String imageDirectory;
  CacheImage cacheImage;

  @PostConstruct
  public void init() {
    cacheImage = new CacheImage(imageDirectory, ".jpg", this::resize);
  }

  public CacheObject getThumbnailMini(String videoId) {
    CacheObject obj = cacheImage.readMini(videoId);
    if (obj != null) return obj;

    var entity = vr.findById(videoId);
    if (entity.isEmpty()) {
      throw new ResourceAccessException("Not found video id");
    }
    if (!cacheImage.download(videoId, entity.get().getThumbnailUrl())) {
      throw new ResourceAccessException("Download error");
    }

    return cacheImage.readMini(videoId);
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
