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
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.SizeSaveVideoEntity;
import com.iskwhdys.project.domain.video.SizeSaveVideoRepository;
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
	SizeSaveVideoRepository ssvr;

	@ResponseBody
	@RequestMapping(value = "/api/video", method = RequestMethod.GET)
	public List<SizeSaveVideoEntity> getVideos(
			@RequestParam("type") String type,
			@RequestParam("mode") String mode,
			@RequestParam(name = "from", required = false) String from, Model model) {

		if ("live".equals(type)) {
			return ssvr.findByTypeInOrderByLiveStartDesc(VideoEntity.TYPE_LIVES);
		} else if ("upload".equals(type)) {
			if ("new".equals(mode)) {
				return ssvr.findByTypeInAndUploadDateBetweenOrderByUploadDateDesc(VideoEntity.TYPE_UPLOADS,
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 2)), new Date());
			} else if ("get".equals(mode)) {
				return ssvr.findTop10ByTypeInAndUploadDateBeforeOrderByUploadDateDesc(
						VideoEntity.TYPE_UPLOADS,
						Common.toDate(from));
			}
		} else if ("archive".equals(type)) {
			if ("new".equals(mode)) {
				return ssvr.findByTypeEqualsAndLiveStartBetweenOrderByLiveStartDesc(
						VideoEntity.TYPE_LIVE_ARCHIVE,
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)), new Date());
			} else if ("get".equals(mode)) {
				return ssvr.findTop30ByTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(
						VideoEntity.TYPE_LIVE_ARCHIVE,
						Common.toDate(from));

			}
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
	@RequestMapping(value = "/api/channel/{id}/thumbnail_mini", method = RequestMethod.GET)
	public ResponseEntity<byte[]> getChannelThumbnailMini(@PathVariable("id") String id, Model model) {
		String base64 = cr.findByIdThumbnailMini(id);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(Common.Base64ImageToByte(base64), headers, HttpStatus.OK);

	}
}
