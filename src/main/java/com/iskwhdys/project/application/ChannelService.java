package com.iskwhdys.project.application;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.interfaces.video.VideoFactory;
import com.iskwhdys.project.interfaces.video.VideoSpecification;

@Service
@Transactional
public class ChannelService {

	@Autowired
	VideoRepository videoRepository;

	private RestTemplate restTemplate = new RestTemplate();

	public List<VideoEntity> createNewVideos(ChannelEntity channel) {
		System.out.println("Channel:[" + channel.getId() + "] [" + channel.getTitle() + "]");

		// XMLからVideoEntityを生成
		byte[] bytes = restTemplate.getForObject(Constans.FEEDS_URL + "?channel_id=" + channel.getId(), byte[].class);
		var elements = bytesToElementList(bytes, "entry");
		var xmlVideos = elements.stream().map(e -> VideoFactory.createViaXmlElement(e)).collect(Collectors.toList());

		// 生成したVideoEntityでまだDBにないものだけ抽出
		var videos = videoRepository.findChannelId(channel.getId());
		var newVideos = xmlVideos.stream().filter(xv -> !videos.stream().anyMatch(v -> v.getId().equals(xv.getId())))
				.collect(Collectors.toList());

		// サムネを取得
		newVideos.forEach(v -> VideoSpecification.setThumbnail(v, restTemplate));

		// その他の情報を取得
		newVideos.forEach(v -> VideoSpecification.updateViaApi(v, restTemplate));

		videoRepository.saveAll(newVideos);

		return newVideos;
	}


	public List<VideoEntity> updateAllVideosViaXml(ChannelEntity channel) {
		System.out.println("Channel:[" + channel.getId() + "] [" + channel.getTitle() + "]");

		// XMLからVideoEntityを生成
		byte[] bytes = restTemplate.getForObject(Constans.FEEDS_URL + "?channel_id=" + channel.getId(), byte[].class);
		var elements = bytesToElementList(bytes, "entry");
		var videos = new ArrayList<VideoEntity>();

		for (var element : elements) {
			String id = element.getChildren().stream().filter(e -> e.getName().equals("videoId")).findFirst().get()
					.getValue();
			var video = videoRepository.findById(id).orElse(null);
			if (video == null) {
				video = VideoFactory.createViaXmlElement(element);
				VideoSpecification.setThumbnail(video, restTemplate);
			} else {
				video = VideoFactory.updateViaXmlElement(element, video, false);
			}
			videos.add(video);
		}
		videoRepository.saveAll(videos);
		return videos;
	}

	public List<VideoEntity> updateTodayUploadVideos(ChannelEntity channel) {
		System.out.println("Channel:[" + channel.getId() + "] [" + channel.getTitle() + "]");

		// 今日アップロードされた動画のみ取得
		var videos = videoRepository.findToday(channel.getId());

		// 情報を更新
		videos.forEach(v -> VideoSpecification.updateViaApi(v, restTemplate));

		videoRepository.saveAll(videos);

		return videos;
	}


	public List<VideoEntity> updateRealTime(ChannelEntity channel) {
		System.out.println("Channel:[" + channel.getId() + "] [" + channel.getTitle() + "]");

		// XMLからVideoEntityを生成
		byte[] bytes = restTemplate.getForObject(Constans.FEEDS_URL + "?channel_id=" + channel.getId(), byte[].class);
		var elements = bytesToElementList(bytes, "entry");
		var videos = new ArrayList<VideoEntity>();

		for (var element : elements) {
			String id = element.getChildren().stream().filter(e -> e.getName().equals("videoId")).findFirst().get()
					.getValue();
			var video = videoRepository.findById(id).orElse(null);
			if (video == null) {
				video = VideoFactory.createViaXmlElement(element);
				VideoSpecification.setThumbnail(video, restTemplate);
				VideoSpecification.updateViaApi(video, restTemplate);

				System.out.println("Video:[" + video.getId() + "] [" + video.getTitle() + "] - New");
			} else {
				video = VideoFactory.updateViaXmlElement(element, video, false);
			}
			videos.add(video);
		}
		videoRepository.saveAll(videos);
		return videos;
	}

	public List<VideoEntity> updateLiveVideo(ChannelEntity channel) {

		// XMLからVideoEntityを生成
		byte[] bytes = restTemplate.getForObject(Constans.FEEDS_URL + "?channel_id=" + channel.getId(), byte[].class);
		var elements = bytesToElementList(bytes, "entry");
		var videos = new ArrayList<VideoEntity>();

		for (var element : elements) {
			String id = element.getChildren().stream().filter(e -> e.getName().equals("videoId")).findFirst().get()
					.getValue();
			var video = videoRepository.findById(id).orElse(null);
			if (video == null) {
				video = VideoFactory.createViaXmlElement(element);
				VideoSpecification.setThumbnail(video, restTemplate);
				VideoSpecification.updateViaApi(video, restTemplate);
				System.out.println("New - Channel:[" + channel.getId() + "] [" + channel.getTitle() + "] Video:[" + video.getId() + "] [" + video.getTitle() + "]");
			} else {
				video = VideoFactory.updateViaXmlElement(element, video, false);

				if(video.getLiveSchedule() != null && video.getLiveEnd() == null && video.getViews() > 0) {
					VideoSpecification.updateViaApi(video, restTemplate);
					System.out.println("Live - Channel:[" + channel.getId() + "] [" + channel.getTitle() + "] Video:[" + video.getId() + "] [" + video.getTitle() + "]");
				}
			}

			videos.add(video);
		}
		videoRepository.saveAll(videos);
		return videos;
	}


	private List<Element> bytesToElementList(byte[] xmlBytes, String name) {

		var is = new ByteArrayInputStream(xmlBytes);
		Element root = null;
		try {
			root = new SAXBuilder().build(is).getRootElement();
		} catch (Exception e) {
			System.out.println(e);
		}
		var list = root.getChildren().stream().filter(p -> p.getName().contains(name)).collect(Collectors.toList());

		return list;
	}

}
