package org.ns.npersistence;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class MultiMediaToolkit {
    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     * Source: https://stackoverflow.com/a/13605411/14727115
     */
    public static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        // Create a buffered image with transparency
        BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null),
                BufferedImage.TYPE_INT_ARGB);

        // Draw the image on to the buffered image
        Graphics2D bGr = bimage.createGraphics();
        bGr.drawImage(img, 0, 0, null);
        bGr.dispose();

        // Return the buffered image
        return bimage;
    }

    public static String img2base64(BufferedImage image) {
        ByteArrayOutputStream outputStream
                = new ByteArrayOutputStream();

        String result
                = "";
        try {
            ImageIO.write(image, "png", outputStream);
            result = Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static byte[] img2bytearray(BufferedImage image) throws IOException {
        ByteArrayOutputStream outputStream
                = new ByteArrayOutputStream();

        ImageIO.write(image, "png", outputStream);
        return outputStream.toByteArray();
    }

}
