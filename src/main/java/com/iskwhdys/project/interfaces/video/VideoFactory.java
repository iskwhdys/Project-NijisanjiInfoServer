package com.iskwhdys.project.interfaces.video;

import org.jdom2.Element;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.domain.video.VideoEntity;

public class VideoFactory {

	public static VideoEntity createViaXmlElement(Element entry) {
		return updateViaXmlElement(entry, new VideoEntity(), true);

	}

	public static VideoEntity updateViaXmlElement(Element entry, VideoEntity entity, Boolean isUpdateThumbnail) {

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
				entity.setUploadDate(Common.youtubeTimeToDate(element.getValue().toString()));
				break;
			case "group":
				group = element;
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
				if (isUpdateThumbnail) {
					entity.setThumbnail(element.getAttributeValue("url"));
				}
				break;
			case "community":
				community = element;
				break;
			}
		}
		if (community == null)
			return entity;

		for (Element element : community.getChildren()) {
			switch (element.getName()) {
			case "starRating":
				int count = Integer.parseInt(element.getAttributeValue("count").toString());
				String ave = element.getAttributeValue("average").toString();
				int like = VideoSpecification.getLikeCount(count, ave);
				int dislike = count - like;
				entity.setLikes(like);
				entity.setDislikes(dislike);

				break;
			case "statistics":
				entity.setViews(Integer.parseInt(element.getAttributeValue("views").toString()));
				break;
			}
		}

		return entity;
	}

}
