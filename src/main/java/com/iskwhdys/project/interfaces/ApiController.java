package com.iskwhdys.project.interfaces;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;

@CrossOrigin
@Controller
public class ApiController {

	@Autowired
	VideoRepository videoRepository;

	@ResponseBody
	@RequestMapping(value = "/api/dailyVideos", method = RequestMethod.GET)
	public List<VideoEntity> getDailyVideos(Model model) {
		return videoRepository.findTodayUpload() ;
	}

	@ResponseBody
	@RequestMapping(value = "/api/liveVideos", method = RequestMethod.GET)
	public List<VideoEntity> getliveVideos(Model model) {
		return videoRepository.findLive() ;
	}


}
