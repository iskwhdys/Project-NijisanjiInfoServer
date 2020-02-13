package com.iskwhdys.project.interfaces;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;

@CrossOrigin
@Controller
public class ChannelController {

  @Autowired
  ChannelRepository cr;

  @ResponseBody
  @GetMapping(value = "/api/channel")
  public List<ChannelEntity> getChannels(Model model) {
    return cr.findAll();

  }

  @ResponseBody
  @GetMapping(value = "/api/channel/{id}")
  public ChannelEntity getChannels(@PathVariable("id") String id, Model model) {
    return cr.findById(id).orElse(null);

  }
}
