package com.iskwhdys.project;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class Common {

	public static Date youtubeTimeToDate(String text) {

		String datetime = text.substring(0, 10) + " " + text.substring(11, 19);

		var cal = Calendar.getInstance();
		cal.setTime(toDate(datetime));
		cal.add(Calendar.HOUR, Constans.UTC_TIME);

		return cal.getTime();
	}

	public static Date toDate(String text) {

		var sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return sdf.parse(text);
		} catch (ParseException e) {
			System.out.println(e);
		}
		return null;
	}

	public static byte[] scaleImage(final byte[] src, int width, int height, final float quality) throws IOException {

		try (ByteArrayInputStream is = new ByteArrayInputStream(src);
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
			BufferedImage srcImage = ImageIO.read(is);
			BufferedImage destImage = resizeImage(srcImage, width, height);

			// 保存品質はユーザー指定に従う
			ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
			ImageWriteParam param = writer.getDefaultWriteParam();
			param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
			param.setCompressionQuality(quality);
			writer.setOutput(ios);
			writer.write(null, new IIOImage(destImage, null, null), param);
			writer.dispose();

			return os.toByteArray();
		}
	}

	private static BufferedImage resizeImage(final BufferedImage image, int width, int height) throws IOException {

		BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
		resizedImage.getGraphics().drawImage(
				image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING),
				0, 0, width, height, null);

		return resizedImage;
	}

}
