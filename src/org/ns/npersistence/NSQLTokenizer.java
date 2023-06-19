package org.ns.npersistence;

import java.util.ArrayList;

/**
 * NSQL-Tokenizer: Dieser erstellt aus dem Rohtext Tokens, die dann an den Compiler übergeben werden.
 */
class NSQLTokenizer {
    private ArrayList<NSQLToken> NSQLTokens;
    private String nsqlQuery;

    public ArrayList<NSQLToken> getTokens() {
        return NSQLTokens;
    }
    public String getNsqlQuery() { return nsqlQuery; }

    public NSQLTokenizer() {
        this.NSQLTokens = new ArrayList<>();
    }



    private void addContextual(String content) {

        if (content.length() == 0) return;


        NSQLToken lastNSQLToken = null;
        if (this.getTokens().size() > 0) lastNSQLToken = this.getTokens().get(this.getTokens().size() - 1);
        TokenType tokenType = NSQLTokenMapping.getTokenType(content);

        if (lastNSQLToken != null && lastNSQLToken.getTokenType().equals(TokenType.SQL_COMPARISION))
            tokenType = TokenType.VALUE;

        NSQLToken currentNSQLToken = new NSQLToken();


        currentNSQLToken.setTokenType(tokenType);

        currentNSQLToken.setTokenContent(content);

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addDot() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.DOT);
        currentNSQLToken.setTokenContent(".");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addPound() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.ITERABLE);
        currentNSQLToken.setTokenContent("#");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addColon() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.OBJECT);
        currentNSQLToken.setTokenContent(":");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addExclamation() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.FIELD);
        currentNSQLToken.setTokenContent("!");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addDollar() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.CLASS);
        currentNSQLToken.setTokenContent("$");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addComma() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.COMMA);
        currentNSQLToken.setTokenContent(",");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addBracketOpen() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.BRACKET);
        currentNSQLToken.setTokenContent("(");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addBracketClosed() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.BRACKET);
        currentNSQLToken.setTokenContent(")");

        this.NSQLTokens.add(currentNSQLToken);
    }

    private void addEOF() {
        NSQLToken currentNSQLToken = new NSQLToken();
        currentNSQLToken.setTokenType(TokenType.EOF);
        currentNSQLToken.setTokenContent(";");

        this.NSQLTokens.add(currentNSQLToken);
    }

    /**
     * Tokenisiert den angegebenen Query.
     * @param query
     */
    public void tokenize(String query) {

        this.NSQLTokens.clear();
        this.nsqlQuery = query;

        String currentSubstring = "";
        boolean stringFound = false;

        for (int i = 0; i < query.length(); i++) {
            char currentChar = query.charAt(i);
            switch (currentChar) {
                case '.':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addContextual(currentSubstring);
                    this.addDot();
                    currentSubstring = "";
                    break;
                case ':':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addColon();
                    currentSubstring = "";
                    break;
                case '#':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addPound();
                    currentSubstring = "";
                    break;
                case '!':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addExclamation();
                    currentSubstring = "";
                    break;
                case '$':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addDollar();
                    currentSubstring = "";
                    break;
                case ',':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addContextual(currentSubstring);
                    this.addComma();
                    currentSubstring = "";
                    break;
                case '(':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addContextual(currentSubstring);
                    this.addBracketOpen();
                    currentSubstring = "";
                    break;
                case ')':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addContextual(currentSubstring);
                    this.addBracketClosed();
                    currentSubstring = "";
                    break;
                case ';':
                case '|':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addContextual(currentSubstring);
                    this.addEOF();
                    currentSubstring = "";
                    break;
                case ' ':
                    if (stringFound) {
                        currentSubstring += currentChar;
                        continue;
                    }
                    this.addContextual(currentSubstring);
                    currentSubstring = "";
                    break;

                case '\'':
                    currentSubstring += currentChar;
                    stringFound = !stringFound;
                    break;

                default:
                    currentSubstring += currentChar;
                    break;
            }

        }
    }


    private static NSQLTokenizer nsqlTokenizer;

    public static NSQLTokenizer getDefault() {
        if (nsqlTokenizer == null) nsqlTokenizer = new NSQLTokenizer();
        return nsqlTokenizer;
    }


}
