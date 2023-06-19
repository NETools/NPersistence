package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

class IterableAttributeProcessorInsert {

    /**
     * Prozessiert ein Listen-Objekt.
     * @param table
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableInsert table, Field field, Annotation annotation) throws Exception {
        NJoinTableInsert tableInsert = new NJoinTableInsert(table, field, annotation);
        IterableProperty iterableProperty = tableInsert.getProperty();

        Object iterableObject = field.get(table.getCurrentObjectInstance());

        if(iterableObject == null)
            return;

        switch (iterableProperty.getIterableType()) {
            case ARRAYLIST:
                ArrayList<?> arrayList = (ArrayList<?>) iterableObject;
                int index = 0;
                for (Object value : arrayList)
                    tableInsert.insert(value, GeneralToolKit.getDefaultRandomGenerator().nextInt(100000000));
                break;
        }

    }

}
