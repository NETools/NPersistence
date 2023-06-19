package org.ns.npersistence;

/**
 * Klasse für Fehlerverwaltung.
 */
class NPSPolicy {
    private static boolean errorOccured;

    static boolean hasErrorOccured() {
        return errorOccured;
    }
    static void setErrorOccured(boolean errorOccured) {
        NPSPolicy.errorOccured = errorOccured;
    }

}
