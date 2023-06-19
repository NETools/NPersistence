package org.ns.npersistence;

/**
 * Gibt ein NPerSistence-ResultSet zurück.
 */
public class NSQLResultSet {

    /**
     * Holt das in der NSQL-Anweisung festgelegte Objekt.
     * Objekte lassen sich über %new.[OBJECT_NAME] spezifizieren.
     * @param classDefinition
     * @param index
     * @param <T>
     * @return
     */
    public <T> T get(Class<T> classDefinition, int index) {
        return (T) NPersistenceSelectPolicy.getResultSet().get(classDefinition).get(index);
    }

    /**
     * Gibt die Anzahl geladener Objekte, die einer Klassendefinition zugeordnet sind, zurück.
     * @param classDefinition
     * @param <T>
     * @return
     */
    public <T> int getResultSetSizeOf(Class<T> classDefinition) {
        return NPersistenceSelectPolicy.getResultSet().get(classDefinition).size();
    }
}
