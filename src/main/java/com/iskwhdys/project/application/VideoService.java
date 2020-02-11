package com.iskwhdys.project.application;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iskwhdys.project.domain.channel.ChannelEntity;
import com.iskwhdys.project.domain.channel.ChannelRepository;
import com.iskwhdys.project.domain.video.VideoEntity;
import com.iskwhdys.project.domain.video.VideoFactory;
import com.iskwhdys.project.domain.video.VideoRepository;
import com.iskwhdys.project.domain.video.VideoSpecification;
import com.iskwhdys.project.infra.twitter.TwitterApi;
import com.iskwhdys.project.infra.youtube.ChannelFeedXml;

import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@Slf4j
public class VideoService {

	@Autowired
	ChannelRepository channelRepository;
	@Autowired
	VideoRepository videoRepository;
	@Autowired
	VideoThumbnailService videoThumbnailService;

	@Autowired
	VideoFactory videoFactory;

	@Autowired
	TwitterApi twitterApi;

	@Autowired
	VideoSpecification videoApi;

	public List<VideoEntity> update10min() {

		// 全チャンネルID取得
		List<String> channelIdList = channelRepository.findAll().stream().map(ChannelEntity::getId)
				.collect(Collectors.toList());
		// チャンネルのRssXmlから動画情報Elementを取得
		Map<String, Element> elements = ChannelFeedXml.getVideoElement(channelIdList);
		List<VideoEntity> videos = new ArrayList<>();

		// 全動画情報Elementを元にEntityを作成 or 情報の更新
		for (var set : elements.entrySet()) {
			String id = set.getKey();
			Element element = set.getValue();

			var video = videoRepository.findById(id).orElse(null);
			if (video == null) {
				video = createNewVideo(element);
			} else if (video.isUpload() || video.isPremierUpload()) {
				updateUploadVideo(element, video);
			} else if (video.isLiveArchive()) {
				updateLiveArchiveVideo(element, video);
			} else if (video.isPremierLive() || video.isLiveLive()) {
				updateLiveVideo(element, video);
			} else if (video.isPremierReserve() || video.isLiveReserve()) {
				updateReserveVideo(element, video);
			} else if (video.isUnknown()) {
				updateUnknownVideo(element, video);
			}
			videos.add(video);
		}

		var videoIds = videos.stream().map(VideoEntity::getId).collect(Collectors.toList());
		// XMLにないライブ情報の更新（非公開系？ライブ完了してホーム(XML)に公開されるまでの動画がここに来た）
		for (var video : videoRepository
				.findByTypeInAndEnabledTrueAndIdNotInOrderByLiveStartDesc(VideoEntity.TYPE_LIVES, videoIds)) {
			updateXmlNotExitVideo(video);
			videos.add(video);
		}
		// 24時間以内に公開された動画類でXMLに無いもの（ライブ終了直後でXMLに反映されてないもの）
		for (var video : videoRepository.findByIdNotInAndTodayUploadVideoAndArchives(videoIds)) {
			updatePrivateVideo(video);
			videos.add(video);
		}
		videoRepository.saveAll(videos);
		return videos;
	}

	private VideoEntity createNewVideo(Element element) {
		var video = videoFactory.createViaXmlElement(element);

		videoThumbnailService.downloadThumbnails(video);
		videoApi.updateEntity(video);
		if (video.isPremierLive() || video.isLiveLive()) {
			tweet(video);
		}
		log.info("API New -> " + video.getType() + " " + video.toString());

		return video;
	}

	private void updateUploadVideo(Element element, VideoEntity video) {
		videoFactory.updateViaXmlElement(element, video);

		if ((new Date().getTime() - video.getUploadDate().getTime()) < 1000 * 60 * 60 * 24) {
			// 公開して24時間以内の動画はサムネイルを更新する
			boolean success = videoThumbnailService.downloadThumbnails(video);
			// サムネ更新が出来ない == 動画が非公開
			video.setEnabled(success);
			log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
		}
	}

	private void updateLiveArchiveVideo(Element element, VideoEntity video) {

		videoFactory.updateViaXmlElement(element, video);

		if ((new Date().getTime() - video.getLiveStart().getTime()) < 1000 * 60 * 60 * 24) {
			// 配信して24時間以内の動画はサムネイルを更新する
			boolean success = videoThumbnailService.downloadThumbnails(video);
			// サムネ更新が出来ない == 動画が非公開
			video.setEnabled(success);
			log.debug("XML Thumbnail -> " + video.getType() + " " + video.toString());
		}
	}

	private void updateLiveVideo(Element element, VideoEntity video) {

		videoFactory.updateViaXmlElement(element, video);
		videoThumbnailService.downloadThumbnails(video);
		videoApi.updateLiveInfoViaApi(video);

		if (video.isPremierUpload() || video.isLiveArchive()) {
			videoApi.updateLiveToArchiveInfoViaApi(video);
		}
		log.info("API Live -> " + video.getType() + " " + video.toString());
	}

	private void updateReserveVideo(Element element, VideoEntity video) {

		videoFactory.updateViaXmlElement(element, video);
		boolean success = videoThumbnailService.downloadThumbnails(video);
		video.setEnabled(success);

		// 無効な動画は除外
		if (Boolean.FALSE.equals(video.getEnabled())) return;
		// 配信予定日時を過ぎてない動画 もしくは再生が0の動画は除外
		if (video.getLiveSchedule().after(new Date()) || video.getViews() == 0) return;
		// 配信予定日時が24時間を超えた動画は除外
		if ((new Date().getTime() - video.getLiveSchedule().getTime()) > 1000 * 60 * 60 * 24) return;

		videoApi.updateReserveInfoViaApi(video);
		if (video.isPremierUpload() || video.isLiveArchive()) {
			videoApi.updateLiveToArchiveInfoViaApi(video);
		}
		if (video.isPremierLive() || video.isLiveLive()) {
			tweet(video);
		}
		log.info("API Reserve -> " + video.getType() + " " + video.toString());
	}

	private void updateUnknownVideo(Element element, VideoEntity video) {
		videoFactory.updateViaXmlElement(element, video);
		videoThumbnailService.downloadThumbnails(video);
		videoApi.updateEntity(video);
		log.info("API Unknown -> " + video.getType() + " " + video.toString());
	}

	private void updateXmlNotExitVideo(VideoEntity video) {
		boolean success = videoThumbnailService.downloadThumbnails(video);
		video.setEnabled(success);
		if (success) {
			videoApi.updateEntity(video);
			log.info("API None ->" + video.getType() + " " + video.toString());
		} else {
			log.info("XML None ->" + video.getType() + " " + video.toString());
		}
	}

	private void updatePrivateVideo(VideoEntity video) {
		video.setEnabled(videoThumbnailService.downloadThumbnails(video));
		log.info("Private ->" + video.getType() + " " + video.toString());
	}

	private void tweet(VideoEntity video) {
		var result = new StringBuilder();
		result.append("～配信開始～\r\n");
		var channel = channelRepository.findById(video.getChannelId());
		if (channel.isPresent()) {
			result.append(channel.get().getTitle() + "\r\n");
		}
		result.append(video.getTitle() + "\r\n");
		result.append("https://www.youtube.com/watch?v=" + video.getId() + "\r\n");
		result.append("\r\n");
		result.append("～その他の配信情報はこちら～\r\n");
		result.append("にじさんじライブ新着 http://nijisanji-live.com");

		twitterApi.tweet(result.toString());
	}
}
