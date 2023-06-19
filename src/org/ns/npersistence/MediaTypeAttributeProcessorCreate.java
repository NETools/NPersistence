package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public class MediaTypeAttributeProcessorCreate {
    void process(NTableCreate table, Field field, Annotation annotation)  {
        NMediaTypeAttributeCreate attribute = new NMediaTypeAttributeCreate(table, table.getClassDefinition(), field, annotation);
        table.setCurrentTable(attribute.getTableDescription(table.getCurrentTable()));
    }
}
