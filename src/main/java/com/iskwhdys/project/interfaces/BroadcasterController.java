package com.iskwhdys.project.interfaces;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.iskwhdys.project.domain.broadcaster.BroadcasterEntity;
import com.iskwhdys.project.domain.broadcaster.BroadcasterRepository;

@CrossOrigin
@RestController
public class BroadcasterController {

  @Autowired BroadcasterRepository br;

  @GetMapping("/api/broadcaster")
  public List< BroadcasterEntity> getBroadcasterWhereChannel(@RequestParam(required = false) String channelId) {
    if(channelId == null) {
      return br.findAll();
    }

    return br.findByYoutubeOrYoutube2(channelId,channelId);
  }

  @GetMapping("/api/broadcaster/{id}")
  public BroadcasterEntity getBroadcaster(@PathVariable String id) {
    return br.findById(id).orElse(null);
  }


}
