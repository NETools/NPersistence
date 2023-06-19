package org.ns.npersistence;

/**
 * Klasse die Daten für NSQLC-Provider-Klassen hält.
 */
class NSQLCProviderClassResult {

    private String sqlTableName;
    private String sqlPrimaryKeyName;
    private String sqlIdentifier;

    private String textualPayload;


    public void setSqlTableName(String sqlTableName) {
        this.sqlTableName = sqlTableName;
    }

    public void setSqlPrimaryKeyName(String sqlPrimaryKeyName) {
        this.sqlPrimaryKeyName = sqlPrimaryKeyName;
    }

    public void setTextualPayload(String textualPayload) { this.textualPayload = textualPayload; }

    public void setSqlIdentifier(String sqlIdentifier) { this.sqlIdentifier = sqlIdentifier; }

    public String getSqlPrimaryKeyName() {
        return sqlPrimaryKeyName;
    }

    public String getSqlTableName() {
        return sqlTableName;
    }

    public String getTextualPayload() { return textualPayload; }

    public String getSqlIdentifier() { return sqlIdentifier; }
}
