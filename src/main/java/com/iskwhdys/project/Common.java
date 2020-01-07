package com.iskwhdys.project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Common {

	public static Date youtubeTimeToDate(String text) {
		try {
			String datetime = text.substring(0, 10) + " " + text.substring(11, 19);
			var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

			var cal = Calendar.getInstance();
			cal.setTime(sdf.parse(datetime));
			cal.add(Calendar.HOUR, Constans.UTC_TIME);

			return cal.getTime();

		} catch (ParseException e) {
			System.out.println(e);
		}
		return null;
	}
}
