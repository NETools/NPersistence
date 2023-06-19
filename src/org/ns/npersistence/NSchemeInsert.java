package org.ns.npersistence;

import javassist.ClassPool;
import javassist.Loader;
import org.ns.npersistence.annotations.Table;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.ArrayList;

class NSchemeInsert {

    private ClassPool classPool;
    private Loader loader;

    private NTableInsert table;

    private ArrayList<NPayload> payloads;

    public ClassPool getClassPool() {
        return classPool;
    }

    public Loader getLoader() {
        return loader;
    }

    public NTableInsert getTable() {
        return table;
    }

    public void addPayload(NPayload payload) {
        this.payloads.add(payload);
    }

    public NSchemeInsert() {

        this.classPool = ClassPool.getDefault();
        this.loader = new Loader(this.getClassPool());

        this.payloads = new ArrayList<>();
    }


    public void write() {
        if (!NPSPolicy.hasErrorOccured())
            for (NPayload payload : this.payloads) {
            NDebugOutputHandler.getDefault().handle(80, "[INSERTING] " + payload.getSqlQuery());
            try {
                SQLManager.getDefault().insert(payload.getSqlQuery(), payload.getData().toArray());
            } catch (SQLException throwables) {
                switch (throwables.getErrorCode()) {
                    case 19:
                        NDebugOutputHandler.getDefault().handle(51);
                        NPSPolicy.setErrorOccured(true);
                        break;
                    default:
                        System.out.println(throwables.getMessage());
                }
            }
        }
    }


    public void insertSingle(Object object) throws Exception {
        Annotation[] pAnnotations = object.getClass().getDeclaredAnnotations();

        if (object.getClass().getName().length() >= 11 && object.getClass().getName().substring(0, 11).equals(
                "_proxyClass")) {
            NDebugOutputHandler.getDefault().handle(12);
            return;
        }

        if(object.getClass().isAnnotationPresent(Table.class)){
            NTableInsert tableInsert = new NTableInsert(this, object);
            tableInsert.insert();
            this.table = tableInsert;
        }

    }

    public void insertLater(String root, Field field, Object object, boolean insert) throws Exception {
        Annotation[] pAnnotations = object.getClass().getDeclaredAnnotations();

        if (object.getClass().getName().length() >= 11 && object.getClass().getName().substring(0, 11).equals(
                "_proxyClass")) {
            NDebugOutputHandler.getDefault().handle(12);
            return;
        }

        NTableInsert tableInsert = null;

        if (object.getClass().isAnnotationPresent(Table.class)) {
            tableInsert = new NTableInsert(this, object);
        } else {
            tableInsert = new NTableInsert(this, object, root, field);
        }


        if (insert) tableInsert.insert();
        this.table = tableInsert;
    }



}



