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
import com.iskwhdys.project.application.BroadcasterImageService;
import com.iskwhdys.project.application.ChannelImageService;
import com.iskwhdys.project.application.VideoThumbnailService;

@CrossOrigin
@RestController
@RequestMapping("/api/image")
public class ImageController {

  @Autowired VideoThumbnailService videoThumbnailService;
  @Autowired ChannelImageService channelImageService;
  @Autowired BroadcasterImageService broadcasterImageService;

  @GetMapping("/video/{id}/thumbnail_mini")
  public ResponseEntity<byte[]> getVideoThumbnailMini(@PathVariable String id) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(videoThumbnailService.getThumbnailMini(id), headers, HttpStatus.OK);
  }

  @GetMapping("/channel/{id}/thumbnail")
  public ResponseEntity<byte[]> getChannelThumbnail(@PathVariable String id) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(channelImageService.getThumbnail(id), headers, HttpStatus.OK);
  }

  @GetMapping("/channel/{id}/thumbnail_mini")
  public ResponseEntity<byte[]> getChannelThumbnailMini(@PathVariable String id) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(channelImageService.getThumbnailMini(id), headers, HttpStatus.OK);
  }

  @GetMapping("/broadcaster/{id}")
  public ResponseEntity<byte[]> getBroadcaster(@PathVariable String id) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(broadcasterImageService.getThumbnail(id), headers, HttpStatus.OK);
  }
}
