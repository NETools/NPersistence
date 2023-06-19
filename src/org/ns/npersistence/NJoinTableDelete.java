package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Klasse zur Löschung von Join-Table-Einträgen.
 */
class NJoinTableDelete {

    private NTableDelete tableDelete;
    private Field field;
    private Annotation annotation;

    private IterableProperty iterableProperty;

    public NTableDelete getTableDelete() {
        return tableDelete;
    }

    public Field getField() {
        return field;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public NJoinTableDelete(NTableDelete tableDelete, Field field, Annotation annotation) {
        this.tableDelete = tableDelete;
        this.field = field;
        this.annotation = annotation;
    }

    public IterableProperty getProperty() {
        if (iterableProperty == null) {

            IterableConstants iterableType =
                    (IterableConstants) AnnotationToolkit.getAnnotationValue(this.getAnnotation(),
                            "mappingType");
            String addMethodName = (String) AnnotationToolkit.getAnnotationValue(this.getAnnotation(),
                    "addMethodName");
            String removeMethodName = (String) AnnotationToolkit.getAnnotationValue(this.getAnnotation(),
                    "removeMethodName");

            String fieldSignature =
                    this.getField().getGenericType().getTypeName().substring(this.getField().getGenericType().getTypeName().indexOf("<") + 1).replace(">", "");

            iterableProperty = new IterableProperty();

            iterableProperty.setFieldName(this.getField().getName());
            iterableProperty.setIterableType(iterableType);
            iterableProperty.setAddMethod(addMethodName);
            iterableProperty.setRemoveMethod(removeMethodName);
            iterableProperty.setPrimitive(NPDataType.isWrapperType(fieldSignature));
            iterableProperty.setType(fieldSignature);

        }

        return iterableProperty;
    }

    public void delete() throws SQLException {
        if (this.getTableDelete().getCurrentInstancePrimaryKey() == null) return;

        String sqlPrimaryKeyInstance =
                "pk_" + this.getTableDelete().getCurrentInstanceClassDlt().getName().replace(
                ".", "_") + "_" + this.getTableDelete().getCurrentInstancePrimaryKey().getField().getName();


        String joinTableName = "";

        if (this.getProperty().isPrimitive()) {
            joinTableName = "JOIN_TABLE_OF_" + this.getTableDelete().getCurrentInstanceSqlName() +
                    "_X_PRIMITIVE_";
        } else {
            joinTableName = "JOIN_TABLE_OF_" + this.getTableDelete().getCurrentInstanceSqlName() +
                    "_X_NONPRIMITIVE_";
        }
        joinTableName += this.getField().getName();
        NDebugOutputHandler.getDefault().handle(80, "[DELETING] " + GeneralToolKit.sha1(joinTableName));

        PreparedStatement preparedStatement = SQLManager.getDefault().getConnection()
                .prepareStatement("DELETE FROM " + GeneralToolKit.sha1(joinTableName) + " WHERE " + sqlPrimaryKeyInstance + " = " +
                        "?");

        preparedStatement.setObject(1, this.getTableDelete().getCurrentInstancePrimaryKeyValue());
        preparedStatement.executeUpdate();


    }

}









