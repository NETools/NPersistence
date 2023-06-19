package org.ns.npersistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Kompiliert eine NSQL-Anweisung und transformiert diese in eine SQLite-Anweisung.
 */
class NSQLCompiler {

    private NSQLTokenizer nsqlTokenizer;
    private String nsqlQuery;

    private ArrayList<NSQLCToken> nsqlcTokens;
    private NSQLCToken currentClassToken;
    private boolean loadedKeySelector;

    private Class<?> currentLoadedClass;
    private String currentLoadedClassIdentifier;
    private String currentLoadedClassSQLName;


    private HashMap<String, String> sqlFieldIdentifier;
    private HashMap<String, String> sqlFieldIdentifierFull;

    private HashSet<String> usedJoinIdentifiers;

    private StringBuilder queryBuilder;


    public NSQLCompiler() {
        this.nsqlTokenizer = new NSQLTokenizer();

        this.nsqlcTokens = new ArrayList<>();

        this.sqlFieldIdentifier = new HashMap<>();
        this.sqlFieldIdentifierFull = new HashMap<>();

        this.usedJoinIdentifiers = new HashSet<>();

        this.queryBuilder = new StringBuilder();

    }


    public void setNsqlQuery(String nsqlQuery) {
        this.nsqlQuery = nsqlQuery;
    }

    public String getNsqlQuery() {
        return nsqlQuery;
    }

    public ArrayList<NSQLCToken> getNsqlcTokens() {
        return nsqlcTokens;
    }

    public HashMap<String, String> getSqlFieldIdentifier() {
        return sqlFieldIdentifier;
    }

    public HashMap<String, String> getSqlFieldIdentifierFull() {
        return sqlFieldIdentifierFull;
    }

    public HashSet<String> getUsedJoinIdentifiers() {
        return usedJoinIdentifiers;
    }

    public void setCurrentLoadedClass(Class<?> currentLoadedClass) {
        this.currentLoadedClass = currentLoadedClass;
    }

    public NSQLCToken getCurrentClassToken() {
        return currentClassToken;
    }

    public Class<?> getCurrentLoadedClass() {
        return currentLoadedClass;
    }

    public String getCurrentLoadedClassIdentifier() {
        return currentLoadedClassIdentifier;
    }

    public String getCurrentLoadedClassSQLName() {
        return currentLoadedClassSQLName;
    }

    public void appendQuery(String query) {
        this.queryBuilder.append(query + " ");
    }

    public String getQuery() {
        return this.queryBuilder.toString();
    }


    public boolean isLoadedKeySelector() {
        return loadedKeySelector;
    }

    /**
     * Erstellt ein allgemeinen Syntax-Baum
     *
     * @throws Exception
     */
    public void schematize() throws Exception {
        this.nsqlTokenizer.tokenize(this.getNsqlQuery());

        NSQLCToken nsqlcToken = null;

        int pointer = 0;
        int currentIndex = -1;
        HashSet<TokenType> eofDirectives = new HashSet<>();

        while (pointer < this.nsqlTokenizer.getTokens().size()) {
            NSQLToken token = this.nsqlTokenizer.getTokens().get(pointer);
            switch (token.getTokenType()) {
                case NEW:
                    nsqlcToken = new NSQLCToken(this, NSQLCToken.NSQLCTokenType.PK_KEYWORD);
                    this.nsqlcTokens.add(nsqlcToken);

                    eofDirectives.add(TokenType.SQL);
                    eofDirectives.add(TokenType.COMMA);
                    pointer = fetch(pointer, nsqlcToken, eofDirectives);
                    eofDirectives.clear();

                    pointer++;

                    break;
                case CLASS:

                    this.currentClassToken = new NSQLCToken(this, NSQLCToken.NSQLCTokenType.CLASS_NAME);

                    eofDirectives.add(TokenType.OBJECT);
                    pointer = fetch(pointer, currentClassToken, eofDirectives);
                    eofDirectives.clear();

                    currentClassToken.setTextualPayload(this.nsqlTokenizer.getTokens().get(++pointer).getTokenContent());
                    currentClassToken.sqlize();

                    this.sqlFieldIdentifier.put(currentClassToken.getTextualPayload(),
                            currentClassToken.getTextualPayload());

                    pointer++;
                    break;

                case FIELD:
                    nsqlcToken = new NSQLCToken(this, NSQLCToken.NSQLCTokenType.FIELD_NAME);
                    this.nsqlcTokens.add(nsqlcToken);

                    eofDirectives.add(TokenType.SQL_COMPARISION);
                    eofDirectives.add(TokenType.SQL_OPERATOR);
                    eofDirectives.add(TokenType.EOF);
                    pointer = fetch(pointer, nsqlcToken, eofDirectives);
                    eofDirectives.clear();

                    nsqlcToken.generateFieldIdentifier(1);


                    for (String identifier : nsqlcToken.getSubIdentifiers()) {

                        if (identifier.equals(this.currentClassToken.getTextualPayload()))
                            continue;
                        if (!this.sqlFieldIdentifier.containsKey(identifier)) {
                            this.sqlFieldIdentifier.put(identifier,
                                    "_" + (++NSQLCToken.IDENTIFIER_INDEX));
                        }
                    }


                    this.sqlFieldIdentifierFull.
                            put("!" + nsqlcToken.getIdentifier() + "." +
                                            nsqlcToken.getIdentifierTokens().get(nsqlcToken.getIdentifierTokens().size() - 1).getTokenContent(),
                                    this.sqlFieldIdentifier.get(nsqlcToken.getIdentifier()) + "." +
                                            nsqlcToken.getIdentifierTokens().get(nsqlcToken.getIdentifierTokens().size() - 1).getTokenContent());

                    nsqlcToken.setTextualPayload(this.sqlFieldIdentifier.get(nsqlcToken.getIdentifier()));
                    pointer++;


                    break;
                default:
                    pointer++;
                    break;
            }

        }


    }


