package org.ns.npersistence;

/**
 * Klasse zur Regelung von bestimmten Verhaltensmustern im Umgang mit Aktualisierung von <strong>persistierten</strong> Objekten
 */
public class NEntityUpdateHandlerPolicy {

    private static boolean forceDeleteTable;
    private static boolean automaticSynchronisation;
    private static boolean allowRecyclement;

    /**
     * Legt fest, ob @Table-annotierte Klassen gelöscht werden sollen, oder nicht.
     *
     * @param forceDeleteTable
     */
    public static void setForceDeleteTable(boolean forceDeleteTable) {
        NEntityUpdateHandlerPolicy.forceDeleteTable = forceDeleteTable;
    }

    public static boolean doForceDeleteTable() {
        return forceDeleteTable;
    }

    public static boolean isAutoSyncActivated() {
        return automaticSynchronisation;
    }

    public static void setAutoSync(boolean automaticSynchronisation) {
        NEntityUpdateHandlerPolicy.automaticSynchronisation = automaticSynchronisation;
    }

    @Deprecated
    public static void setAllowRecyclement(boolean allowRecyclement) {
        NEntityUpdateHandlerPolicy.allowRecyclement = allowRecyclement;
    }

    @Deprecated
    public static boolean isRecyclementAllowed() {
        return allowRecyclement;
    }
}
