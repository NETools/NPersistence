package org.ns.npersistence;

import org.ns.npersistence.annotations.Table;

import java.util.ArrayList;

class NSchemeCreate {
    private String mPackageName;

    private ArrayList<Class<?>> modifiedClasses = new ArrayList<>();
    private ArrayList<String> sqlQueries = new ArrayList<>();

    public String getPackageName() {
        return mPackageName;
    }

    public void addModifiedClass(Class<?> ctClass) {
        this.modifiedClasses.add(ctClass);
    }

    public void addSqlQuery(String sql) {
        this.sqlQueries.add(sql);
    }

    public void write() {

        if (!NPSPolicy.hasErrorOccured())
            for (String s : this.sqlQueries) {
                NDebugOutputHandler.getDefault().handle(80, "EXECUTING: " + s);
                SQLManager.getDefault().createTable(s);
            }
    }

    public void createSingle(Class classDefinition) throws Exception {
        if (classDefinition.getName().length() >= 11 && classDefinition.getName().substring(0, 11).equals("_proxyClass")) {
            NDebugOutputHandler.getDefault().handle(12);
            return;
        }

        if (classDefinition.isAnnotationPresent(Table.class)) {
            NTableCreate pTable = new NTableCreate(this, classDefinition.getName());
            pTable.create();
        }

    }
}
