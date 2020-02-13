package com.iskwhdys.project.application;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.channel.ChannelSpecification;

@Service
@Transactional
public class ChannelService {

  @Autowired
  ChannelRepository channelRepository;

  @Autowired
  ChannelImageService channelImageService;

  @Autowired
  ChannelSpecification channelSpecification;

  public ChannelEntity createOrUpdate(String id) {
    var channel = channelRepository.findById(id).orElse(null);
    if (channel == null) {
      channel = new ChannelEntity();
      channel.setId(id);
    }

    channelSpecification.update(channel);
    channelImageService.downloadThumbnail(channel);

    channelRepository.save(channel);

    return channel;
  }

  public List<ChannelEntity> updateAll() {
    var channels = channelRepository.findAll();
    for (var channel : channels) {
      channelSpecification.update(channel);
      channelImageService.downloadThumbnail(channel);

      channelRepository.save(channel);
    }
    channelRepository.saveAll(channels);
    return channels;
  }

}
