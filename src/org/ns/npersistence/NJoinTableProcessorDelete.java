package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;


class NJoinTableProcessorDelete {

    /**
     * Prozessiert ein Join-Table-Delete-Vorgang.
     * @param tableDelete
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableDelete tableDelete, Field field, Annotation annotation) throws Exception {
        NJoinTableDelete joinTableDelete = new NJoinTableDelete(tableDelete, field, annotation);
        joinTableDelete.delete();

        ArrayList<?> iterableAttribute = (ArrayList<?>) field.get(tableDelete.getCurrentInstanceDlt());

        if (iterableAttribute == null) return;

        for (Object data : iterableAttribute) {
            if (data.getClass().getSuperclass().isAnnotationPresent(CompositionTable.class)) {
                NCompositionTableDelete compositionTableDelete =
                        new NCompositionTableDelete(data);
                compositionTableDelete.loadPrimaryKey();
                compositionTableDelete.delete();
            }
        }

    }

    public static NJoinTableProcessorDelete getInstance() throws Exception {
        return new NJoinTableProcessorDelete();
    }

}
