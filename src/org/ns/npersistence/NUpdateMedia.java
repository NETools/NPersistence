package org.ns.npersistence;

import org.ns.npersistence.annotations.MediaAttribute;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class NUpdateMedia {
    private StringBuilder sqlQueryBuilder;

    private NEntityUpdateHandlerProperty entityUpdateHandlerProperty;

    public NEntityUpdateHandlerProperty getEntityUpdateHandlerProperty() {
        return entityUpdateHandlerProperty;
    }

    public String getSqlQuery() {
        return this.sqlQueryBuilder.toString();
    }

    public NUpdateMedia(NEntityUpdateHandlerProperty entityUpdateHandlerProperty) {
        this.sqlQueryBuilder = new StringBuilder();
        this.entityUpdateHandlerProperty = entityUpdateHandlerProperty;
    }

    public NUpdateMedia buildQuery(String columnName) {
        this.sqlQueryBuilder =
                this.sqlQueryBuilder
                        .append("UPDATE ")
                        .append(GeneralToolKit.sha1(this.getEntityUpdateHandlerProperty().getInstanceSqlName()))
                        .append(" SET ")
                        .append(columnName)
                        .append(" = ")
                        .append("?")
                        .append(" WHERE ")
                        .append(this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyName())
                        .append(" = ")
                        .append("?");

        return this;
    }

    public void write(Object data) throws SQLException, IOException {

        MediaAttribute.MediaType mediaType =
                (MediaAttribute.MediaType) AnnotationToolkit.getAnnotationValue(this.entityUpdateHandlerProperty.getFieldAnnotation(),
                        "mediaType");

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


        PreparedStatement preparedStatement =
                SQLManager.getDefault().getConnection().prepareStatement(this.getSqlQuery());
        preparedStatement.setObject(2, this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyValue());
        preparedStatement.setBytes(1, src);
        preparedStatement.executeUpdate();
    }


    public static NUpdateMedia getInstance(NEntityUpdateHandlerProperty entityUpdateHandlerProperty) {
        return new NUpdateMedia(entityUpdateHandlerProperty);
    }

}
