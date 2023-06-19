package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class AtomicAttributeProcessorInsert {

    /**
     * Prozessiert atomares Attribute @InsertTable-Mode.
     * @param table
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableInsert table, Field field, Annotation annotation) throws Exception {
        table.setInsertQuery(table.getCurrentInsertQuery().values(field.getName()));
        table.addObject(field.get(table.getCurrentObjectInstance()));
    }
}






