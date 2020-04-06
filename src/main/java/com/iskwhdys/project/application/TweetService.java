package com.iskwhdys.project.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.infra.twitter.TwitterApi;
import com.twitter.twittertext.TwitterTextParser;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class TweetService {

  @Autowired ChannelRepository channelRepository;
  @Autowired VideoRepository videoRepository;
  @Autowired TwitterApi twitterApi;

  public void tweet(String id) {
    var opt = videoRepository.findById(id);
    if (opt.isPresent()) {
      tweet(opt.get());
    } else {
      log.warn("Videoがありません。：" + id);
    }
  }

  public void tweet(VideoEntity video) {
    try {
      String msg = getTweetMessage(video, true, true);
      if (TwitterTextParser.parseTweet(msg).isValid) {
        twitterApi.tweet(msg);
        return;
      }

      msg = getTweetMessage(video, false, true);
      if (TwitterTextParser.parseTweet(msg).isValid) {
        twitterApi.tweet(msg);
        return;
      }

      msg = getTweetMessage(video, false, false);
      twitterApi.tweet(msg);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private String getTweetMessage(VideoEntity video, boolean useChannel, boolean use2j3jInfo) {
    var result = new StringBuilder();
    result.append("～配信開始～").append("\r\n");

    if (useChannel) {
      var channel = channelRepository.findById(video.getChannelId());
      if (channel.isPresent()) {
        result.append(channel.get().getTitle()).append("\r\n");
      }
    }

    result.append(video.getTitle()).append("\r\n");
    result.append("https://www.youtube.com/watch?v=" + video.getId());

    if (use2j3jInfo) {
      result.append("\r\n");
      result.append("\r\n");
      result.append("～その他の配信情報はこちら～\r\n");
      result.append("にじさんじライブ新着 https://nijisanji-live.com");
    }

    return result.toString();
  }
}
