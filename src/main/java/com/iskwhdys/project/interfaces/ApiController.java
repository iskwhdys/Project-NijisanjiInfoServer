package com.iskwhdys.project.interfaces;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;

@CrossOrigin
@Controller
public class ApiController {

	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	VideoRepository videoRepository;

	@ResponseBody
	@RequestMapping(value = "/api/liveVideos", method = RequestMethod.GET)
	public List<VideoEntity> getliveVideos(Model model) { return videoRepository.findLive(); }

	@ResponseBody
	@RequestMapping(value = "/api/dailyVideos", method = RequestMethod.GET)
	public List<VideoEntity> getDailyVideos(Model model) { return videoRepository.find24HourUpload(); }

	@ResponseBody
	@RequestMapping(value = "/api/dailyArchives", method = RequestMethod.GET)
	public List<VideoEntity> getDailyArchives(Model model) { return videoRepository.find24HourArchive(); }

	@ResponseBody
	@RequestMapping(value = "/api/uploadVideos", method = RequestMethod.GET)
	public List<VideoEntity> getUploadVideos(
			@RequestParam("from") String from,
			@RequestParam("count") int count, Model model) {
		return videoRepository.findUpload(Common.toDate(from), count);
	}

	@ResponseBody
	@RequestMapping(value = "/api/archiveVideos", method = RequestMethod.GET)
	public List<VideoEntity> getArchiveVideos(
			@RequestParam("from") String from,
			@RequestParam("count") int count, Model model) {
		return videoRepository.findArchive(Common.toDate(from), count);
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
