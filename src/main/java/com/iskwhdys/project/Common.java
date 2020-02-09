package com.iskwhdys.project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Common {

	private Common() {}

	public static Date youtubeTimeToDate(String text) {

		String datetime = text.substring(0, 10) + " " + text.substring(11, 19);

		var cal = Calendar.getInstance();
		cal.setTime(toDate(datetime));
		cal.add(Calendar.HOUR, Constans.UTC_TIME);

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

	public static byte[] base64ImageToByte(String base64) {
		base64 = base64.substring(Constans.BASE64_HEADER_IMAGE.length());
		return Base64.getDecoder().decode(base64);
	}

}
