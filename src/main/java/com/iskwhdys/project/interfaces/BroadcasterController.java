package com.iskwhdys.project.interfaces;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import com.iskwhdys.project.domain.broadcaster.BroadcasterEntity;
import com.iskwhdys.project.domain.broadcaster.BroadcasterRepository;

@CrossOrigin
@Controller
public class BroadcasterController {

  @Autowired BroadcasterRepository br;

  @ResponseBody
  @GetMapping( "/api/broadcaster")
  public List<BroadcasterEntity> getChannels() {
    return br.findAll();
  }

  @ResponseBody
  @GetMapping("/api/broadcaster/{id}")
  public BroadcasterEntity getChannels(@PathVariable String id) {
    return br.findById(id).orElse(null);
  }
}
