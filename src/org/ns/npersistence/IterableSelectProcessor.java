package org.ns.npersistence;

import org.ns.npersistence.annotations.PrimaryKey;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.util.ArrayList;

class IterableSelectProcessor {

    /**
     * Prozessiert Listen-Objekt.
     *
     * @param tableSelect
     * @param field
     * @param annotation
     * @throws Exception
     */
    public void process(NTableSelect tableSelect, Field field, Annotation annotation) throws Exception {
        NJoinTableSelect joinTableSelect = new NJoinTableSelect(tableSelect, field, annotation);
        NPSFluentSelecting fluentSelecting = joinTableSelect.getQuery();


        if (fluentSelecting == null)
            return;

        ResultSet resultSet = SQLManager.getDefault().select(fluentSelecting.end());

        /* INITIALIZE ARRAYLIST */
        if (NPersistenceSelectPolicy.isDetached())
            field.set(tableSelect.getCurrentInstanceOfClass(),
                    field.getType().getDeclaredConstructor().newInstance());
        else field.set(tableSelect.getCurrentInstanceOfClass(),
                NArrayList.class.getDeclaredConstructor(Object.class, String.class)
                        .newInstance(tableSelect.getCurrentInstanceOfClass(), field.getName()));

        ArrayList<Object> arrayList =
                (ArrayList<Object>) (field.get(tableSelect.getCurrentInstanceOfClass()));
        ArrayList<Object> cachedPrimaryKeys
                = new ArrayList<>();

        while (resultSet.next()) {
            String propertyName = "";

            if (joinTableSelect.getProperty().isPrimitive())
                propertyName = "primitivedata_" + field.getName();
            else propertyName = "pk_" +
                    joinTableSelect.getProperty().getClassDefinition().getName().replace(".", "_") +
                    "_" + GeneralToolKit.isAnnotationPresent(joinTableSelect.getProperty().getClassDefinition(),
                    PrimaryKey.class).getField().getName();

            Object peerPrimaryKey = resultSet.getObject(propertyName);
            Object data = joinTableSelect.select(peerPrimaryKey);

            if (NPersistenceSelectPolicy.isDetached()) {
                arrayList.add(data);
            } else cachedPrimaryKeys.add(peerPrimaryKey);
        }

        resultSet.close();

        if (!NPersistenceSelectPolicy.isDetached()) {
            ((NArrayList<Object>) arrayList).setCachedPrimaryKeys(cachedPrimaryKeys);
            ((NArrayList<Object>) arrayList).setInstanceJoinTable(joinTableSelect);
            ((NArrayList<Object>) arrayList).setActivated(true);

        }
    }
}