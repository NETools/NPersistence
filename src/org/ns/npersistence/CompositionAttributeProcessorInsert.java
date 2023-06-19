package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;
import org.ns.npersistence.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class CompositionAttributeProcessorInsert {
    /**
     * Prozessiert kompositionelles Attribute @InsertTable-Mode.
     * @param table
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableInsert table, Field field, Annotation annotation) throws Exception {
        Object currentValue = field.get(table.getCurrentObjectInstance());
        if (currentValue == null) return;
        SearchResult<Field> peerPrimaryKey = GeneralToolKit.isAnnotationPresent(currentValue.getClass(),
                PrimaryKey.class);
        if (!peerPrimaryKey.isPresent()) {
            NDebugOutputHandler.getDefault().handle(50);
            NPSPolicy.setErrorOccured(true);
            return;
        }

        NTableInsert nextTable = null;

        if (currentValue.getClass().isAnnotationPresent(CompositionTable.class))
            nextTable = new NCompositionTableInsert(table.getScheme(), currentValue, table, field);
        else nextTable = new NTableInsert(table.getScheme(), currentValue);
        nextTable.insert();


        table.setInsertQuery(table.getCurrentInsertQuery().values("fk_" + field.getName() + "_" + peerPrimaryKey.getField().getName()));
        Object primaryKeyValue = peerPrimaryKey.getField().get(nextTable.getCurrentObjectInstance());
        table.addObject(primaryKeyValue);


    }
}
