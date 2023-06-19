package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;
import org.ns.npersistence.annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Klasse zur Selektion von Listenelementen aus einer Join-Tabelle.
 */
class NJoinTableSelect {

    private IterableProperty iterableProperty;
    private NTableSelect table;
    private Field field;
    private Annotation annotation;

    public NJoinTableSelect(NTableSelect table, Field field, Annotation annotation) {
        this.table = table;
        this.field = field;
        this.annotation = annotation;
    }

    public NTableSelect getTable() {
        return table;
    }

    public Field getField() {
        return field;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public IterableProperty getProperty() {
        if (iterableProperty == null) {

            IterableConstants iterableType =
                    (IterableConstants) AnnotationToolkit.getAnnotationValue(this.getAnnotation(), "mappingType");
            String addMethodName = (String) AnnotationToolkit.getAnnotationValue(this.getAnnotation(), "addMethodName");
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


    public Object select(Object primaryKey) throws Exception {
        Object data = null;

        if (this.getProperty().isPrimitive()) {
            data = primaryKey;
        } else {

            NTableSelect tableSelect = null;

            if (this.getProperty().getClassDefinition().isAnnotationPresent(CompositionTable.class))
                tableSelect = new NCompositionTableSelect(
                        this.getTable().getSchemeSelect(),
                        this.getProperty().getClassDefinition(),
                        this.getTable(),
                        this.getField());

            else if (this.getProperty().getClassDefinition().isAnnotationPresent(Table.class))
                tableSelect = new NTableSelect(this.table.getSchemeSelect(), this.getProperty().getClassDefinition());

            tableSelect.setPrimaryKey(primaryKey);
            tableSelect.select();

            data = tableSelect.getCurrentInstanceOfClass();
        }


        return data;
    }

    public NPSFluentSelecting getQuery() {
        if(this.getTable().getPrimaryKey() == null)
            return null;

        if(this.getProperty().isPrimitive())
            return NPSFluentSQLing.selectTable()
                    .begin().all()
                    .addTable("JOIN_TABLE_OF_" +
                            this.getTable().getTableSqlName().replace(".", "_") +
                            "_X_PRIMITIVE_" +
                            this.getProperty().getFieldName().replace(".", "_"))
                    .where("pk_" + this.getTable().getCurrentClassDefinition().getName().replace(".", "_")
                            + "_"
                            + this.getTable().getPrimaryKeyField().getField().getName(), this.getTable().getPrimaryKey());
        else return NPSFluentSQLing.selectTable()
                .begin().all()
                .addTable("JOIN_TABLE_OF_" +
                        this.getTable().getTableSqlName().replace(".", "_") +
                        "_X_NONPRIMITIVE_" +
                        this.getProperty().getFieldName().replace(".", "_"))
                .where("pk_" + this.getTable().getCurrentClassDefinition().getName().replace(".", "_")
                        + "_"
                        + this.getTable().getPrimaryKeyField().getField().getName(), this.getTable().getPrimaryKey());
    }

}

