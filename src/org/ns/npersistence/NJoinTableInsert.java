package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;
import org.ns.npersistence.annotations.PrimaryKey;
import org.ns.npersistence.annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Klasse um neue Join-Tabellen-Werte hinzuzufügen.
 */
class NJoinTableInsert {

    private IterableProperty iterableProperty;
    private NTableInsert table;
    private Field field;
    private Annotation annotation;

    private SearchResult<Field> primaryKeySearch;
    private Object currentObjectInstance;

    private NPSFluentInserting currentInsertQuery;

    public NJoinTableInsert(NTableInsert table, Field field, Annotation annotation) {
        this.table = table;
        this.field = field;
        this.annotation = annotation;
    }

    public NTableInsert getTable() {
        return table;
    }

    public Field getField() {
        return field;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public SearchResult<Field> getPrimaryKeySearch() {
        return primaryKeySearch;
    }

    public void setPrimaryKeySearch(SearchResult<Field> primaryKeySearch) {
        this.primaryKeySearch = primaryKeySearch;
    }

    public Object getCurrentObjectInstance() {
        return currentObjectInstance;
    }

    public void setCurrentObjectInstance(Object currentObjectInstance) {
        this.currentObjectInstance = currentObjectInstance;
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

    public void setCurrentInsertQuery(NPSFluentInserting currentInsertQuery) {
        this.currentInsertQuery = currentInsertQuery;
    }

    public NPSFluentInserting getCurrentInsertQuery() {
        return currentInsertQuery;
    }

    public void insert(Object object, int index) throws Exception {

        if (primaryKeySearch == null) {
            primaryKeySearch = GeneralToolKit.isAnnotationPresent(this.getTable().getCurrentObjectClass(),
                    PrimaryKey.class);

        }

        if (primaryKeySearch.getField() == null) {
            NDebugOutputHandler.getDefault().handle(50);
            NPSPolicy.setErrorOccured(true);
            return;
        }

        String primaryKeySignature = primaryKeySearch.getField().getType().getName();
        String sqlColumnTypeA = NPDataType.getSQLiteType(primaryKeySignature);

        String primaryKeyName = "";

        if (this.getCurrentObjectInstance() == null)
            primaryKeyName = "pk_" + this.getTable().getCurrentObjectClass()
                    .getName().replace(".", "_") + "_" +
                    primaryKeySearch.getField().getName();
        else
            primaryKeyName = "pk_" + this.getCurrentObjectInstance().getClass().getSuperclass()
                    .getName().replace(".", "_") + "_" +
                    primaryKeySearch.getField().getName();


        this.currentInsertQuery = NPSFluentSQLing.insertInto().begin();

        String sqlColumnTypeB = "";
        Object valueB = object;

        if (this.getProperty().isPrimitive()) {
            sqlColumnTypeB = NPDataType.getSQLiteType(iterableProperty.getType());

            currentInsertQuery =
                    currentInsertQuery.toInsertIn("JOIN_TABLE_OF_" +
                            this.getTable()
                                    .getSqlName()
                                    .replace(".", "_") + "_X_PRIMITIVE_" + this
                            .getProperty().getFieldName()
                            .replace(".", "_"))
                            .values(primaryKeyName).values("primitivedata_" + this.getField().getName()).values("list_index");

        } else {

            Class<?> peerClass = object.getClass();
            SearchResult<Field> primaryKeyPeerSearch = GeneralToolKit.isAnnotationPresent(peerClass,
                    PrimaryKey.class);

            if (primaryKeyPeerSearch.getField() == null) {
                NDebugOutputHandler.getDefault().handle(50);
                return;
            }

            NTableInsert nextTable = null;

            if (object.getClass().isAnnotationPresent(CompositionTable.class))
                nextTable = new NCompositionTableInsert(table.getScheme(), object, table, field);
            else if (object.getClass().isAnnotationPresent(Table.class))
                nextTable = new NTableInsert(table.getScheme(), object);

            nextTable.insert();

            sqlColumnTypeB = NPDataType.getSQLiteType(primaryKeyPeerSearch.getField().getType().getName());

            currentInsertQuery =
                    currentInsertQuery
                            .toInsertIn("JOIN_TABLE_OF_" + this.getTable()
                                    .getSqlName().replace(".", "_") + "_X_NONPRIMITIVE_" +
                                    iterableProperty.getFieldName().replace(".", "_"))
                            .values(primaryKeyName)
                            .values("pk_" + peerClass.getName().replace(".", "_") + "_" +
                                    primaryKeyPeerSearch.getField().getName()).values("list_index");

            valueB = primaryKeyPeerSearch.getField().get(object);

        }


        NPayload payload = new NPayload(this.getCurrentInsertQuery().end());

        if (this.getCurrentObjectInstance() == null)
            payload.addData(primaryKeySearch.getField().get(this.getTable().getCurrentObjectInstance()));
        else
            payload.addData(primaryKeySearch.getField().get(this.getCurrentObjectInstance()));

        payload.addData(valueB);
        payload.addData(index);

        this.getTable().getScheme().addPayload(payload);
    }

}
