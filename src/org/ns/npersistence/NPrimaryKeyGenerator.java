package org.ns.npersistence;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Klasse zur automatischen Generierung und Vergabe von Primärschlüsseln.
 * Kollisionswahrscheinlichkeit: ~ 4.65661288E-10 unter der Annahme, dass javainterne Random-Klasse eine Gleichverteilung der Zahlen gewährleistet.
 */
class NPrimaryKeyGenerator {

    private static NPrimaryKeyGenerator primaryKeyGenerator;

    public static NPrimaryKeyGenerator getDefault() {
        if (primaryKeyGenerator == null)
            primaryKeyGenerator = new NPrimaryKeyGenerator();
        return primaryKeyGenerator;
    }

    private int lastGeneratedPrimaryKey;

    public int getLastGeneratedPrimaryKey() {
        return this.lastGeneratedPrimaryKey;
    }

    public void setLastGeneratedPrimaryKey(int lastGeneratedPrimaryKey) {
        this.lastGeneratedPrimaryKey = lastGeneratedPrimaryKey;
    }

    NPrimaryKeyGenerator() {

    }


    private NPersistenceSession persistence;

    /**
     * Bereitet für das angegebene Session eine Allokationstabelle vor.
     *
     * @param currentSession
     */
    public void prepareSession(NPersistenceSession currentSession) {
        this.persistence = currentSession;
        String sqlQuery = NPSFluentSQLing.createNewTable()
                .begin()
                .ifNotExists()
                .callIt("META_" + currentSession.getDatabaseName().replace(".", "_"))
                .addColumn("meta_class_name", "TEXT", "")
                .addColumn("meta_object_pk", "INTEGER", "")
                .end();
        SQLManager.getDefault().createTable(sqlQuery);

        NDebugOutputHandler.getDefault().handle(80, "[INSERTING] " + sqlQuery);
    }

    public NPersistenceSession getCurrentSession() {
        return persistence;
    }

    /**
     * Erzeugt ein neuen Primärschlüssel - ist dieser Primärschlüssel vergeben, wird rekursiv ein neuer erzeugt.
     * Stackoverflow dann möglich, wenn 2147483646 Schlüssel für eine @Table/@Composition vergeben sind.
     *
     * @param tableName
     * @return
     * @throws SQLException
     */
    public int getPrimaryKey(String tableName) throws SQLException {
        int result = GeneralToolKit.getDefaultRandomGenerator().nextInt(Integer.MAX_VALUE);
        if (result == 0)
            return getPrimaryKey(tableName);

        String selectQuery
                = new StringBuilder()
                .append("SELECT ")
                .append("* ")
                .append("FROM ")
                .append("META_" + this.getCurrentSession().getDatabaseName().replace(".", "_"))
                .append(" WHERE ")
                .append("meta_class_name = '" + tableName + "'")
                .append("meta_object_pk = " + result).toString();

        ResultSet resultSet =
                SQLManager.getDefault().select(selectQuery);

        if (resultSet != null && !resultSet.isClosed())
            return getPrimaryKey(tableName);

        String insertQuery =
                NPSFluentSQLing.insertInto()
                        .begin()
                        .toInsertIn("META_" + this.getCurrentSession().getDatabaseName().replace(".", "_"))
                        .values("meta_class_name")
                        .values("meta_object_pk")
                        .end();

        SQLManager.getDefault().insert(insertQuery, tableName, result);

        lastGeneratedPrimaryKey = result;
        return result;
    }


}
