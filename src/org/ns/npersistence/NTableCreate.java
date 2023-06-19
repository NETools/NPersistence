package org.ns.npersistence;

import org.ns.npersistence.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

class NTableCreate {
    private NSchemeCreate scheme;
    private Class<?> classDefinition;

    private String sqlName;

    private NPSFluentTabling currentTable;
    private NTableProperty tableProperty;

    public NSchemeCreate getScheme() {
        return this.scheme;
    }

    public Class<?> getClassDefinition() {
        return this.classDefinition;
    }

    public String getSqlName() {
        return this.sqlName;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public NPSFluentTabling getCurrentTable() {
        return this.currentTable;
    }

    public void setCurrentTable(NPSFluentTabling currentTable) {
        this.currentTable = currentTable;
    }

    public NTableProperty getTableProperty() {
        return tableProperty;
    }

    public void setTableProperty(NTableProperty tableProperty) {
        this.tableProperty = tableProperty;
    }

    public NTableCreate(NSchemeCreate scheme, String className) throws ClassNotFoundException {
        this.scheme = scheme;

        this.classDefinition = Class.forName(className);

        this.currentTable = NPSFluentSQLing.createNewTable();
        this.tableProperty = new NTableProperty();
        this.tableProperty.setKeySearchResults(new SearchResult<Field>());

        this.sqlName = this.getClassDefinition().getName().replace(".", "_");
    }

    public void create() throws Exception {
        currentTable = currentTable.begin().ifNotExists().callIt(this.getSqlName());

        TableProcessorsCreate.getTableSignatureProcessor().signClass(this);


        this.process(this.getClassDefinition().getDeclaredFields());

        if (NPSPolicy.hasErrorOccured()) {
            NDebugOutputHandler.getDefault().handle(52);
            return;
        }

        if (!this.getTableProperty().isInjected())
            this.getScheme().addModifiedClass(this.getClassDefinition());

        this.getScheme().addSqlQuery(currentTable.end());
    }

    private void process(Field[] fields) throws Exception {
        for (Field field : fields) {
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {

                /* ATOMIC ATTRIBUTES */
                if (annotation.annotationType().equals(AtomicAttribute.class)) {
                    TableProcessorsCreate.getAtomicAttributeProcessor().process(this, field, annotation);
                }
                /* PRIMARY KEY */
                else if (annotation.annotationType().equals(PrimaryKey.class)) {
                    TableProcessorsCreate.getPrimaryKeyAttributeProcessor().process(this, field, annotation);
                    SearchResult<Field> primaryKeyField = new SearchResult<>();
                    primaryKeyField.setPresent(true);
                    primaryKeyField.setField(field);
                }
                /* COMPOSITION */
                else if (annotation.annotationType().equals(CompositeAttribute.class)) {
                    TableProcessorsCreate.getCompositionAttributeProcessor().process(this, field, annotation);
                }
                /* MANY TO MANY */
                else if (annotation.annotationType().equals(IterableAttribute.class)) {
                    TableProcessorsCreate.getIterableAttributeProcessor().process(this, field, annotation);
                } else if (annotation.annotationType().equals(MediaAttribute.class)) {
                    TableProcessorsCreate.getMediaTypeAttributeProcessorCreate().process(this, field, annotation);
                }
            }
        }
    }

}
