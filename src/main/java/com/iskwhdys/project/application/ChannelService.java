package com.iskwhdys.project.application;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.interfaces.video.VideoFactory;
import com.iskwhdys.project.interfaces.video.VideoSpecification;

@Service
@Transactional
public class ChannelService {

	@Autowired
	ChannelRepository channelRepository;
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
				video = VideoFactory.updateViaXmlElement(element, video, true);
				VideoSpecification.setThumbnail(video, restTemplate);
			}
			videos.add(video);
		}
		videoRepository.saveAll(videos);
		return videos;
	}

	public List<VideoEntity> update10min() {

		var videos = new ArrayList<VideoEntity>();

		// 全チャンネルのXMLを取得し、動画情報のElementを作成
		List<Map<String, Element>> elementMaps = channelRepository.findAll().stream().map(c -> {
			String url = Constans.FEEDS_URL + "?channel_id=" + c.getId();
			byte[] bytes = restTemplate.getForObject(url, byte[].class);
			return bytesToIdElementMap(bytes);
		}).collect(Collectors.toList());

		// 全動画情報Elementを元にEntityを作成 or 情報の更新
		elementMaps.stream().forEach(map -> map.entrySet().stream().forEach(entry -> {
			String id = entry.getKey();
			Element element = entry.getValue();

			var video = videoRepository.findById(id).orElse(null);
			if (video == null) {
				video = VideoFactory.createViaXmlElement(element);
				VideoSpecification.setThumbnail(video, restTemplate);
				VideoSpecification.updateViaApi(video, restTemplate);
				System.out.println("New -> " + video.getType() + " " + video.toString());

			} else if (video.isUpload() || video.isPremierUpload() || video.isLiveArchive()) {

				VideoFactory.updateViaXmlElement(element, video, false);

			} else if (video.isPremierLive() || video.isLiveLive()) {

				VideoFactory.updateViaXmlElement(element, video, true);
				VideoSpecification.setThumbnail(video, restTemplate);
				VideoSpecification.updateLiveInfoViaApi(video, restTemplate);
				System.out.println("Live -> " + video.getType() + " " + video.toString());

			} else if (video.isPremierReserve() || video.isLiveReserve()) {

				VideoFactory.updateViaXmlElement(element, video, true);
				VideoSpecification.setThumbnail(video, restTemplate);

				if (video.getLiveSchedule().before(new Date())) {
					VideoSpecification.updateReserveInfoViaApi(video, restTemplate);
					System.out.println("Reserve -> " + video.getType() + " " + video.toString());
				}
			}
			videos.add(video);
		}));

		// XMLにないライブ情報の更新（非公開系？ライブ完了してホーム(XML)に公開されるまでの動画がここに来た）
		for (var video : videoRepository.findLive()) {
			if (videos.stream().anyMatch(v -> v.getId().equals(video.getId()))) {
				continue; // XMLにあるなら処理しない
			}
			VideoSpecification.updateViaApi(video, restTemplate);
			// サムネ更新したいけどXMLがないのでURLが不明
			// 一応動的に生成は出来るけど、ライブの最終的にサムネから変わることもないと思う
			// TODO:本関数(update10min)が正常に動作しなかったときのための日次関数にはサムネ取得入れたほうがいい
			videos.add(video);
			System.out.println("None ->" + video.getType() + " " + video.toString());
		}

		videoRepository.saveAll(videos);

		return videos;
	}

	public List<VideoEntity> update1day() {

		var videos = new ArrayList<VideoEntity>();

		// 全チャンネルのXMLを取得し、動画情報のElementを作成
		List<Map<String, Element>> elementMaps = channelRepository.findAll().stream().map(c -> {
			String url = Constans.FEEDS_URL + "?channel_id=" + c.getId();
			byte[] bytes = restTemplate.getForObject(url, byte[].class);
			return bytesToIdElementMap(bytes);
		}).collect(Collectors.toList());

		// 全動画情報Elementを元にEntityを作成 or 情報の更新
		elementMaps.stream().forEach(map -> map.entrySet().stream().forEach(entry -> {
			String id = entry.getKey();
			Element element = entry.getValue();

			var video = videoRepository.findById(id).orElse(null);
			if (video == null) {
				video = VideoFactory.createViaXmlElement(element);
				VideoSpecification.setThumbnail(video, restTemplate);
				VideoSpecification.updateViaApi(video, restTemplate);
				System.out.println("New -> " + video.getType() + " " + video.toString());
			} else {
				VideoFactory.updateViaXmlElement(element, video, true);
				VideoSpecification.setThumbnail(video, restTemplate);

				// API節約で48時間以内にアップされた動画のみ更新
				int hour48 = 1000 * 60 * 60 * 48;
				if (video.getLiveStart() != null) {
					if (new Date().getTime() - video.getLiveStart().getTime() < hour48) {
						VideoSpecification.updateViaApi(video, restTemplate);
						System.out.println("Update -> " + video.getType() + " " + video.toString());
					}
				} else if (new Date().getTime() - video.getUploadDate().getTime() < hour48) {
					VideoSpecification.updateViaApi(video, restTemplate);
					System.out.println("Update -> " + video.getType() + " " + video.toString());
				}
			}
			videos.add(video);
		}));

		// XMLにないライブ情報の更新（非公開系？ライブ完了してホーム(XML)に公開されるまでの動画がここに来た）
		for (var video : videoRepository.findLive()) {
			if (videos.stream().anyMatch(v -> v.getId().equals(video.getId()))) {
				continue; // XMLにあるなら処理しない
			}
			VideoSpecification.updateViaApi(video, restTemplate);
			// TODO:サムネ取得
			videos.add(video);
			System.out.println("None ->" + video.getType() + " " + video.toString());
		}

		videoRepository.saveAll(videos);

		return videos;
	}

	private Map<String, Element> bytesToIdElementMap(byte[] xmlBytes) {

		var is = new ByteArrayInputStream(xmlBytes);
		Element root = null;
		try {
			root = new SAXBuilder().build(is).getRootElement();
		} catch (Exception e) {
			System.out.println(e);
		}

		var entries = root.getChildren().stream().filter(p -> p.getName().contains("entry"))
				.collect(Collectors.toList());
		var map = new HashMap<String, Element>();
		for (var element : entries) {
			String id = element.getChildren().stream().filter(e -> e.getName().equals("videoId")).findFirst().get()
					.getValue();

			map.put(id, element);
		}
		return map;
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
