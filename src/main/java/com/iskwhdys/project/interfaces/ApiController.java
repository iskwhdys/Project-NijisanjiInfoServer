package com.iskwhdys.project.interfaces;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
	public List<VideoEntity> getliveVideos(Model model) {
		return videoRepository.findLive() ;
	}

	@ResponseBody
	@RequestMapping(value = "/api/dailyVideos", method = RequestMethod.GET)
	public List<VideoEntity> getDailyVideos(Model model) {
		return videoRepository.find24HourUpload() ;
	}

	@ResponseBody
	@RequestMapping(value = "/api/dailyArchives", method = RequestMethod.GET)
	public List<VideoEntity> getDailyArchives(Model model) {
		return videoRepository.find24HourArchive() ;
	}

	@ResponseBody
	@RequestMapping(value = "/api/channel/{id}", method = RequestMethod.GET )
	public ChannelEntity getChannelThumbnail(@PathVariable("id") String id, Model model) {;
		return channelRepository.findById(id).get();

//		System.out.println(id);
//		return id;
	}

}
