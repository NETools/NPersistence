package org.ns.npersistence;

/**
 * Klasse die wichtige Tabellen-Daten hält
 */
class ForeignReference {
    private String sqlCompositionTableName;
    private String sqlPrimaryKeyName;
    private Object sqlPrimaryKeyValue;

    public String getSqlCompositionTableName() {
        return sqlCompositionTableName;
    }

    public String getSqlPrimaryKeyName() {
        return sqlPrimaryKeyName;
    }

    public Object getSqlPrimaryKeyValue() {
        return sqlPrimaryKeyValue;
    }

    public void setSqlCompositionTableName(String sqlCompositionTableName) {
        this.sqlCompositionTableName = sqlCompositionTableName;
    }

    public void setSqlPrimaryKeyName(String sqlPrimaryKeyName) {
        this.sqlPrimaryKeyName = sqlPrimaryKeyName;
    }

    public void setSqlPrimaryKeyValue(Object sqlPrimaryKeyValue) {
        this.sqlPrimaryKeyValue = sqlPrimaryKeyValue;
    }

    @Override
    public String toString() {
        return "ForeignReference{" + "sqlCompositionTableName='" + sqlCompositionTableName + '\'' + ", " +
                "sqlPrimaryKeyName='" + sqlPrimaryKeyName + '\'' + ", sqlPrimaryKeyValue=" + sqlPrimaryKeyValue + '}';
    }
}

