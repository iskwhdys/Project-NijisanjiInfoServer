package com.iskwhdys.project.interfaces;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.application.ChannelImageService;
import com.iskwhdys.project.application.VideoService;
import com.iskwhdys.project.application.VideoThumbnailService;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoCardEntity;
import com.iskwhdys.project.domain.video.VideoCardRepository;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;

@CrossOrigin
@Controller
public class ApiController {

	@Autowired
	ChannelRepository cr;
	@Autowired
	VideoRepository vr;
	@Autowired
	VideoCardRepository vcr;

	@Autowired
	VideoService videoService;
	@Autowired
	VideoThumbnailService videoThumbnailService;
	@Autowired
	ChannelImageService channelImageService;

	@ResponseBody
	@GetMapping(value = "/api/video")
	public List<VideoCardEntity> getVideos(
			@RequestParam(name = "type", required = false) String type,
			@RequestParam(name = "mode", required = false) String mode,
			@RequestParam(name = "from", required = false) String from,
			@RequestParam(name = "channel_id", required = false) String channelId, Model model) {

		if ("live".equals(type)) {
			return vcr.findByTypeInOrderByLiveStartDesc(VideoEntity.TYPE_LIVES);
		} else if ("upload".equals(type)) {
			if ("new".equals(mode)) {
				return vcr.findByTypeInAndUploadDateBetweenOrderByUploadDateDesc(VideoEntity.TYPE_UPLOADS,
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 2)),
						new Date());
			} else if ("get".equals(mode)) {
				return vcr.findTop10ByTypeInAndUploadDateBeforeOrderByUploadDateDesc(
						VideoEntity.TYPE_UPLOADS,
						Common.toDate(from));
			}
		} else if ("archive".equals(type)) {
			if ("new".equals(mode)) {
				return vcr.findByTypeEqualsAndLiveStartBetweenOrderByLiveStartDesc(
						VideoEntity.TYPE_LIVE_ARCHIVE,
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)),
						new Date());
			} else if ("get".equals(mode)) {
				return vcr.findTop30ByTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(
						VideoEntity.TYPE_LIVE_ARCHIVE,
						Common.toDate(from));
			}
		} else if ("premier".equals(type)) {
			if ("new".equals(mode)) {
				return vcr.findByTypeEqualsAndLiveScheduleBetweenOrderByLiveSchedule(
						VideoEntity.TYPE_PREMIER_RESERVE,
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)),
						new Date(new Date().getTime() + (1000 * 60 * 60 * 24 * 2)));
			} else if ("get".equals(mode)) {
				return vcr.findTop10ByTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(
						VideoEntity.TYPE_PREMIER_RESERVE,
						Common.toDate(from));
			}
		} else if ("schedule".equals(type)) {
			if ("new".equals(mode)) {
				return vcr.findByTypeEqualsAndLiveScheduleBetweenOrderByLiveSchedule(
						VideoEntity.TYPE_LIVE_RESERVE,
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)),
						new Date(new Date().getTime() + (1000 * 60 * 60 * 24 * 2)));
			} else if ("get".equals(mode)) {
				return vcr.findTop30ByTypeEqualsAndLiveScheduleAfterOrderByLiveSchedule(
						VideoEntity.TYPE_LIVE_RESERVE,
						Common.toDate(from));
			}
		} else if ("channel_video".equals(type)) {
			return vcr.findTop10ByChannelIdEqualsAndUploadDateBeforeOrderByUploadDateDesc(channelId, new Date());
		}

		return new ArrayList<>();
	}


	@ResponseBody
	@GetMapping(value = "/api/video/{id}/thumbnail_mini")
	public ResponseEntity<byte[]> geThumbnailMini(@PathVariable("id") String id, Model model) {
		var bytes = videoThumbnailService.getThumbnailMini(id);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
	}

	@ResponseBody
	@GetMapping(value = "/api/channel/{id}/thumbnail_mini")
	public ResponseEntity<byte[]> getChannelThumbnailMini(@PathVariable("id") String id, Model model) {
		var bytes = channelImageService.getThumbnailMini(id);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
	}

	@ResponseBody
	@GetMapping(value = "/api/channel/{id}/thumbnail")
	public ResponseEntity<byte[]> getChannelThumbnail(@PathVariable("id") String id, Model model) {
		var bytes = channelImageService.getThumbnail(id);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
	}

	@ResponseBody
	@GetMapping(value = "/api/channel")
	public List<ChannelEntity> getChannels(Model model) {
		return cr.findAll();

	}
	@ResponseBody
	@GetMapping(value = "/api/channel/{id}")
	public ChannelEntity getChannels(@PathVariable("id") String id, Model model) {
		return cr.findById(id).orElse(null);

	}
}
