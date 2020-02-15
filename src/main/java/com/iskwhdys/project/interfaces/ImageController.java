package com.iskwhdys.project.interfaces;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.iskwhdys.project.application.ChannelImageService;
import com.iskwhdys.project.application.VideoThumbnailService;

@RestController
@RequestMapping("/api/image")
public class ImageController {

  @Autowired
  VideoThumbnailService videoThumbnailService;
  @Autowired
  ChannelImageService channelImageService;

  @GetMapping(value = "/video/{id}/thumbnail_mini")
  public ResponseEntity<byte[]> geThumbnailMini(@PathVariable("id") String id, Model model) {
    var bytes = videoThumbnailService.getThumbnailMini(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }

  @GetMapping(value = "/channel/{id}/thumbnail")
  public ResponseEntity<byte[]> getChannelThumbnail(@PathVariable("id") String id, Model model) {
    var bytes = channelImageService.getThumbnail(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }

  @GetMapping(value = "/channel/{id}/thumbnail_mini")
  public ResponseEntity<byte[]> getChannelThumbnailMini(@PathVariable("id") String id,
      Model model) {
    var bytes = channelImageService.getThumbnailMini(id);

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.IMAGE_JPEG);
    return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
  }

}
