package org.ns.npersistence;

/**
 * Token eines Tokenizers
 */
class NSQLToken {
    private TokenType tokenType;
    private String tokenContent;


    public void setTokenType(TokenType tokenType) {
        this.tokenType = tokenType;
    }

    public void setTokenContent(String tokenContent) {
        this.tokenContent = tokenContent;
    }

    public TokenType getTokenType() {
        return tokenType;
    }

    public String getTokenContent() {
        return tokenContent;
    }

    @Override
    public String toString() {
        return "[" + tokenType + "] " + this.getTokenContent();
    }
}
