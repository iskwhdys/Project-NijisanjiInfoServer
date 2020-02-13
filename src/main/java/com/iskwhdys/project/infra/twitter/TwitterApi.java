package com.iskwhdys.project.infra.twitter;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

@Slf4j
@Component
public class TwitterApi {

  Twitter twitter;

  @Value("${nis.api.twitter.consumerKey}")
  String consumerKey;
  @Value("${nis.api.twitter.consumerSecret}")
  String consumerSecret;
  @Value("${nis.api.twitter.accessToken}")
  String accessToken;
  @Value("${nis.api.twitter.accessTokenSecret}")
  String accessTokenSecret;

  @PostConstruct
  private void init() {
    if (consumerKey.isEmpty() || consumerSecret.isEmpty() || accessToken.isEmpty()
        || accessTokenSecret.isEmpty()) {
      log.info("Twitterの各種キーが無いためツイートしません。");
      return;
    }

    twitter = TwitterFactory.getSingleton();
    twitter.setOAuthConsumer(consumerKey, consumerSecret);
    twitter.setOAuthAccessToken(new AccessToken(accessToken, accessTokenSecret));
  }

  public void tweet(String msg) {
    log.info(msg);

    if (twitter == null) {
      log.info("Twitterの各種キーが無いためツイートしません。");
      return;
    }

    try {
      twitter.updateStatus(msg);
    } catch (Exception e) {
      log.error(e.toString(), e);
    }
  }
}
