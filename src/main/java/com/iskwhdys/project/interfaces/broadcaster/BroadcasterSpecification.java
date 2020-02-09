package com.iskwhdys.project.interfaces.broadcaster;

import java.util.Base64;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.client.RestTemplate;

import com.iskwhdys.project.Common;
import com.iskwhdys.project.Constans;
import com.iskwhdys.project.domain.broadcaster.BroadcasterEntity;

public class BroadcasterSpecification {

	private static Logger logger = LogManager.getLogger(BroadcasterSpecification.class);

	public static Boolean setIcon(BroadcasterEntity entity, RestTemplate restTemplate) {

		try {
			byte[] buf = restTemplate.getForObject(entity.getIconUrl(), byte[].class);
			String base64 = Base64.getEncoder().encodeToString(buf);
			entity.setIcon(Constans.BASE64_HEADER_IMAGE_PNG + base64);

			buf = Common.scaleImagePng(buf, 64, 64, 1.0f);
			base64 = Base64.getEncoder().encodeToString(buf);
			entity.setIconMini(Constans.BASE64_HEADER_IMAGE_PNG + base64);

		} catch (Exception e) {

			logger.info(e);
			logger.info(entity.getIconUrl());
			return false;
		}
		return true;
	}
}