    /**
     * Parst den Syntax-Baum und erstellt SQLite-Anweisungen.
     *
     * @return
     * @throws Exception
     */
    public boolean parse() throws Exception {

        if (this.getCurrentClassToken() == null) {
            NDebugOutputHandler.getDefault().handle(151);
            return false;
        }

        NPersistenceSelectPolicy.addClass(this.getCurrentLoadedClass());

        int percentNewTokens = 0;

        this.appendQuery("SELECT DISTINCT");

        try {
            for (int i = 0; i < this.getNsqlcTokens().size(); i++, percentNewTokens++) {
                if (this.getNsqlcTokens().get(i).getNsqlcTokenType() == NSQLCToken.NSQLCTokenType.PK_KEYWORD) {
                    this.getNsqlcTokens().get(i).sqlize();
                    loadedKeySelector = true;
                } else break;
            }
        } catch (Exception ex) {
            NDebugOutputHandler.getDefault().handle(150);
            return false;
        }

        this.appendQuery("FROM");
        this.appendQuery(GeneralToolKit.sha1(this.getCurrentClassToken().getClassResult().getSqlTableName()));
        this.appendQuery(this.getCurrentClassToken().getTextualPayload());

        this.getUsedJoinIdentifiers()
                .add(this.getCurrentClassToken().getTextualPayload());


        try {
            for (int i = percentNewTokens; i < this.getNsqlcTokens().size(); i++)
                this.getNsqlcTokens().get(i).sqlize();
        } catch (Exception ex) {
            NDebugOutputHandler.getDefault().handle(150);
            return false;
        }

        int whereIndex = this.getNsqlQuery().indexOf("WHERE");

        if (whereIndex == -1) {
            NDebugOutputHandler.getDefault().handle(152);
            return false;
        }

        String whereClausel = this.getNsqlQuery().substring(whereIndex);

        for (var kv : this.sqlFieldIdentifierFull.entrySet()) {
            whereClausel = whereClausel.replace(kv.getKey(), kv.getValue());
        }

        for (var pk : this.getNsqlcTokens()) {
            if (!pk.getIdentifier().equals("NON_FIELD"))
                continue;

            if (pk.getSqlPrimaryKeyName() == null) continue;

            whereClausel
                    = whereClausel.replace(
                    pk.getQualifiedNameSpace() + "." + pk.getSqlPrimaryKeyName()
                            .substring(3),
                    pk.getQualifiedNameSpace() + "." + pk.getSqlPrimaryKeyName());

        }

        this.appendQuery(whereClausel);

        return true;
    }

    private int fetch(int currentPointer, NSQLCToken token, HashSet<TokenType> eofs) {
        while (!eofs.contains(this.nsqlTokenizer.getTokens().get(++currentPointer).getTokenType())) {
            token.append(this.nsqlTokenizer.getTokens().get(currentPointer));
        }


        return currentPointer;
    }


}
