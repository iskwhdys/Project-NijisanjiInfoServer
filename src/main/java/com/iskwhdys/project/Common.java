package com.iskwhdys.project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Common {

  public static final int UTC_TIME = 9;

  private Common() {}

  public static Date youtubeTimeToDate(String text) {

    String datetime = text.substring(0, 10) + " " + text.substring(11, 19);

    var cal = Calendar.getInstance();
    cal.setTime(toDate(datetime));
    cal.add(Calendar.HOUR, UTC_TIME);

    return cal.getTime();
  }

  public static Date toDate(String text) {

    var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    try {
      return sdf.parse(text);
    } catch (ParseException e) {
      log.error(e.toString(), e);
    }
    return null;
  }

}
