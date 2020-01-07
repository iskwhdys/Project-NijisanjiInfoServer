package com.iskwhdys.project.interfaces.video;

import org.jdom2.Element;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.domain.video.VideoEntity;

public class VideoFactory {

	public static VideoEntity createViaXmlElement(Element entry) {

		var entity = new VideoEntity();
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
		for (Element element : group.getChildren()) {
			switch (element.getName()) {
			case "description":
				entity.setDescription(element.getValue());
				break;
			case "thumbnail":
				entity.setThumbnail(element.getAttributeValue("url"));
				break;
			}
		}

		return entity;
	}

}
