package com.iskwhdys.project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public final class Constans {

	public static final String JSON_DB_URL = "http://localhost:3000/channels";

	public static final String FEEDS_URL = "https://www.youtube.com/feeds/videos.xml";

	public static final String YOUTUBE_API_URL = "https://www.googleapis.com/youtube/v3";

	public static final String YOUTUBE_API_KEY;

	public static final int UTC_TIME = 9;

	public static final int YOUTUBE_LIKE_VALUE = 5;
	public static final int YOUTUBE_DISLIKE_VALUE = 1;


	static {
		String key = "";

		try {
			key = Files.readAllLines(Paths.get("YoutubeApiKey.txt")).get(0);
		} catch (IOException e) {

		}

		YOUTUBE_API_KEY = key;
	}



}
