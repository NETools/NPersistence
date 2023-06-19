package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;
import org.ns.npersistence.annotations.Table;

import java.sql.PreparedStatement;

/**
 * Klasse zur Löschung von Listen-Objekten aus der Datenbank
 */
class NIterableElementDelete {
    private StringBuilder sqlQueryBuilder;

    private NEntityUpdateHandlerProperty entityUpdateHandlerProperty;

    private IterableProperty iterableProperty;

    public NEntityUpdateHandlerProperty getEntityUpdateHandlerProperty() {
        return entityUpdateHandlerProperty;
    }

    public String getSqlQuery() {
        return this.sqlQueryBuilder.toString();
    }

    public NIterableElementDelete(NEntityUpdateHandlerProperty entityUpdateHandlerProperty) {
        this.sqlQueryBuilder = new StringBuilder();
        this.entityUpdateHandlerProperty = entityUpdateHandlerProperty;
    }

    public IterableProperty getProperty() {
        if (iterableProperty == null) {
            this.iterableProperty = new IterableProperty();

            IterableConstants iterableType =
                    (IterableConstants) AnnotationToolkit.getAnnotationValue(this.entityUpdateHandlerProperty.getFieldAnnotation(), "mappingType");
            String addMethodName =
                    (String) AnnotationToolkit.getAnnotationValue(this.entityUpdateHandlerProperty.getFieldAnnotation(), "addMethodName");
            String removeMethodName =
                    (String) AnnotationToolkit.getAnnotationValue(this.entityUpdateHandlerProperty.getFieldAnnotation(), "removeMethodName");

            String fieldSignature =
                    this.entityUpdateHandlerProperty.getModifiedField().getGenericType().getTypeName().substring(this.entityUpdateHandlerProperty.getModifiedField().getGenericType().getTypeName().indexOf("<") + 1).replace(">", "");


            iterableProperty.setFieldName(this.entityUpdateHandlerProperty.getModifiedField().getName());
            iterableProperty.setIterableType(iterableType);
            iterableProperty.setAddMethod(addMethodName);
            iterableProperty.setRemoveMethod(removeMethodName);
            iterableProperty.setPrimitive(NPDataType.isWrapperType(fieldSignature));
            iterableProperty.setType(fieldSignature);

        }

        return iterableProperty;
    }


    public void delete(Object data) throws Exception {

        String primaryKeyName =
                this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyName().replace(this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyName(), "pk_" + this.getEntityUpdateHandlerProperty().getCurrentInstance().getClass().getSuperclass().getName().replace(".", "_") + "_" + this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyName().substring(3));

        String peerPrimaryKeyName = "";

        String joinTableName = "";
        NTableDelete tableDelete = null;

        if (this.getProperty().isPrimitive()) {
            joinTableName = "JOIN_TABLE_OF_" + this.getEntityUpdateHandlerProperty().getInstanceSqlName() +
                    "_X_PRIMITIVE_";

            peerPrimaryKeyName = "primitivedata_" + this.getEntityUpdateHandlerProperty().getModifiedFieldName();

        } else {
            joinTableName = "JOIN_TABLE_OF_" + this.getEntityUpdateHandlerProperty().getInstanceSqlName() +
                    "_X_NONPRIMITIVE_";

            if (data.getClass().getSuperclass().isAnnotationPresent(CompositionTable.class)) {
                tableDelete = new NCompositionTableDelete(data);
                tableDelete.loadPrimaryKey();
                tableDelete.delete();
            } else if (data.getClass().getSuperclass().isAnnotationPresent(Table.class)) {
                tableDelete = new NTableDelete(data);
                tableDelete.loadPrimaryKey();
                if (NEntityUpdateHandlerPolicy.doForceDeleteTable())
                    tableDelete.delete();
            }

            peerPrimaryKeyName = "pk_" + tableDelete.getCurrentInstanceClassDlt().getName().replace(".", "_") + "_" +
                    tableDelete.getCurrentInstancePrimaryKey().getField().getName();

        }

        joinTableName += this.getEntityUpdateHandlerProperty().getModifiedFieldName();

        joinTableName = GeneralToolKit.sha1(joinTableName);

        int listIndex = -1;

        PreparedStatement preparedStatement =
                SQLManager.getDefault().getConnection()
                        .prepareStatement("SELECT list_index FROM " + joinTableName + " WHERE " + primaryKeyName + " = ? "
                                + "AND " + peerPrimaryKeyName + " = ?");

        preparedStatement.setObject(1, this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyValue());
        if (this.getProperty().isPrimitive())
            preparedStatement.setObject(2, data);
        else preparedStatement.setObject(2, tableDelete.getCurrentInstancePrimaryKeyValue());

        preparedStatement.execute();
        var rs = preparedStatement.getResultSet();

        switch (NEntityUpdateHandler.getDefault().getCurrentSession().getCurrentLoadedDriver()) {
            case MySQL:
                rs.next();
                break;
        }

        listIndex = rs.getInt("list_index");

        preparedStatement.close();
        preparedStatement =
                SQLManager.getDefault().getConnection()
                        .prepareStatement("DELETE FROM " + joinTableName + " WHERE list_index = ?");
        preparedStatement.setObject(1, listIndex);
        preparedStatement.executeUpdate();
    }


    public static NIterableElementDelete getInstance(NEntityUpdateHandlerProperty entityUpdateHandlerProperty) {
        return new NIterableElementDelete(entityUpdateHandlerProperty);
    }


}
