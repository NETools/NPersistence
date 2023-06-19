package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Erstellt eine neue Spalte die als Primärschlüssel ausgezeichnet ist-
 */
class NPrimaryKeyCreate extends NAttributeCreate {

    public NPrimaryKeyCreate(NTableCreate table, Class<?> ctClass, Field field, Annotation annotation) {
        super(table, ctClass, field, annotation);
        // TODO Auto-generated constructor stub
    }

    @Override
    public NPSFluentTabling getTableDescription(NPSFluentTabling current) {
        String columnName = getField().getName();
        String dataType = NPDataType.getSQLiteType(getField().getType().getName());

        switch (NEntityUpdateHandler.getDefault().getCurrentSession().getCurrentLoadedDriver()) {
            case MySQL:
                if (dataType.equals("TEXT"))
                    dataType = "VARCHAR(256)";
                break;
        }

        return current.addColumn("pk_" + columnName, dataType, "PRIMARY KEY");
    }

}
