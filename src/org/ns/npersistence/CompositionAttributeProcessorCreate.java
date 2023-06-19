package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class CompositionAttributeProcessorCreate {
    /**
     * Prozessiert kompositionelles Attribute @CreateTable-Mode.
     * @param table
     * @param field
     * @param annotation
     * @throws Exception
     */
    void process(NTableCreate table, Field field, Annotation annotation) throws Exception {

        NTableCreate nextTable = null;

        if (field.getType().isAnnotationPresent(CompositionTable.class))
            nextTable = new NCompositionTableCreate(table.getScheme(), field.getType().getName(), table, field);
        else nextTable = new NTableCreate(table.getScheme(), field.getType().getName());


        nextTable.create();

        NCompositionAttributeCreate compositionAttribute = new NCompositionAttributeCreate(table,
                table.getClassDefinition(), field, annotation);

        table.setCurrentTable(compositionAttribute.getTableDescription(table.getCurrentTable()));

    }

}









