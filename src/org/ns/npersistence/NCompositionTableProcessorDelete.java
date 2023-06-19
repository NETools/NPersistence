package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class NCompositionTableProcessorDelete {

    public void process(NTableDelete tableDelete, Field field, Annotation annotation) throws Exception {
        NCompositionTableDelete compositionTableDelete = new NCompositionTableDelete(field.get(tableDelete.getCurrentInstanceDlt()));
        compositionTableDelete.loadPrimaryKey();
        compositionTableDelete.delete();
    }

    public static NCompositionTableProcessorDelete getInstance() throws Exception {
        return new NCompositionTableProcessorDelete();
    }

}
