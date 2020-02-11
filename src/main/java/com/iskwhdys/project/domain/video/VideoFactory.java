package com.iskwhdys.project.domain.video;

import java.util.Date;

import org.jdom2.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.infra.youtube.YoutubeApi;

@Component
public class VideoFactory {

	@Autowired
	YoutubeApi youtubeApi;

	public VideoEntity createViaXmlElement(Element entry) {
		var video = updateViaXmlElement(entry, new VideoEntity());
		video.setCreateDate(new Date());
		return video;
	}

	public VideoEntity updateViaXmlElement(Element entry, VideoEntity entity) {
		entity.setUpdateDate(new Date());

		Element group = null;
		for (Element element : entry.getChildren()) {
			switch (element.getName()) {
			case "videoId":
				entity.setId(element.getValue());
				break;
			case "channelId":
				entity.setChannelId(element.getValue());
				break;
			case "title":
				entity.setTitle(element.getValue());
				break;
			case "published":
				entity.setUploadDate(Common.youtubeTimeToDate(element.getValue()));
				break;
			case "group":
				group = element;
				break;
			default:
				break;
			}
		}
		if (group == null)
			return entity;

		Element community = null;
		for (Element element : group.getChildren()) {
			switch (element.getName()) {
			case "description":
				entity.setDescription(element.getValue());
				break;
			case "thumbnail":
				entity.setThumbnailUrl(element.getAttributeValue("url"));
				break;
			case "community":
				community = element;
				break;
			default:
				break;
			}
		}
		if (community == null)
			return entity;

		for (Element element : community.getChildren()) {
			switch (element.getName()) {
			case "starRating":
				int count = Integer.parseInt(element.getAttributeValue("count"));
				String ave = element.getAttributeValue("average");
				int like = youtubeApi.getLikeCount(count, ave);
				int dislike = count - like;
				entity.setLikes(like);
				entity.setDislikes(dislike);
				break;
			case "statistics":
				entity.setViews(Integer.parseInt(element.getAttributeValue("views")));
				break;
			default:
				break;
			}
		}

		return entity;
	}

}
