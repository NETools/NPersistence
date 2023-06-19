package org.ns.npersistence;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Loader;
import javassist.NotFoundException;

/**
 * Klasse die wichtige Daten für iterable-Felder bereithält
 */
class IterableProperty {

    private Class<?> classDefinition;
    private CtClass ctClass;

    private String fieldName;
    private IterableConstants iterableType;

    private String addMethod;
    private String removeMethod;

    private boolean primitive;

    private String type;

    public IterableConstants getIterableType() {
        return iterableType;
    }

    public void setIterableType(IterableConstants iterableType) {
        this.iterableType = iterableType;
    }

    public String getAddMethod() {
        return addMethod;
    }

    public void setAddMethod(String addMethod) {
        this.addMethod = addMethod;
    }

    public String getRemoveMethod() {
        return removeMethod;
    }

    public void setRemoveMethod(String removeMethod) {
        this.removeMethod = removeMethod;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public void setPrimitive(boolean primitive) {
        this.primitive = primitive;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public CtClass getClassDefinition(ClassPool pool) throws NotFoundException {

        if (ctClass == null) {
            ctClass = pool.get(this.getType());
            ctClass.defrost();
        }

        return ctClass;
    }

    public Class<?> getClassDefinition(Loader loader) throws ClassNotFoundException {
        if (classDefinition == null) classDefinition = loader.loadClass(this.getType());
        return classDefinition;
    }

    public Class<?> getClassDefinition() throws ClassNotFoundException {
        if (classDefinition == null) classDefinition = Class.forName(this.getType());
        return classDefinition;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
