package com.iskwhdys.project.application;

import java.io.ByteArrayInputStream;
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
		newVideos.forEach(v ->  VideoSpecification.updateViaApi(v, restTemplate));

		videoRepository.saveAll(newVideos);

		return newVideos;
	}


	public List<VideoEntity> updateTodayUploadVideo(ChannelEntity channel) {
		System.out.println("Channel:[" + channel.getId() + "] [" + channel.getTitle() + "]");

		// 今日アップロードされた動画のみ取得
		var videos = videoRepository.findToday(channel.getId());

		// 情報を更新
		videos.forEach(v -> VideoSpecification.updateViaApi(v, restTemplate));

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
