package io.github.glandais.trouvaille.service;

import jakarta.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;

@ApplicationScoped
public class ImageService {

  public byte[] resizeImage(
      InputStream inputStream, int targetWidth, int targetHeight, String format)
      throws IOException {
    BufferedImage originalImage = ImageIO.read(inputStream);

    if (originalImage == null) {
      throw new IOException("Unable to read image data");
    }

    int originalWidth = originalImage.getWidth();
    int originalHeight = originalImage.getHeight();

    // Calculate dimensions maintaining aspect ratio
    double aspectRatio = (double) originalWidth / originalHeight;
    int newWidth, newHeight;

    if (aspectRatio > 1) {
      // Image is wider than tall
      newWidth = Math.min(targetWidth, originalWidth);
      newHeight = (int) (newWidth / aspectRatio);
    } else {
      // Image is taller than wide or square
      newHeight = Math.min(targetHeight, originalHeight);
      newWidth = (int) (newHeight * aspectRatio);
    }

    // Don't upscale images
    if (newWidth > originalWidth || newHeight > originalHeight) {
      newWidth = originalWidth;
      newHeight = originalHeight;
    }

    BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = resizedImage.createGraphics();

    // High quality rendering
    g2d.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g2d.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
    g2d.dispose();

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    ImageIO.write(resizedImage, format, outputStream);

    return outputStream.toByteArray();
  }
}
