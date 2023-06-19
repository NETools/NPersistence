/* بسم الله الرحمن الرحيم  */

package org.ns.npersistence;

import org.ns.npersistence.annotations.PrimaryKey;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class NPersistenceSession {
    private String rootFolder;
    private String databaseName;

    private NSQLCompiler nsqlCompiler;
    private Object currentSyncingObject;

    private boolean sessionOpened;

    /**
     * Legt die Ladeart fest.
     */
    public enum RetrieveMode {
        Attached,
        Detached
    }

    public enum SQLDriver {
        SQLite,
        MySQL
    }

    private SQLDriver currentLoadedDriver;

    /**
     * Setzt das aktuelle, synchronisationsbereite Objekt
     *
     * @param currentSyncingObject
     */
    void setCurrentSyncingObject(Object currentSyncingObject) {
        this.currentSyncingObject = currentSyncingObject;
    }


    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isSessionOpened() {
        return sessionOpened;
    }

    public SQLDriver getCurrentLoadedDriver() {
        return this.currentLoadedDriver;
    }

    /***
     * Startet eine neue NPerSistence-Session
     * @param driver
     * @param host
     * @param schema
     * @param userName
     * @param userPassword
     * @throws Exception
     */
    public void openSession(SQLDriver driver, String host, String schema,
                            String userName, String userPassword) throws Exception {

        switch (driver) {
            case MySQL:
                openRemoteSession(driver, host, schema, userName, userPassword);
                break;
            case SQLite:
                openLocalSession(schema);
                break;
        }

    }

    private void openLocalSession(String dbName) throws SQLException {
        if (sessionOpened) {
            NDebugOutputHandler.getDefault().handle(14);
            return;
        }
        this.sessionOpened = true;
        this.databaseName = dbName;

        this.establishLocalConnection();

        NPrimaryKeyGenerator.getDefault().prepareSession(this);
        NEntityUpdateHandler.getDefault().setCurrentSession(this);

    }

    private void openRemoteSession(SQLDriver driver, String host, String schema,
                                   String userName, String userPassword) throws SQLException {
        if (sessionOpened) {
            NDebugOutputHandler.getDefault().handle(14);
            return;
        }
        this.sessionOpened = true;
        this.databaseName = schema;

        this.establishRemoteConnection(driver, host, schema, userName, userPassword);

        NPrimaryKeyGenerator.getDefault().prepareSession(this);
        NEntityUpdateHandler.getDefault().setCurrentSession(this);
    }

    /**
     * Schließt eine NPerSistence-Session
     */
    public void closeSession() throws SQLException {
        if (!sessionOpened) {
            NDebugOutputHandler.getDefault().handle(10);
            return;
        }

        SQLManager.getDefault().getConnection().close();
        this.sessionOpened = false;
    }

    private void establishLocalConnection() throws SQLException {
        SQLManager.getDefault().connectLocal(rootFolder, databaseName);
        this.currentLoadedDriver = SQLDriver.SQLite;
    }

    private void establishRemoteConnection(SQLDriver driver, String host, String schema,
                                           String userName, String userPassword) throws SQLException {
        SQLManager.getDefault().connectRemote(driver, host, schema, userName, userPassword);
        this.currentLoadedDriver = driver;
    }

    /**
     * @param rootFolder Gibt das Hauptverzeichnis zur Datenbank an
     */
    private NPersistenceSession(String rootFolder) {
        this.rootFolder = rootFolder;
    }

    /**
     * Persistiert das angegebene Objekt
     *
     * @param object Das zu persistierende, <strong>@Table</strong>-annotierte Objekt
     */
    public void persistObject(Object object) throws Exception {

        if (!sessionOpened) {
            NDebugOutputHandler.getDefault().handle(10);
            return;
        }

        if (!object.getClass().getName().contains("_proxyClass") && !object.getClass().getSuperclass().getName().equals(Object.class.getName())) {
            NDebugOutputHandler.getDefault().handle(11);
            return;
        }

        if (object.getClass().getName().length() >= 11 && object.getClass().getName().substring(0, 11).equals("_proxyClass")) {
            NDebugOutputHandler.getDefault().handle(12);
            return;
        }

        /* SCHEME GENERATION */

        NSchemeCreate nSchemeGen = new NSchemeCreate();
        nSchemeGen.createSingle(object.getClass());
        nSchemeGen.write();


        /* SCHEME INSERTION */

        NSchemeInsert schemeInsert = new NSchemeInsert();
        schemeInsert.insertSingle(object);
        schemeInsert.write();

        this.getObject(object.getClass(), schemeInsert.getTable().getPrimaryKey(), RetrieveMode.Attached);
    }

    /**
     * Löscht das angegebene @Table markiertes Attached-Objekt.
     *
     * @param remoteObject
     * @throws Exception
     */
    public void delete(Object remoteObject) throws Exception {

        if (!(remoteObject.getClass().getName().length() >= 11 && remoteObject.getClass().getName().substring(0, 11).equals("_proxyClass"))) {
            NDebugOutputHandler.getDefault().handle(13);
            return;
        }

        NTableDelete tableDelete = new NTableDelete(remoteObject);
        tableDelete.loadPrimaryKey();
        tableDelete.delete();

        this.closeSession();
    }

    /***
     * Stellt das letzte gelöschte Element eines einer in einem Remote-Objekt enthaltenen ArrayLists wieder
     * her.
     * @param remoteObject
     * @throws Exception
     */
    public void rollBackDeletion(Object remoteObject) throws Exception {
        if (!(remoteObject.getClass().getName().length() >= 11 && remoteObject.getClass().getName().substring(0, 11).equals("_proxyClass"))) {
            NDebugOutputHandler.getDefault().handle(100);
            return;
        }
        NRecyclement.getDefault().rollback(remoteObject);
    }

    /***
     * Lädt das Remote-Objekt in den RAM
     * @param remoteObject
     * @param <T>
     * @return
     * @throws Exception
     */
    public <T> T makeLocal(T remoteObject) throws Exception {
        if (!(remoteObject.getClass().getName().length() >= 11 && remoteObject.getClass().getName().substring(0, 11).equals("_proxyClass"))) {
            NDebugOutputHandler.getDefault().handle(99);
            return remoteObject;
        }

        var currentInstanceSqlName =
                GeneralToolKit.getFieldValue(remoteObject, "sqlQualifiedName") + "";

        var currentInstancePrimaryKeyName =
                GeneralToolKit.isAnnotationPresent(remoteObject.getClass().getSuperclass(), PrimaryKey.class).
                        getField().
                        getName();

        var currentInstancePrimaryKeyValue = GeneralToolKit.getFieldValue(remoteObject,
                "sqlPrimaryKeyValue");


        var superInstanceName = currentInstanceSqlName.replace("_", ".");
        var nsqlQualifier = "";

        if (currentInstanceSqlName.indexOf("_COMPOSITION_") != -1) {
            superInstanceName =
                    currentInstanceSqlName.substring(0, currentInstanceSqlName.indexOf("_COMPOSITION_")).replace("_", ".");
            var s = currentInstanceSqlName.split("_COMPOSITION_");
            for (int i = 1; i < s.length; i++)
                nsqlQualifier += "." + s[i];
        }

        var nsqlQuery
                =
                "SELECT %new" + nsqlQualifier + " FROM $" + superInstanceName + " :a WHERE !a" + nsqlQualifier + "." +
                currentInstancePrimaryKeyName +
                " = '" + currentInstancePrimaryKeyValue + "';";


        NDebugOutputHandler.getDefault().handle(80, "[NSQL] " + nsqlQuery);

        var rs = this.getByNSQL(
                nsqlQuery,
                RetrieveMode.Detached);
        return (T) rs.get(remoteObject.getClass().getSuperclass(), 0);

    }

    /**
     * Gibt eine Liste mit allen zu einer <strong>@Table</strong>-annotierten Klasse gehörenden
     * Primärschlüsseln zurück.
     *
     * @param classDefinition Klassendefinition
     */
    public ArrayList<Object> getPrimaryKeysOfClass(Class<?> classDefinition) throws Exception {
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder =
                queryBuilder.append("SELECT").append(" * ").append(" FROM ").append(classDefinition.getName().replace(".", "_"));

        ResultSet resultSet = SQLManager.getDefault().select(queryBuilder.toString());

        ArrayList<Object> primaryKeys = new ArrayList<>();

        while (resultSet.next()) primaryKeys.add(resultSet.getObject(1));

        return primaryKeys;
    }

    /**
     * Lädt das zur <strong>@Table</strong>-annotierten Klassendefinition gehörende Objekt anhand des
     * Primärschlüssels
     *
     * @param <T>             Generischer Typ
     * @param classDefinition <strong>@Table</strong>-annotierte Klassendefinition
     * @param primaryKey      Primärschlüssel
     */
    public <T> T getObject(Class<T> classDefinition, Object primaryKey, RetrieveMode mode) throws Exception {
        if (!sessionOpened) {
            NDebugOutputHandler.getDefault().handle(10);
            return null;
        }

        if (mode == RetrieveMode.Detached)
            NPersistenceSelectPolicy.setDetached(true);

        NSchemeSelect schemeSelect = new NSchemeSelect();

        T currentlyLoadedInstance = (T) schemeSelect.get(classDefinition, primaryKey);

        this.setCurrentSyncingObject(currentlyLoadedInstance);

        NPersistenceSelectPolicy.setDetached(false);

        if (NPSPolicy.hasErrorOccured())
            return null;

        return currentlyLoadedInstance;
    }

    /**
     * Synchronisiert das zuletzt abgerufene Objekt mit der Datenbank.
     * Diese Methode <strong>MUSS</strong> immer <strong>NACH</strong> Hinzufügung eines kompositionell
     * Objektes auf
     * dasjenige
     * <strong>@Table</strong>-annotierte Objekt angewandt werden, das geladen wurde.
     * <br>
     * <font color="#800000">Wird diese Methode nach Modifikation eines geladenen
     * <strong>@Table</strong>-annotierten
     * Objekts
     * <br>
     * nicht aufgerufen, sind die Daten inkonsistent: Die Datenbank wird also ungültig und damit
     * fehlerhaft!</font>
     * <p>
     */
    @Deprecated
    public <T> T synchronize() throws Exception {
        return (T) this.currentSyncingObject;
    }

    /**
     * Parses an ns-query-language-directive
     *
     * @param nsql
     * @return
     * @throws Exception
     */
    private ResultSet parseNSQL(String nsql) throws Exception {
        nsqlCompiler = new NSQLCompiler();
        nsqlCompiler.setNsqlQuery(nsql);
        nsqlCompiler.schematize();

        if (!nsqlCompiler.parse()) {
            return null;
        }

        NDebugOutputHandler.getDefault().handle(80, "[SQL] " + nsqlCompiler.getQuery());

        return SQLManager.getDefault().select(nsqlCompiler.getQuery());
    }

    /**
     * Lädt ein durch ein NSQL-Query spezifiziertes Objekt.
     *
     * @param nsql
     * @param retrieveMode
     * @return
     * @throws Exception
     */
    public NSQLResultSet getByNSQL(String nsql, RetrieveMode retrieveMode) throws Exception {

        NPersistenceSelectPolicy.initialize();
        NPersistenceSelectPolicy.setRequired(true);

        ResultSet resultSet = this.parseNSQL(nsql);
        if (resultSet == null)
            return null;

        while (resultSet.next()) {
            for (int i = 0; i < resultSet.getMetaData().getColumnCount(); i++) {
                var currentClass = NPersistenceSelectPolicy.getIndices().get(i + 1);
                NPersistenceSelectPolicy.addPrimaryKey(currentClass, resultSet.getObject(i + 1));
            }
        }

        /* LOAD OBJECTS -- BEGIN BY ROOT */
        for (Object pk : NPersistenceSelectPolicy.getPrimaryKeys(this.nsqlCompiler.getCurrentLoadedClass()))
            this.getObject(this.nsqlCompiler.getCurrentLoadedClass(), pk, retrieveMode);


        NPersistenceSelectPolicy.setRequired(false);


        return new NSQLResultSet();
    }

    /**
     * Löscht alle generiertes Primärschlüsseln.
     * Achtung: Kann zu Inkonsistenz der Daten führen und das gesamte Datenbanksystem unbrauchbar machen.
     */
    public void resetMetaTable() throws SQLException {
        var stmt = SQLManager.getDefault().getConnection().createStatement();
        stmt.executeUpdate("DELETE FROM " + GeneralToolKit.sha1("META_" + this.getDatabaseName().replace(".", "_")));
        stmt.close();
    }

    private static String ROOT_FOLDER;
    public static void setRootFolder(String rootFolder){
        ROOT_FOLDER = rootFolder;
    }

    private static NPersistenceSession persistenceSession;
    public static NPersistenceSession getDefault() {
        if (persistenceSession == null)
            persistenceSession = new NPersistenceSession(ROOT_FOLDER);
        return persistenceSession;
    }


}





