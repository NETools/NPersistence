package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositeAttribute;
import org.ns.npersistence.annotations.IterableAttribute;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;

class NTableDelete {

    private Object currentInstanceDlt;
    private String currentInstanceSqlName;

    private String currentInstancePrimaryKeyName;
    private Object currentInstancePrimaryKeyValue;

    private SearchResult<Field> currentInstancePrimaryKey;

    public Object getCurrentInstanceDlt() {
        return currentInstanceDlt;
    }

    public Class<?> getCurrentInstanceClassDlt() {
        return this.getCurrentInstanceDlt().getClass().getSuperclass();
    }

    public String getCurrentInstanceSqlName() {
        return currentInstanceSqlName;
    }

    public String getCurrentInstancePrimaryKeyName() {
        return currentInstancePrimaryKeyName;
    }

    public Object getCurrentInstancePrimaryKeyValue() {
        return currentInstancePrimaryKeyValue;
    }

    public void setCurrentInstancePrimaryKey(SearchResult<Field> currentInstancePrimaryKey) {
        this.currentInstancePrimaryKey = currentInstancePrimaryKey;
    }

    public SearchResult<Field> getCurrentInstancePrimaryKey() {
        return currentInstancePrimaryKey;
    }


    public NTableDelete(Object data) throws NoSuchFieldException, IllegalAccessException {
        if (data == null) return;

        this.currentInstanceDlt = data;
        this.currentInstanceSqlName =
                GeneralToolKit.getFieldValue(this.getCurrentInstanceDlt(), "sqlQualifiedName") + "";
        this.currentInstancePrimaryKeyName = GeneralToolKit.getFieldValue(this.getCurrentInstanceDlt(),
                "sqlPrimaryKeyName") + "";
        this.currentInstancePrimaryKeyValue = GeneralToolKit.getFieldValue(this.getCurrentInstanceDlt(),
                "sqlPrimaryKeyValue");
    }

    public void loadPrimaryKey() throws Exception {
        if (this.getCurrentInstanceDlt() == null) return;
        NPrimaryKeyProcessorDelete.getInstance().process(this);
    }


    public void delete() throws Exception {

        if (this.getCurrentInstanceDlt() == null) return;
        NDebugOutputHandler.getDefault().handle(80, "[DELETING] " + GeneralToolKit.sha1(this.getCurrentInstanceSqlName()));
        this.process(this.getCurrentInstanceDlt().getClass().getSuperclass().getDeclaredFields());


    }

    public void process(Field[] fields) throws Exception {
        for (Field field : fields) {
            field.setAccessible(true);
            Annotation[] annotations = field.getDeclaredAnnotations();
            for (Annotation annotation : annotations) {
                if (annotation.annotationType().getName().equals(CompositeAttribute.class.getName())) {
                    NCompositionTableProcessorDelete.getInstance().process(this, field, annotation);
                } else if (annotation.annotationType().getName().equals(IterableAttribute.class.getName())) {
                    NJoinTableProcessorDelete.getInstance().process(this, field, annotation);
                }


            }
        }

        try {
            PreparedStatement preparedStatement =
                    SQLManager.getDefault().getConnection().prepareStatement("DELETE " + "FROM " +
                            GeneralToolKit.sha1(this.getCurrentInstanceSqlName()) + " WHERE " + this.getCurrentInstancePrimaryKeyName() + " " + "= ?");
            preparedStatement.setObject(1, this.getCurrentInstancePrimaryKeyValue());
            preparedStatement.executeUpdate();
        } catch (Exception ex) {

        }
    }


}
