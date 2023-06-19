package org.ns.npersistence;

import java.util.HashMap;

/**
 * Klasse die Mapper zur Verfügung stellt, um gewisse Symbole Tokentypen zuzupordnen
 */
class NSQLTokenMapping {

    private static HashMap<String, TokenType> tokenMapping = new HashMap<>() {
        {
            put(".", TokenType.DOT);
			put("%new", TokenType.NEW);
            put(":", TokenType.OBJECT);
            put("#", TokenType.ITERABLE);
            put("!", TokenType.FIELD);
            put("$", TokenType.CLASS);

            put("SELECT", TokenType.SQL);
            put("FROM", TokenType.SQL);
            put("WHERE", TokenType.SQL);
            put("OR", TokenType.SQL_OPERATOR);
            put("AND", TokenType.SQL_OPERATOR);
            put("ORDER", TokenType.SQL_OPERATOR);
            put("BY", TokenType.SQL_OPERATOR);
            put("THEN", TokenType.SQL_OPERATOR);
            put("ASC", TokenType.SQL_OPERATOR);
            put("DESC", TokenType.SQL_OPERATOR);

            put("LIKE", TokenType.SQL_COMPARISION);
            put("=", TokenType.SQL_COMPARISION);
            put(">", TokenType.SQL_COMPARISION);
            put("<", TokenType.SQL_COMPARISION);
            put("<=", TokenType.SQL_COMPARISION);
            put(">=", TokenType.SQL_COMPARISION);
            put("<>", TokenType.SQL_COMPARISION);
            put("IS", TokenType.SQL_COMPARISION);
            put("NULL", TokenType.SQL_COMPARISION);

            put(";", TokenType.EOF);
        }
    };

    public static TokenType getTokenType(String currentSubstring) {
        if(tokenMapping.containsKey(currentSubstring))
            return tokenMapping.get(currentSubstring);
        else return TokenType.CONTEXTUAL;
    }
}
