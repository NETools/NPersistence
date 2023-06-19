package org.ns.npersistence;


/**
 * Klasse zur Erstellung einer sql-SELECT-Anweisung
 */
class NPSFluentSelecting {
    private StringBuilder sqlQueryBuilder;

    public NPSFluentSelecting() {
        this.sqlQueryBuilder = new StringBuilder();
    }

    /* SELECT * FROM WHERE */

    public NPSFluentSelecting begin() {
        this.sqlQueryBuilder.append("SELECT ");
        return this;
    }

    private boolean columnAdded;

    public NPSFluentSelecting all() {
        this.sqlQueryBuilder.append("*");
        return this;
    }

    public NPSFluentSelecting addColumn(String columnName) {
        if (!columnAdded) this.sqlQueryBuilder.append(columnName);
        else this.sqlQueryBuilder.append(", " + columnName);
        columnAdded = true;
        return this;
    }

    private boolean tableAdded;

    public NPSFluentSelecting addTable(String tableName) {
        tableName = GeneralToolKit.sha1(tableName);
        if (!tableAdded) this.sqlQueryBuilder.append(" FROM " + tableName);
        else this.sqlQueryBuilder.append(", " + tableName);
        tableAdded = true;
        return this;
    }


    public NPSFluentSelecting where(String condition) {
        addWhereClausel();
        this.sqlQueryBuilder.append(" " + condition);
        return this;
    }

    public NPSFluentSelecting where(String columnName, Object data) {
        if(data.getClass().equals(String.class))
            return where(columnName + " = '" + data + "'");
        else return where(columnName + " = " + data);
    }

    public NPSFluentSelecting and() {
        this.sqlQueryBuilder.append(" AND");
        return this;
    }

    public NPSFluentSelecting or() {
        this.sqlQueryBuilder.append(" OR");
        return this;
    }

    public String end() {
        return this.sqlQueryBuilder.toString();
    }

    boolean whereClauselAdded;

    private void addWhereClausel() {
        if (!whereClauselAdded) this.sqlQueryBuilder.append(" WHERE");
        whereClauselAdded = true;
    }

}
