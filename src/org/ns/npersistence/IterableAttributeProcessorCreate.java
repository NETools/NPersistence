package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;


class IterableAttributeProcessorCreate {
    /**
     * Prozessiert ein Listen-Objekt.
     * @param table
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableCreate table, Field field, Annotation annotation) throws Exception {
        NJoinTableCreate joinTable = new NJoinTableCreate(table, table.getClassDefinition(), field, annotation);
        joinTable.createJoinTable();

        table.setCurrentTable(joinTable.getTableDescription(table.getCurrentTable()));
    }
}
