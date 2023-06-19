package org.ns.npersistence;

import org.ns.npersistence.annotations.MediaAttribute;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Blob;
import java.sql.ResultSet;

public class MediaTypeAttributeProcessorInsert {
    /**
     * Prozessiert atomares Attribute @InsertTable-Mode.
     *
     * @param table
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableInsert table, Field field, Annotation annotation) throws Exception {
        table.setInsertQuery(table.getCurrentInsertQuery().values(field.getName()));
        MediaAttribute.MediaType mediaType =
                (MediaAttribute.MediaType) AnnotationToolkit.getAnnotationValue(annotation,
                        "mediaType");

        Object data
                = field.get(table.getCurrentObjectInstance());


        byte[] src
                = new byte[0];

        if (data != null) {
            switch (mediaType) {
                case Image:
                    BufferedImage image = null;
                    if (data.getClass().equals(BufferedImage.class))
                        image = (BufferedImage) data;
                    src = MultiMediaToolkit.img2bytearray(image);
                    break;
                case File:
                    File file = (File) data;
                    FileInputStream fileInputStream
                            = new FileInputStream(file);
                    src = fileInputStream.readAllBytes();
                    break;
            }
        }

        table.addObject(src);

    }
}
