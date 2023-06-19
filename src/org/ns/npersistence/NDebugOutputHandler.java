package org.ns.npersistence;

/**
 * Allgemeines Interface zur Ausgabe von Debug-Informationen
 */
public class NDebugOutputHandler {
    private static NDebugOutputHandler errorHandler;

    /**
     * Gibt ein Default-Objekt dieser Klasse zurück
     *
     * @return
     */
    public static NDebugOutputHandler getDefault() {
        if (errorHandler == null)
            errorHandler = new NDebugOutputHandler();
        return errorHandler;
    }


    /**
     * Legt das Output-Verhalten fest
     */
    public enum DebugOutputMode {
        NoOutput,
        Console
    }

    enum DebugOutputType {
        Error,
        Warning,
        Information
    }

    NDebugOutputHandler() {
        debugOutputMode = DebugOutputMode.Console;
    }

    private DebugOutputMode debugOutputMode;
    private DebugOutputType debugOutputType;

    public DebugOutputMode getDebugOutputMode() {
        return debugOutputMode;
    }

    public void setDebugOutputMode(DebugOutputMode debugOutputMode) {
        this.debugOutputMode = debugOutputMode;
    }

    /**
     * Ausgabe einer Nachricht gemäß des messageCodes
     * @param messageCode
     */
    void handle(int messageCode) {

        switch (messageCode) {
            case 150:
                debugOutputType = DebugOutputType.Error;
                output("NSQL COMPILATION FAILED!\n\t\tPLEASE CHECK THE SYNTAX!");
                break;

            case 151:
                debugOutputType = DebugOutputType.Error;
                output("NSQL COMPILATION FAILED!\n\t\tNO CLASS WAS DECLARED!");
                break;

            case 152:
                debugOutputType = DebugOutputType.Error;
                output("NSQL COMPILATION FAILED!\n\t\tNO WHERE STATEMENT WAS DECLARED!");
                break;

            case 10:
                debugOutputType = DebugOutputType.Error;
                output("ILLEGAL OPERATION: SESSION NOT OPENED!\n" + "\t" +
                        "\tPLEASE CONTACT THE DEVELOPER FOR FURTHER INFORMATION\n");
                break;
            case 11:
                debugOutputType = DebugOutputType.Error;
                output("INHERITANCE NOT IMPLEMENTED YET!\n" + "\t" +
                        "\tPLEASE CONTACT THE DEVELOPER FOR FURTHER INFORMATION\n");
                break;
            case 12:
                debugOutputType = DebugOutputType.Error;
                output("ILLEGAL OPERATION: OBJECT IS ATTACHED TO " +
                        "DATABASE!\n" +
                        "\t\tPLEASE CONTACT THE DEVELOPER FOR FURTHER INFORMATION\n" +
                        "\t\tPROBLEM ORIGIN: YOU TRY TO PERSIST A PROXY CLASS: PLEASE MODIFIY RETRIEVED " +
                        "OBJECTS" +
                        " WITHIN THE SCOPE OF AN OPENED SESSION!");
                break;

            case 13:
                debugOutputType = DebugOutputType.Error;
                output("ILLEGAL OPERATION: OBJECT IS NOT ATTACHED " +
                        "TO " +
                        "DATABASE!\n" +
                        "\t\tPLEASE CONTACT THE DEVELOPER FOR FURTHER INFORMATION\n" +
                        "\t\tPROBLEM ORIGIN: YOU TRY TO DELETE A NON-REMOTE OBJECT: PLEASE REMOTE RETRIEVED" +
                        " " +
                        "OBJECTS" +
                        " WITHIN THE SCOPE OF AN OPENED SESSION!");
                break;
            case 99:
                debugOutputType = DebugOutputType.Error;
                output("ILLEGAL OPERATION: OBJECT IS NOT ATTACHED " +
                        "TO " +
                        "DATABASE!\n" +
                        "\t\tPLEASE CONTACT THE DEVELOPER FOR FURTHER INFORMATION\n" +
                        "\t\tPROBLEM ORIGIN: OBJECT IS ALREADY LOADED INTO RAM.");
                break;
            case 100:
                debugOutputType = DebugOutputType.Error;
                output("ILLEGAL OPERATION: OBJECT IS NOT ATTACHED " +
                        "TO " +
                        "DATABASE!\n" +
                        "\t\tPLEASE CONTACT THE DEVELOPER FOR FURTHER INFORMATION\n" +
                        "\t\tPROBLEM ORIGIN: YOU TRY TO ROLLBACK A NON-REMOTE OBJECT: PLEASE REMOTE RETRIEVED" +
                        " " +
                        "OBJECTS" +
                        " WITHIN THE SCOPE OF AN OPENED SESSION!");
                break;
            case 14:
                debugOutputType = DebugOutputType.Error;
                output("ILLEGAL OPERATION: SESSION ALREADY" +
                        " OPENED!\n" + "\t" + "\tPLEASE CONTACT THE DEVELOPER FOR FURTHER INFORMATION\n");
                break;

            case 15:
                debugOutputType = DebugOutputType.Error;
                output("DATABASES IS INCONSISTENT - HOST CLASS HAS BEEN MODIFIED!\n" +
                        "\t\tALTER THE TABLE MANUALLY, OR DELETE .DB-FILE AND RE-PERSIST THE " + ConsoleConstants
                        .RED_BOLD + ConsoleConstants.RED_UNDERLINED + "WHOLE" + ConsoleConstants.RED + " " +
                        "STRUCTURE!");
                break;

            case 50:
                debugOutputType = DebugOutputType.Error;
                output("NO PRIMARY KEY FOUND -- DATA CORRUPT!!!");
                break;
            case 51:
                debugOutputType = DebugOutputType.Error;
                output("ROW ALREADY EXISTS!");
                break;
            case 52:
                debugOutputType = DebugOutputType.Error;
                output("DATA CORRUPTED -- CAN'T " +
                        "PERSIST!!");
                break;

            case 60:
                debugOutputType = DebugOutputType.Warning;
                output("RETRIEVED OBJECT IS NOT SYNCHRONIZED!\nCONSIDER USING NEntityUpdateHandlerPolicy.setAutoSync(true);!");
                break;

            case 61:
                debugOutputType = DebugOutputType.Warning;
                output("ILLEGAL OPERATION: SESSION CLOSED!\n" +
                        "\t" +
                        "\tCHANGES " + "ARE NOT SYNCHRONIZED!");
                break;

            case 62:
                debugOutputType = DebugOutputType.Error;
                output("OBJECT RETRIEVED IN ANOTHER SESSION!\n" +
                        "\t\tMODIFICATION NOT ALLOWED HERE!\n" +
                        "\t\tRETRIEVE OBJECT IN DETACHED MODE!");
                break;
            case 65:
                debugOutputType = DebugOutputType.Error;
                output("REMOTE OBJECTS CAN'T ADDED TO " +
                        "\t\tLIST!\n" +
                        "RETRIEVE REMOTE OBJECT IN DETACHED MODE AND TRY AGAIN!");
                break;
            case 70:
                debugOutputType = DebugOutputType.Error;
                output("PRIMARY KEYS CANT BE CHANGED!");
                break;

            case 71:
                debugOutputType = DebugOutputType.Error;
                output("NO DEFAULT-CONSTRUCTOR DECLARED!");
                break;
            case 567:
                debugOutputType = DebugOutputType.Error;
                output("FATAL ERROR!");
                break;

        }
    }

    /**
     * Ausgabe eines Textes gemäß des messageCodes
     * @param messageCode
     * @param additions
     */
    void handle(int messageCode, String additions) {
        switch (messageCode) {
            case 80:
                debugOutputType = DebugOutputType.Information;
                break;
            case 81:
                debugOutputType = DebugOutputType.Warning;
                break;
            case 82:
                debugOutputType = DebugOutputType.Error;
                break;
        }

        output(additions);
    }

    /**
     * Ausgabe des Textes
     * @param message
     */
    void output(String message) {

        switch (this.getDebugOutputMode()) {
            case Console:
                switch (this.debugOutputType) {
                    case Error:
                        System.out.println(ConsoleConstants.RED + "[ERROR]\t" + message + ConsoleConstants.RESET);
                        break;
                    case Warning:
                        System.out.println(ConsoleConstants.RED_BRIGHT + "[WARNING]\t" + message + ConsoleConstants.RESET);
                        break;
                    case Information:
                        System.out.println(ConsoleConstants.YELLOW + "[INFORMATION] " + message + ConsoleConstants.RESET);
                        break;
                }
                break;
            case NoOutput:
                break;
        }
    }

}
