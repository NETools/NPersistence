package org.ns.npersistence;

import org.ns.npersistence.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

class NTableInsert {
    private NSchemeInsert scheme;

    private Object object;
    private Class<?> objectClass;

    private String sqlName;
    private Object primaryKey;

    private ArrayList<Object> values;
    private NPSFluentInserting fluentInserting;

    public NSchemeInsert getScheme() {
        return this.scheme;
    }

    public void setSqlName(String sqlName) {
        this.sqlName = sqlName;
    }

    public String getSqlName() {
        return sqlName;
    }

    public void addObject(Object object) {
        this.values.add(object);
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    public NPSFluentInserting getCurrentInsertQuery() {
        return this.fluentInserting;
    }

    public void setInsertQuery(NPSFluentInserting insertQuery) {
        this.fluentInserting = insertQuery;
    }

    public Object getCurrentObjectInstance() {
        return this.object;
    }

    public Class<?> getCurrentObjectClass() {
        return this.objectClass;
    }

    public NTableInsert(NSchemeInsert currentScheme, Object object) {
        this.scheme = currentScheme;
        this.values = new ArrayList<>();

        this.object = object;
        this.objectClass = this.getCurrentObjectInstance().getClass();


        this.sqlName = this.getCurrentObjectClass().getName().replace(".", "_");
    }

    public NTableInsert(NSchemeInsert currentScheme, Object data, String root, Field field) {
        this(currentScheme, data);
        this.sqlName = root;
        if (field != null) this.sqlName += "_COMPOSITION_" + field.getName();
    }


    public void insert() throws Exception {

        if (this.getCurrentObjectClass().getName().length() >= 11 && this.getCurrentObjectClass().getName().substring(0, 11).equals(
                "_proxyClass")) {
            NDebugOutputHandler.getDefault().handle(12);
            return;
        }

        this.fluentInserting = NPSFluentSQLing.insertInto().begin().toInsertIn(this.getSqlName());

        this.process(this.getCurrentObjectClass().getDeclaredFields());

        NPayload payload = new NPayload(this.getCurrentInsertQuery().end());
        payload.setData(this.values);

        this.getScheme().addPayload(payload);
    }

    public void process(Field[] fields) throws Exception {
        for (Field field : fields) {
            field.setAccessible(true);

            Annotation[] annotations = field.getDeclaredAnnotations();

            for (Annotation annotation : annotations) {
                if (annotation.annotationType().equals(AtomicAttribute.class)) {
                    TableProcessorsInsert.getAtomicAttributeProcessor().process(this, field, annotation);
                } else if (annotation.annotationType().equals(PrimaryKey.class)) {
                    TableProcessorsInsert.getPrimaryKeyAttributeProcessor().process(this, field, annotation);
                } else if (annotation.annotationType().equals(CompositeAttribute.class)) {
                    TableProcessorsInsert.getCompositionAttributeProcessor().process(this, field, annotation);
                } else if (annotation.annotationType().equals(IterableAttribute.class)) {
                    TableProcessorsInsert.getIterableAttributeProcessor().process(this, field, annotation);
                } else if (annotation.annotationType().equals(MediaAttribute.class)) {
                    TableProcessorsInsert.getMediaTypeAttributeProcessorInsert().process(this, field, annotation);
                }
            }
        }
    }

}








