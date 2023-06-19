package org.ns.npersistence;

import org.ns.npersistence.annotations.CompositionTable;
import org.ns.npersistence.annotations.IterableAttribute;
import org.ns.npersistence.annotations.PrimaryKey;
import org.ns.npersistence.annotations.Table;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * Hilsfklasse um NSQLC-Tokens zu verarbeiten
 */
class NSQLCProvider {

    private NSQLCToken nsqlcToken;
    private StringBuilder currentCompositionTableName;
    private StringBuilder currentCompositionIdentifier;

    private ArrayList<NSQLCProviderClassResult> classResults;
    private NSQLCProviderClassResult currentClassResult = null;


    public static NSQLCProvider getInstance(NSQLCToken nsqlcToken) {
        return new NSQLCProvider(nsqlcToken);
    }

    public NSQLCProvider(NSQLCToken nsqlcToken) {
        this.nsqlcToken = nsqlcToken;
    }


    public NSQLCProviderClassResult parseClass(String classIdentifier) throws Exception {
        NSQLCProviderClassResult result = new NSQLCProviderClassResult();

        Class<?> currentClass = Class.forName(classIdentifier);
        SearchResult<Field> primaryKeyField = GeneralToolKit.isAnnotationPresent(currentClass,
                PrimaryKey.class);


        result.setSqlPrimaryKeyName("pk_" + primaryKeyField.getField().getName());
        result.setSqlTableName(classIdentifier.replace(".", "_"));

        return result;
    }


    public void parseField() throws Exception {
        this.classResults = new ArrayList<>();

        currentCompositionTableName = new StringBuilder();
        currentCompositionIdentifier = new StringBuilder();

        currentCompositionTableName
                .append(this.nsqlcToken.getNsqlCompiler().getCurrentClassToken().getClassResult().getSqlTableName());


        putClassResult(currentCompositionTableName + "",
                this.nsqlcToken.getNsqlCompiler().getCurrentClassToken().getTextualPayload(),
                "",
                "");


        currentCompositionIdentifier
                .append(this.nsqlcToken.getNsqlCompiler().getCurrentClassToken().getTextualPayload())
                .append(".");

        parseFieldRecursively(this.nsqlcToken.getNsqlCompiler().getCurrentLoadedClass(), 2);

    }

    protected void putClassResult(String sqlTableName, String sqlIdentifier, String sqlPrimaryKeyName,
                                  String payload) {
        NSQLCProviderClassResult classResult = new NSQLCProviderClassResult();

        classResult.setSqlTableName(sqlTableName);
        classResult.setSqlIdentifier(sqlIdentifier);
        classResult.setSqlPrimaryKeyName(sqlPrimaryKeyName);
        classResult.setTextualPayload(payload);

        this.classResults.add(classResult);
    }

    private boolean parsePrimitiveIterable(Class<?> currentClass) throws Exception {
        SearchResult<Field> primaryKeyField =
                GeneralToolKit.isAnnotationPresent(currentClass, PrimaryKey.class);

        this.classResults.get(this.classResults.size() - 1)
                .setTextualPayload("pk_" + primaryKeyField.getField().getName());


        Field tfield = currentClass
                .getDeclaredField(this.nsqlcToken.getIdentifierTokens().get(this.nsqlcToken.getIdentifierTokens().size() - 1)
                        .getTokenContent());


        if (tfield.isAnnotationPresent(IterableAttribute.class)) {
            NSQLCIterableProvider iterableProvider =
                    new NSQLCIterableProvider(
                            this,
                            currentClass,
                            tfield,
                            tfield.getDeclaredAnnotations()[0]);


            iterableProvider.getResult(this.currentCompositionTableName);

            return true;
        }

        return false;
    }

