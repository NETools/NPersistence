package org.ns.npersistence;

import java.util.ArrayList;

/**
 * Stellt eine Nutzlast dar - i.e. zu persistierende (insert) Attribute.
 */
class NPayload {


    private String sqlQuery;
    private ArrayList<Object> data;

    public NPayload(String sqlQuery) {
        this.sqlQuery = sqlQuery;
        this.data = new ArrayList<>();
    }

    public ArrayList<Object> getData() {
        return data;
    }

    public void setData(ArrayList<Object> data) {
        this.data = data;
    }

    public void addData(Object object) {
        this.data.add(object);
    }

    public String getSqlQuery() {
        return sqlQuery;
    }
}
