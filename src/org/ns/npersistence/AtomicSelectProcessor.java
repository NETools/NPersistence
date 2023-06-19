package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class AtomicSelectProcessor {

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
        Object data = tableSelect.getResultSet().getObject(field.getName());

        if (field.getType().getName().equals(boolean.class.getName()) || field.getType().getName().equals(Boolean.class.getName()))
            data = (data.equals(1));

        field.set(tableSelect.getCurrentInstanceOfClass(), data);
    }
}
