package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;
import org.ns.npersistence.annotations.Table;

/**
 * Allgemeines Schema für verkettete SELECT-Anweisung um Objekte zu laden.
 */
class NSchemeSelect {

    public Object get(Class<?> classDefinition, Object primaryKey) throws Exception {
        NTableSelect tableSelect = null;

        if (classDefinition.isAnnotationPresent(Table.class)) {
            tableSelect = new NTableSelect(this, classDefinition);
            tableSelect.setPrimaryKey(primaryKey);
            tableSelect.select();

        }

        return tableSelect.getCurrentInstanceOfClass();
    }

    public Object getLater(Class<?> classDefinition, String sqlRoot, Object primaryKey) throws Exception {

        NTableSelect tableSelect = null;

        if (classDefinition.isAnnotationPresent(Table.class)) {
            tableSelect = new NTableSelect(this, classDefinition);
            tableSelect.setPrimaryKey(primaryKey);
            tableSelect.select();

        } else if (classDefinition.isAnnotationPresent(CompositionTable.class)) {
            tableSelect = new NTableSelect(this, classDefinition, sqlRoot);
            tableSelect.setPrimaryKey(primaryKey);
            tableSelect.select();
        }

        return tableSelect.getCurrentInstanceOfClass();
    }
}



