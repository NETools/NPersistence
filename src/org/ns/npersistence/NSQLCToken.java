package org.ns.npersistence;

import org.ns.npersistence.annotations.IterableAttribute;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Zu kompilierender Token.
 */
class NSQLCToken {
    public static int IDENTIFIER_INDEX = 0;

    public enum NSQLCTokenType {
        PK_KEYWORD, CLASS_NAME, FIELD_NAME
    }

    private NSQLCompiler nsqlCompiler;
    private NSQLCTokenType nsqlcTokenType;
    private ArrayList<NSQLToken> identifierTokens;

    private NSQLCProviderClassResult classResult;

    private String primaryKeyName;

    private String qualifiedNameSpace;
    private String sqlPrimaryKeyName;

    private String identifier;
    private String textualPayload;

    private ArrayList<String> subIdentifiers;

    public NSQLCToken(NSQLCompiler nsqlCompiler, NSQLCTokenType nsqlcTokenType) {

        this.nsqlCompiler = nsqlCompiler;
        this.nsqlcTokenType = nsqlcTokenType;
        this.identifierTokens = new ArrayList<>();
        this.textualPayload = "";
        this.identifier = "NON_FIELD";

        this.subIdentifiers = new ArrayList<>();
    }

    public NSQLCompiler getNsqlCompiler() {
        return nsqlCompiler;
    }

    public NSQLCTokenType getNsqlcTokenType() {
        return nsqlcTokenType;
    }

    public void append(NSQLToken nsqlToken) {
        this.identifierTokens.add(nsqlToken);
    }

    public void setTextualPayload(String payload) {
        this.textualPayload = payload;
    }

    public String getTextualPayload() {
        return textualPayload;
    }

    public String getIdentifier() {
        return identifier;
    }

    public ArrayList<String> getSubIdentifiers() {
        return subIdentifiers;
    }

    public ArrayList<NSQLToken> getIdentifierTokens() {
        return identifierTokens;
    }

    public NSQLCProviderClassResult getClassResult() {
        return classResult;
    }

    public String getPrimaryKeyName() {
        return primaryKeyName;
    }

    public String getSqlPrimaryKeyName() {
        return sqlPrimaryKeyName;
    }

    public String getQualifiedNameSpace() {
        return qualifiedNameSpace;
    }

    public void setPrimaryKeyName(String primaryKeyName) {
        this.primaryKeyName = primaryKeyName;
    }

    private void addClassDefinitions(Class<?> initialClass) throws Exception {
        Class<?> currentClass = initialClass;

        String fqns = this.getNsqlCompiler().getCurrentClassToken().getTextualPayload();
        String qns = "^new";

        for (NSQLToken token : this.getIdentifierTokens()) {
            switch (token.getTokenType()) {
                case DOT:
                    fqns += ".";
                    qns += ".";
                    break;
                case CONTEXTUAL:

                    Field currentField = currentClass.getDeclaredField(token.getTokenContent());
                    currentField.setAccessible(true);

                    fqns += token.getTokenContent();
                    qns += token.getTokenContent();

                    if (currentField.isAnnotationPresent(IterableAttribute.class)) {
                        var fieldSignature = currentField
                                .getGenericType()
                                .getTypeName()
                                .substring(currentField.getGenericType().getTypeName().indexOf("<") + 1)
                                .replace(">", "");

                        currentClass = Class.forName(fieldSignature);
                    } else currentClass = currentField.getType();

                    break;
            }
        }


        var primaryKeySql =
                NSQLCProvider.getInstance(this).parseClass(currentClass.getName()).getSqlPrimaryKeyName();


        this.getNsqlCompiler().appendQuery(", " + this.getNsqlCompiler().getSqlFieldIdentifier().get(fqns) + "." + primaryKeySql);

        this.getNsqlCompiler().getSqlFieldIdentifierFull().put(qns,
                this.getNsqlCompiler().getSqlFieldIdentifier().get(fqns) + "." + primaryKeySql);

        NPersistenceSelectPolicy.addClass(currentClass);

        this.qualifiedNameSpace = this.getNsqlCompiler().getSqlFieldIdentifier().get(fqns);
        this.sqlPrimaryKeyName = primaryKeySql;

    }

    public void sqlize() throws Exception {
        switch (this.getNsqlcTokenType()) {
            case PK_KEYWORD:
                /* ADD QUERY: SELECT [PRIMARY_KEY] */
                if (!this.getNsqlCompiler().isLoadedKeySelector()) {
                    this.getNsqlCompiler().appendQuery(this.getNsqlCompiler()
                            .getCurrentClassToken()
                            .getTextualPayload() + "." + this
                            .getNsqlCompiler()
                            .getCurrentClassToken()
                            .getClassResult()
                            .getSqlPrimaryKeyName());


                    this.getNsqlCompiler().getSqlFieldIdentifierFull()
                            .put(this.getNsqlCompiler().getCurrentClassToken().getTextualPayload() + "." +
                                            this.getNsqlCompiler().getCurrentClassToken().getClassResult().getSqlPrimaryKeyName().replace("pk_", ""),
                                    this.getNsqlCompiler().getCurrentClassToken().getTextualPayload() + "." +
                                            this.getNsqlCompiler().getCurrentClassToken().getClassResult().getSqlPrimaryKeyName());


                    this.getNsqlCompiler().getSqlFieldIdentifierFull()
                            .put("^new", this.getNsqlCompiler().getCurrentClassToken().getTextualPayload() + "." +
                                    this.getNsqlCompiler().getCurrentClassToken().getClassResult().getSqlPrimaryKeyName());

                }

                if (this.getIdentifierTokens().size() > 0)
                    addClassDefinitions(this.getNsqlCompiler().getCurrentLoadedClass());


                break;

            case CLASS_NAME:
                /* ADD QUERY: FROM [CLASSNAME] [CLASS_IDENTIFIER] */
                this.generateFieldIdentifier(0);
                this.classResult = NSQLCProvider.getInstance(this).parseClass(this.getIdentifier());
                this.getNsqlCompiler().setCurrentLoadedClass(Class.forName(this.getIdentifier()));

                break;

            case FIELD_NAME:
                /* ADD QUERY: JOIN [FIELD_NAME] [IDENTIFIER_N] ON [IDENTIFIER_(N-1)].[FK] = [IDENTIFIER_N]
                .[PK] JOIN [...] */
                NSQLCProvider nsqlcProvider = NSQLCProvider.getInstance(this);
                nsqlcProvider.parseField();
                nsqlcProvider.createFieldJoins();

                /* GENERATE JOIN QUERIES; ADD ONLY IF NOT EXISTS CURRENTLY! (PERFOMANCE BOOST!!!) */

                break;
        }

    }

    public void generateFieldIdentifier(int mode) {
        this.identifier = "";
        for (int i = 0; i < this.getIdentifierTokens().size() - mode * 2; i++) {
            this.identifier += this.getIdentifierTokens().get(i).getTokenContent();
            if (i % 2 == 0) {
                this.subIdentifiers.add(this.identifier);
            }
        }
    }

    @Override
    public String toString() {
        String s =
                "[" + this.getNsqlcTokenType() + ", \nIDENTIFIER = " + identifier + "] " + this.textualPayload +
                        "\n\t\tDATA INCLUDED:";

        for (var token : this.identifierTokens) {
            s += "\n\t\t" + token;
        }

        s += "\nPK: " + this.primaryKeyName;

        return s;

    }

}
