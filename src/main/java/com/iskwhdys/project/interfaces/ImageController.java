package com.iskwhdys.project.interfaces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import com.iskwhdys.project.application.BroadcasterImageService;
import com.iskwhdys.project.application.ChannelImageService;
import com.iskwhdys.project.application.VideoThumbnailService;
import com.iskwhdys.project.infra.util.CacheObject;

@CrossOrigin
@RestController
@RequestMapping("/api/image")
public class ImageController {

  @Autowired VideoThumbnailService videoThumbnailService;
  @Autowired ChannelImageService channelImageService;
  @Autowired BroadcasterImageService broadcasterImageService;

  @GetMapping("/video/{id}/thumbnail_mini")
  public ResponseEntity<byte[]> getVideoThumbnailMini(WebRequest swr, @PathVariable String id) {
    return getResponse(swr, videoThumbnailService.getThumbnailMini(id));
  }

  @GetMapping("/channel/{id}/thumbnail")
  public ResponseEntity<byte[]> getChannelThumbnail(WebRequest swr, @PathVariable String id) {
    return getResponse(swr, channelImageService.getThumbnail(id));
  }

  @GetMapping("/channel/{id}/thumbnail_mini")
  public ResponseEntity<byte[]> getChannelThumbnailMini(WebRequest swr, @PathVariable String id) {
    return getResponse(swr, channelImageService.getThumbnailMini(id));
  }

  @GetMapping("/broadcaster/{id}")
  public ResponseEntity<byte[]> getBroadcaster(WebRequest swr, @PathVariable String id) {
    return getResponse(swr, broadcasterImageService.getThumbnail(id));
  }

  private ResponseEntity<byte[]> getResponse(WebRequest swr, CacheObject obj) {
    HttpHeaders headers = new HttpHeaders();

    if (swr.checkNotModified(obj.getLastUpdate().getTime())) {
      return new ResponseEntity<>(HttpStatus.NOT_MODIFIED);
    } else {
      headers.setContentType(MediaType.IMAGE_JPEG);
      headers.setLastModified(obj.getLastUpdate().getTime());
      return new ResponseEntity<>(obj.getBytes(), headers, HttpStatus.OK);
    }
  }
}
