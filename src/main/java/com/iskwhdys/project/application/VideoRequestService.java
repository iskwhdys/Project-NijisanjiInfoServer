package com.iskwhdys.project.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoFactory;
import com.iskwhdys.project.domain.video.VideoRepository;

@Service
public class VideoRequestService {

  @Autowired
  ChannelRepository channelRepository;
  @Autowired
  VideoRepository videoRepository;
  @Autowired
  VideoThumbnailService videoThumbnailService;

  @Autowired
  VideoFactory videoFactory;


}
