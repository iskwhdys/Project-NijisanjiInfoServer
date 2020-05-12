package com.iskwhdys.project.application;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.iskwhdys.project.domain.broadcaster.BroadcasterRepository;
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
  @Autowired BroadcasterRepository broadcasterRepository;
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
      String msg = getTweetMessage(video, true, true, true);
      if (TwitterTextParser.parseTweet(msg).isValid) {
        twitterApi.tweet(msg);
        return;
      }

      msg = getTweetMessage(video, true, false, true);
      if (TwitterTextParser.parseTweet(msg).isValid) {
        twitterApi.tweet(msg);
        return;
      }

      msg = getTweetMessage(video, true, false, false);
      if (TwitterTextParser.parseTweet(msg).isValid) {
        twitterApi.tweet(msg);
        return;
      }

      msg = getTweetMessage(video, false, false, false);
      twitterApi.tweet(msg);

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private String getTweetMessage(
      VideoEntity video, boolean useBroadcaster, boolean useChannel, boolean use2j3jInfo) {
    var result = new StringBuilder();
    result.append("～配信開始～").append("\r\n");
    result.append(video.getTitle()).append("\r\n");
    result.append("https://www.youtube.com/watch?v=" + video.getId() + "\r\n");

//    if (useChannel) {
//      channelRepository
//          .findById(video.getChannelId())
//          .ifPresent(
//              channel -> {
//                if (!result.toString().contains(channel.getTitle())) {
//                  result.append(channel.getTitle()).append("\r\n");
//                }
//              });
//    }

    if (useBroadcaster) {
      var liver =
          broadcasterRepository.findByYoutubeOrYoutube2(video.getChannelId(), video.getChannelId());
      if (liver != null) {
        appendTag(result, liver.getName().replace("★", ""));
        appendTag(result, liver.getTagLive());
        appendTag(result, liver.getTagTweet());
        appendTagEx(result, liver.getTagEx(), video);
        result.append("\r\n");
      }
    }

    if (use2j3jInfo) {
      result.append("\r\n");
      result.append("その他配信情報：https://nijisanji-live.com");
    }

    return result.toString();
  }

  private void appendTagEx(StringBuilder buf, String ex, VideoEntity video) {
    if (ex == null || ex.equals("")) {
      return;
    }

    for (String line : ex.split(",")) {
      var clm = line.split(":");

      if (clm[0].equals("Title")) {
        if (video.getTitle().contains(clm[1])) {
          appendTag(buf, clm[2]);
        }
      } else if (clm[0].equals("Type")) {
        if (clm[1].equals("Video") && (video.isPremierUpload() || video.isUpload())) {
          appendTag(buf, clm[2]);
        }
      }
    }
  }

  private void appendTag(StringBuilder buf, String tag) {
    if (tag == null || tag.equals("")) {
      return;
    }
    tag = "#" + tag;

    if (buf.toString().contains(tag)) {
      return;
    }
    buf.append(tag).append(" ");
  }
}
