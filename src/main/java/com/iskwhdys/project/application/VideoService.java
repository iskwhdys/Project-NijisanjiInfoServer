package com.iskwhdys.project.application;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.interfaces.video.VideoFactory;
import com.iskwhdys.project.interfaces.video.VideoSpecification;

@Service
@Transactional
public class VideoService {

	Logger logger = LogManager.getLogger(VideoService.class);

	@Autowired
	ChannelRepository cr;
	@Autowired
	VideoRepository vr;

	private RestTemplate restTemplate = new RestTemplate();

	public List<VideoEntity> update10min() {
		// 全チャンネルのXMLを取得し、動画情報のElementを作成
		List<Map<String, Element>> elementMaps = getChannelVideoElements();

		// 全動画情報Elementを元にEntityを作成 or 情報の更新
		var videos = new ArrayList<VideoEntity>();
		elementMaps.stream().forEach(map -> map.entrySet().stream().forEach(entry -> {
			VideoEntity video = createOrUpdateVideo(entry.getKey(), entry.getValue());
			videos.add(video);
		}));

		// XMLにないライブ情報の更新（非公開系？ライブ完了してホーム(XML)に公開されるまでの動画がここに来た）
		for (var video : vr.findByTypeInAndEnabledTrueOrderByLiveStartDesc(VideoEntity.TYPE_LIVES)) {
			if (videos.stream().anyMatch(v -> v.getId().equals(video.getId()))) {
				continue; // XMLにあるなら処理しない
			}
			VideoSpecification.updateViaApi(video, restTemplate);
			Boolean enabled = VideoSpecification.setThumbnail(video, restTemplate);
			video.setEnabled(enabled);

			videos.add(video);
			logger.info("None ->" + video.getType() + " " + video.toString());
		}
		vr.saveAll(videos);
		return videos;
	}

	private List<Map<String, Element>> getChannelVideoElements() {
		// 全チャンネルのXMLを取得し、動画情報のElementを作成
		List<Map<String, Element>> elementMaps = cr.findAll().stream().map(c -> {
			String url = Constans.FEEDS_URL + "?channel_id=" + c.getId();
			byte[] bytes = restTemplate.getForObject(url, byte[].class);
			return bytesToIdElementMap(bytes);
		}).collect(Collectors.toList());

		return elementMaps;
	}

	private VideoEntity createOrUpdateVideo(String id, Element element) {

		var video = vr.findById(id).orElse(null);
		if (video == null) {
			video = VideoFactory.createViaXmlElement(element);
			VideoSpecification.setThumbnail(video, restTemplate);
			VideoSpecification.updateViaApi(video, restTemplate);
			logger.info("API New -> " + video.getType() + " " + video.toString());

		} else if (video.isUpload() || video.isPremierUpload()) {

			VideoFactory.updateViaXmlElement(element, video);
			if ((new Date().getTime() - video.getUploadDate().getTime()) < 1000 * 60 * 60 * 24) {
				// 公開して24時間以内の動画はサムネイルを更新する
				var success = VideoSpecification.setThumbnail(video, restTemplate);
				// サムネ更新が出来ない == 動画が非公開
				video.setEnabled(success);
				logger.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
			}

		} else if (video.isLiveArchive()) {

			VideoFactory.updateViaXmlElement(element, video);
			if ((new Date().getTime() - video.getLiveStart().getTime()) < 1000 * 60 * 60 * 24) {
				// 配信して24時間以内の動画はサムネイルを更新する
				var success = VideoSpecification.setThumbnail(video, restTemplate);
				// サムネ更新が出来ない == 動画が非公開
				video.setEnabled(success);
				logger.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
			}

		} else if (video.isPremierLive() || video.isLiveLive()) {

			VideoFactory.updateViaXmlElement(element, video);
			VideoSpecification.setThumbnail(video, restTemplate);
			VideoSpecification.updateLiveInfoViaApi(video, restTemplate);

			if (video.isPremierUpload() || video.isLiveArchive()) {
				VideoSpecification.updateLiveToArchiveInfoViaApi(video, restTemplate);
			}
			logger.info("API Live -> " + video.getType() + " " + video.toString());

		} else if (video.isPremierReserve() || video.isLiveReserve()) {

			VideoFactory.updateViaXmlElement(element, video);
			Boolean success = VideoSpecification.setThumbnail(video, restTemplate);
			video.setEnabled(success);

			if (video.getEnabled() && video.getLiveSchedule().before(new Date())) {
				// 配信予定日時を過ぎた動画を対象
				if ((new Date().getTime() - video.getLiveSchedule().getTime()) < 1000 * 60 * 60 * 24) {
					// 配信予定時間から24時間経過していない動画のみAPIを使用する
					VideoSpecification.updateReserveInfoViaApi(video, restTemplate);
					if (video.isPremierUpload() || video.isLiveArchive()) {
						VideoSpecification.updateLiveToArchiveInfoViaApi(video, restTemplate);
					}
					logger.info("API Reserve -> " + video.getType() + " " + video.toString());
				}
			}
		} else if (video.isUnknown()) {

			VideoFactory.updateViaXmlElement(element, video);
			VideoSpecification.setThumbnail(video, restTemplate);
			VideoSpecification.updateViaApi(video, restTemplate);
			logger.info("API Unknown -> " + video.getType() + " " + video.toString());
		}
		return video;
	}

	private Map<String, Element> bytesToIdElementMap(byte[] xmlBytes) {

		var is = new ByteArrayInputStream(xmlBytes);
		Element root = null;
		try {
			root = new SAXBuilder().build(is).getRootElement();
		} catch (Exception e) {
			logger.info(e);
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
}
