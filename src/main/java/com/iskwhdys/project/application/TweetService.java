package com.iskwhdys.project.application;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

  public void tweetReserves() {

    var videos =
        videoRepository.findByEnabledTrueAndTypeInAndLiveScheduleBetweenOrderByLiveSchedule(
            VideoEntity.TYPE_RESERVES,
            new Date(new Date().getTime() + (1000 * 60 * 29)),
            new Date(new Date().getTime() + (1000 * 60 * 30)));

    var livers = new ArrayList<String>();
    for (VideoEntity video : videos) {
      try {
        var liver =
            broadcasterRepository.findByYoutubeOrYoutube2(
                video.getChannelId(), video.getChannelId());
        if (liver != null) {
          if (!livers.contains(liver.getName())) {
            livers.add(liver.getName());
          }
        } else {
          channelRepository
              .findById(video.getChannelId())
              .ifPresent(
                  channel -> {
                    if (!livers.contains(channel.getTitle())) {
                      livers.add(channel.getTitle());
                    }
                  });
        }
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      }
    }
    if (livers.isEmpty()) return;

    try {

      String time = new SimpleDateFormat("HH:mm").format(videos.get(0).getLiveSchedule());
      var str = new StringBuilder();
      str.append(time).append("から配信予定のライバーです。\r\n");
      str.append(String.join(" ", livers));
      str.append("\r\n");
      str.append("\r\n");
      str.append("配信予定と現在配信中の情報はこちら：https://nijisanji-live.com/schedules");
      twitterApi.tweet(str.toString());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  public void tweetReserve(VideoEntity video) {

    try {
      var liver =
          broadcasterRepository.findByYoutubeOrYoutube2(video.getChannelId(), video.getChannelId());

      String time = new SimpleDateFormat("HH:mm").format(video.getLiveSchedule());
      var str = new StringBuilder();
      if (liver == null) {
        channelRepository
            .findById(video.getChannelId())
            .ifPresent(
                channel ->
                    str.append(time)
                        .append("から チャンネル「")
                        .append(channel.getTitle())
                        .append("」で配信予定です。\r\n"));
      } else {
        str.append(time).append("から").append(liver.getName()).append("が配信予定です。\r\n");
      }

      str.append("\r\n");
      str.append("配信予定と現在配信中の情報はこちら：https://nijisanji-live.com/schedules");

      twitterApi.tweet(str.toString());
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  private String getTweetMessage(
      VideoEntity video, boolean useBroadcaster, boolean useChannel, boolean use2j3jInfo) {
    var result = new StringBuilder();

    if (video.liveElapsedMinute() == 0) {
      result.append("～配信開始～").append("\r\n");
    } else {
      result.append("～").append(video.liveElapsedMinute()).append("分前に配信開始～").append("\r\n");
    }

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

//        appendTag(result, liver.getName().replace("★", ""));
//        appendTag(result, liver.getTagLive());
//        appendTag(result, liver.getTagTweet());
//        appendTagEx(result, liver.getTagEx(), video);

        result.append(liver.getName()) .append("\r\n");
      }
    }

    if (use2j3jInfo) {
//      result.append("\r\n");
//      result.append("その他配信情報：https://nijisanji-live.com");
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
