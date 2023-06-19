package org.ns.npersistence;


/**
 * Klasse zur Erstellung einert sql-Anweisung
 */
class NPSFluentSQLing {
    public static NPSFluentTabling createNewTable() {
        return new NPSFluentTabling();
    }

    public static NPSFluentInserting insertInto() {
        return new NPSFluentInserting();
    }

    public static NPSFluentSelecting selectTable() { return new NPSFluentSelecting(); }
}
