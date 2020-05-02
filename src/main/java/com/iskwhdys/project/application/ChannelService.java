package com.iskwhdys.project.application;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.channel.ChannelSpecification;
import com.iskwhdys.project.infra.youtube.ChannelFeedXml;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Transactional
public class ChannelService {

  @Autowired ChannelRepository channelRepository;

  @Autowired ChannelImageService channelImageService;

  @Autowired ChannelSpecification channelSpecification;

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
      try {
        channelSpecification.update(channel);
        channelImageService.downloadThumbnail(channel);
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
    channelRepository.saveAll(channels);
    return channels;
  }

  public void xmlUpdate() {
    var channels = channelRepository.findAll();
    for (var channel : channels) {
      try {
        channel.setTitle(ChannelFeedXml.getChannelTitle(channel.getId()));
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
    channelRepository.saveAll(channels);
  }

  public List<String> getIds(){
    var channels = channelRepository.findByEnabledTrue();
    return channels.stream().map(ChannelEntity::getId).collect(Collectors.toList());

  }
}
