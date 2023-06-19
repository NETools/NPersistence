package org.ns.npersistence;

import org.ns.npersistence.annotations.MediaAttribute;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.util.Base64;

public class MediaTypeSelectProcessor {
    /**
     * Prozessiert atomares Attribute @SelectTable-Mode.
     *
     * @param tableSelect
     * @param field
     * @param annotation
     * @throws Exception
     */
    void process(NTableSelect tableSelect, Field field, Annotation annotation) throws Exception {

        if (tableSelect.getResultSet() == null) return;

        byte[] blob = tableSelect.getResultSet().getBytes(field.getName());
        MediaAttribute.MediaType mediaType =
                (MediaAttribute.MediaType) AnnotationToolkit.getAnnotationValue(annotation,
                        "mediaType");

        switch (mediaType) {
            case Image:
                BufferedImage bufferedImage
                        = ImageIO.read(new ByteArrayInputStream(blob));
                field.set(tableSelect.getCurrentInstanceOfClass(), bufferedImage);
                break;
            case File:
                // QUELLE: https://stackoverflow.com/a/617438/14727115
                var file = File.createTempFile("temp", Long.toString(System.nanoTime()));

                FileOutputStream outputStream
                        = new FileOutputStream(file);
                outputStream.write(blob);
                outputStream.flush();
                outputStream.close();

                field.set(tableSelect.getCurrentInstanceOfClass(), file);

                break;
        }
    }
}
