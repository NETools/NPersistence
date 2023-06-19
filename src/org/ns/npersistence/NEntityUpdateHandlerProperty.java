package org.ns.npersistence;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * Klasse die Daten für veränderte Felder hält.
 */
class NEntityUpdateHandlerProperty {

    private Object currentInstance;
    private String sqlObjectClassName;

    private String instanceSqlName;
    private String instancePrimaryKeyName;
    private Object instancePrimaryKeyValue;

    private Field modifiedField;
    private String modifiedFieldName;

    private Annotation fieldAnnotation;

    private Object currentModifiedValue;
    private Object lastModifiedValue;

    private boolean isInvalid;


    public String getSqlObjectClassName() {
        return sqlObjectClassName;
    }

    public void setSqlObjectClassName(String sqlObjectClassName) {
        this.sqlObjectClassName = sqlObjectClassName;
    }

    public void setModifiedFieldName(String modifiedFieldName) {
        this.modifiedFieldName = modifiedFieldName;
    }

    public String getModifiedFieldName() {
        return modifiedFieldName;
    }

    public Object getCurrentInstance() {
        return currentInstance;
    }

    public void setCurrentInstance(Object currentInstance) {
        this.currentInstance = currentInstance;
    }

    public Object getCurrentModifiedValue() {
        return currentModifiedValue;
    }

    public void setCurrentModifiedValue(Object currentModifiedValue) {
        this.currentModifiedValue = currentModifiedValue;
    }

    public Annotation getFieldAnnotation() {
        return fieldAnnotation;
    }

    public void setFieldAnnotation(Annotation fieldAnnotation) {
        this.fieldAnnotation = fieldAnnotation;
    }

    public boolean isInvalid() {
        return isInvalid;
    }

    public void setInvalid(boolean invalid) {
        isInvalid = invalid;
    }

    public Field getModifiedField() {
        return modifiedField;
    }

    public void setModifiedField(Field modifiedField) {
        modifiedField.setAccessible(true);
        this.modifiedField = modifiedField;
    }

    public String getInstancePrimaryKeyName() {
        return instancePrimaryKeyName;
    }

    public void setInstancePrimaryKeyName(String instancePrimaryKeyName) {
        this.instancePrimaryKeyName = instancePrimaryKeyName;
    }

    public Object getInstancePrimaryKeyValue() {
        return instancePrimaryKeyValue;
    }

    public void setInstancePrimaryKeyValue(Object instancePrimaryKeyValue) {
        this.instancePrimaryKeyValue = instancePrimaryKeyValue;
    }

    public Object getLastModifiedValue() {
        return lastModifiedValue;
    }

    public void setLastModifiedValue(Object lastModifiedValue) {
        this.lastModifiedValue = lastModifiedValue;
    }

    public String getInstanceSqlName() {
        return instanceSqlName;
    }

    public void setInstanceSqlName(String instanceSqlName) {
        this.instanceSqlName = instanceSqlName;
    }



}