    private void parseFieldRecursively(Class<?> currentClass, int pointer) throws Exception {

        if (pointer >= this.nsqlcToken.getIdentifierTokens().size() - 1) {
            if (parsePrimitiveIterable(currentClass)) {

                this.nsqlcToken.getNsqlCompiler().getSqlFieldIdentifierFull().put("!" + this.nsqlcToken.getIdentifier() + "."
                                + this.nsqlcToken.getIdentifierTokens().get(this.nsqlcToken.getIdentifierTokens().size() - 1).getTokenContent(),
                        this.classResults.get(this.classResults.size() - 1).getSqlIdentifier() + "." +
                                this.classResults.get(this.classResults.size() - 1).getTextualPayload());
            }
            return;
        }

        var token = this.nsqlcToken.getIdentifierTokens().get(pointer);
        switch (token.getTokenType()) {
            case DOT:


                if (this.currentClassResult == null) {
                    NDebugOutputHandler.getDefault().handle(150);
                    return;
                }

                this.currentClassResult.setSqlTableName(this.currentCompositionTableName + "");
                this.currentClassResult.setSqlIdentifier(this.nsqlcToken.getNsqlCompiler()
                        .getSqlFieldIdentifier().get(currentCompositionIdentifier + ""));


                this.classResults.add(currentClassResult);

                this.currentCompositionIdentifier
                        .append(".");
                parseFieldRecursively(currentClass, ++pointer);
                break;

            case CONTEXTUAL:
                /* field */

                Field currentInspectedFild = currentClass.getDeclaredField(this
                        .nsqlcToken
                        .getIdentifierTokens()
                        .get(pointer)
                        .getTokenContent());

                currentInspectedFild.setAccessible(true);

                SearchResult<Field> primaryKeyField
                        = GeneralToolKit.isAnnotationPresent(currentClass, PrimaryKey.class);

                this.nsqlcToken.setPrimaryKeyName(primaryKeyField.getField().getName());

                boolean dontChange = false;
                if (currentInspectedFild.isAnnotationPresent(IterableAttribute.class)) {


                    this.classResults.get(this.classResults.size() - 1)
                            .setTextualPayload("pk_" + primaryKeyField.getField().getName());

                    NSQLCIterableProvider iterableProvider =
                            new NSQLCIterableProvider(
                                    this,
                                    currentClass,
                                    currentInspectedFild,
                                    currentInspectedFild.getDeclaredAnnotations()[0]);


                    iterableProvider.getResult(this.currentCompositionTableName);
                    currentClass = iterableProvider.getSecondClass();

                    dontChange = true;


                } else
                    currentClass = currentInspectedFild.getType();

                primaryKeyField
                        = GeneralToolKit.isAnnotationPresent(currentClass, PrimaryKey.class);

                this.currentClassResult = new NSQLCProviderClassResult();
                this.currentClassResult.setSqlPrimaryKeyName("pk_" +
                        primaryKeyField.getField().getName());

                /* CHANGE PREV PAYLOAD */

                if (!dontChange)
                    this.classResults.get(this.classResults.size() - 1)
                            .setTextualPayload("fk_" + currentInspectedFild.getName() + "_" +
                                    primaryKeyField.getField().getName());


                if (currentClass.isAnnotationPresent(CompositionTable.class))
                    this.currentCompositionTableName
                            .append("_COMPOSITION_")
                            .append(currentInspectedFild.getName());
                else if (currentClass.isAnnotationPresent(Table.class)) {
                    this.currentCompositionTableName = new StringBuilder();
                    this.currentCompositionTableName
                            .append(currentClass.getName().replace(".", "_"));
                }
                this.currentCompositionIdentifier
                        .append(currentInspectedFild.getName());


                parseFieldRecursively(currentClass, ++pointer);
                break;
        }
    }

    public void createFieldJoins() {

        StringBuilder currentSQLBuilder = new StringBuilder();

        for (int i = 1; i < this.classResults.size(); i++) {

            NSQLCProviderClassResult prev
                    = this.classResults.get(i - 1);
            NSQLCProviderClassResult current
                    = this.classResults.get(i);

            boolean prevUsed =
                    this.nsqlcToken.getNsqlCompiler().getUsedJoinIdentifiers().contains(prev.getSqlIdentifier());
            boolean currentUsed =
                    this.nsqlcToken.getNsqlCompiler().getUsedJoinIdentifiers().contains(current.getSqlIdentifier());


            currentSQLBuilder
                    = currentSQLBuilder
                    .append(prevUsed ? "" : GeneralToolKit.sha1(prev.getSqlTableName()) + " ")
                    .append(prevUsed ? "" : prev.getSqlIdentifier())
                    .append(currentUsed ? "" : "JOIN ")
                    .append(currentUsed ? "" : GeneralToolKit.sha1(current.getSqlTableName()) + " ")
                    .append(currentUsed ? "" : current.getSqlIdentifier())
                    .append(currentUsed ? "" : " ON ")
                    .append(currentUsed ? "" : prev.getSqlIdentifier() + "." + prev.getTextualPayload())
                    .append(currentUsed ? "" : " = ")
                    .append(currentUsed ? "" :
                            current.getSqlIdentifier() + "." + current.getSqlPrimaryKeyName() + " ");

            this.nsqlcToken.getNsqlCompiler()
                    .getUsedJoinIdentifiers()
                    .add(current.getSqlIdentifier());

            this.nsqlcToken.getNsqlCompiler()
                    .getUsedJoinIdentifiers()
                    .add(prev.getSqlIdentifier());

        }

        if (currentSQLBuilder.length() == 0)
            return;

        this.nsqlcToken.getNsqlCompiler().appendQuery(currentSQLBuilder + "\n");

    }

}
