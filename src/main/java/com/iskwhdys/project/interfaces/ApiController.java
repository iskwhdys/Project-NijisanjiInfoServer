package com.iskwhdys.project.interfaces;

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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.domain.broadcaster.BroadcasterEntity;
import com.iskwhdys.project.domain.broadcaster.BroadcasterRepository;
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
	BroadcasterRepository br;


	@ResponseBody
	@RequestMapping(value = "/api/video", method = RequestMethod.GET)
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

		return null;
	}



	@ResponseBody
	@RequestMapping(value = "/api/video/{id}/thumbnail_mini", method = RequestMethod.GET)
	public ResponseEntity<byte[]> geThumbnailMini(@PathVariable("id") String id, Model model) {
		String base64 = vr.findByIdThumbnailMini(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(Common.Base64ImageToByte(base64), headers, HttpStatus.OK);
	}


	@ResponseBody
	@RequestMapping(value = "/api/broadcaster", method = RequestMethod.GET)
	public List<BroadcasterEntity> getBroadcasters(Model model) {
		return br.findAllWithoutIcon();
	}
	@ResponseBody
	@RequestMapping(value = "/api/broadcaster/{id}/icon", method = RequestMethod.GET)
	public ResponseEntity<byte[]>  getBroadcasterIcon(@PathVariable("id") String id,Model model) {
		String base64 = br.findByIdIcon(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_PNG);
		return new ResponseEntity<>(Common.Base64ImageToByte(base64), headers, HttpStatus.OK);
	}


	@ResponseBody
	@RequestMapping(value = "/api/channel/{id}/thumbnail_mini", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getChannelThumbnailMini(@PathVariable("id") String id, Model model) {
		String base64 = cr.findByIdThumbnailMini(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(Common.Base64ImageToByte(base64), headers, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/api/channel/{id}/thumbnail", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getChannelThumbnail(@PathVariable("id") String id, Model model) {
		String base64 = cr.findByIdThumbnail(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(Common.Base64ImageToByte(base64), headers, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/api/channel", method = RequestMethod.GET)
	public List<ChannelEntity> getChannels(Model model) {
		return cr.findAllWithoutThumbnail();

	}
	@ResponseBody
	@RequestMapping(value = "/api/channel/{id}", method = RequestMethod.GET)
	public ChannelEntity getChannels(@PathVariable("id") String id, Model model) {
		return cr.findByIdWithoutThumbnail(id);

	}
}
