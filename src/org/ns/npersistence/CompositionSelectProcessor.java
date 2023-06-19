package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class CompositionSelectProcessor {
    /**
     * Prozessiert kompositionelles Attribute @SelectTable-Mode.
     * @param tableSelect
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableSelect tableSelect, Field field, Annotation annotation) throws Exception {
        NTableSelect compositionTableSelect = null;

        if(tableSelect.getResultSet() == null) return;

        // field.getType().isAnnotationPresent(CompositionTable.class)

        if (field.getType().isAnnotationPresent(CompositionTable.class))
            compositionTableSelect = new NCompositionTableSelect(tableSelect.getSchemeSelect(), field.getType(),
                    tableSelect, field);
        else compositionTableSelect = new NTableSelect(tableSelect.getSchemeSelect(), field.getType());

        if(compositionTableSelect.getPrimaryKeyField().getField() == null){
            NDebugOutputHandler.getDefault().handle(50);
            return;
        }

        compositionTableSelect.setPrimaryKey(tableSelect.getResultSet()
                .getObject("fk_" + field.getName() + "_" + compositionTableSelect.getPrimaryKeyField().getField().getName()));
        compositionTableSelect.select();

        field.set(tableSelect.getCurrentInstanceOfClass(), compositionTableSelect.getCurrentInstanceOfClass());
    }

}
