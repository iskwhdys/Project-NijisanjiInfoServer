package com.iskwhdys.project.interfaces;

import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.SizeSaveVideoEntity;
import com.iskwhdys.project.domain.video.SizeSaveVideoRepository;
import com.iskwhdys.project.domain.video.VideoRepository;

@CrossOrigin
@Controller
public class ApiController {

	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	VideoRepository videoRepository;

	@Autowired
	SizeSaveVideoRepository ssVideoRepository;
	@ResponseBody
	@RequestMapping(value = "/api/video", method = RequestMethod.GET)
	public List<SizeSaveVideoEntity> getVideos(
			@RequestParam("type") String type,
			@RequestParam("mode") String mode,
			@RequestParam(name="from", required = false) String from, Model model) {

		if ("live".equals(type)) {
			return ssVideoRepository.findByTypeInOrderByLiveStartDesc(List.of("PremierLive", "LiveLive"));
		}
		else if ("upload".equals(type)) {
			if ("new".equals(mode)) {
				return ssVideoRepository.findByTypeInAndUploadDateBetweenOrderByUploadDateDesc(
						List.of("PremierUpload", "Upload"),
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 2)), new Date()
						);
			} else if ("get".equals(mode)) {
				return ssVideoRepository.findTop10ByTypeInAndUploadDateBeforeOrderByUploadDateDesc(
					List.of("PremierUpload", "Upload"),
					Common.toDate(from));
			}
		} else if ("archive".equals(type)) {
			if ("new".equals(mode)) {
				return ssVideoRepository.findByTypeEqualsAndLiveStartBetweenOrderByLiveStartDesc(
						"LiveArchive",
						new Date(new Date().getTime() - (1000 * 60 * 60 * 24 * 1)), new Date()
						);
			} else if ("get".equals(mode)) {
				return ssVideoRepository.findTop30ByTypeEqualsAndLiveStartBeforeOrderByLiveStartDesc(
						"LiveArchive",
						Common.toDate(from));

			}
		}
		return null;
	}

	@ResponseBody
	@RequestMapping(value = "/api/video/{id}/thumbnail_mini", method = RequestMethod.GET)
	public ResponseEntity<byte[]> geThumbnailMini(@PathVariable("id") String id, Model model) {
		String base64 = videoRepository.findById(id).get().getThumbnailMini();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.IMAGE_JPEG);
		return new ResponseEntity<>(Common.Base64ImageToByte(base64), headers, HttpStatus.OK);
	}

	@ResponseBody
	@RequestMapping(value = "/api/channel/{id}", method = RequestMethod.GET)
	public ChannelEntity getChannel(
			@PathVariable("id") String id,
			@RequestParam Map<String, String> params, Model model) {
		if (params.containsKey("MiniThumbnail")) {
			return channelRepository.findByIdThumbnailMini(id);
		} else {
			return channelRepository.findById(id).get();
		}
	}
}
