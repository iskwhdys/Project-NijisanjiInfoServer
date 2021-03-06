package com.iskwhdys.project.infra.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

public class ImageEditor {

  private ImageEditor() {}

  public static byte[] trim(final byte[] src, int width, int height, final float quality)
      throws IOException {

    try (ByteArrayInputStream is = new ByteArrayInputStream(src);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
      BufferedImage srcImage = ImageIO.read(is);
      BufferedImage destImage = trimCenter(srcImage, width, height);

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

  public static byte[] resize(final byte[] src, int width, int height, final float quality)
      throws IOException {

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

  private static BufferedImage resizeImage(final BufferedImage image, int width, int height) {

    BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
    resizedImage
        .getGraphics()
        .drawImage(
            image.getScaledInstance(width, height, java.awt.Image.SCALE_AREA_AVERAGING),
            0,
            0,
            width,
            height,
            null);

    return resizedImage;
  }

  private static BufferedImage trimCenter(final BufferedImage image, int width, int height) {

    BufferedImage resizedImage = new BufferedImage(width, height, image.getType());
    var img = image.getSubimage(0, (image.getHeight() - height) / 2, width, height);

    resizedImage.getGraphics().drawImage(img, 0, 0, width, height, null);

    return resizedImage;
  }
}
