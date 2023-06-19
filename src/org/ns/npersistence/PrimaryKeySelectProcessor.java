package org.ns.npersistence;

class PrimaryKeySelectProcessor {

    public void process(NTableSelect tableSelect) throws Exception {

        if (tableSelect.getPrimaryKey() == null || tableSelect.getPrimaryKeyField().getField() == null) {
            NDebugOutputHandler.getDefault().handle(50);
            return;
        }


        tableSelect.getPrimaryKeyField().getField().set(tableSelect.getCurrentInstanceOfClass(),
                tableSelect.getPrimaryKey());


        tableSelect.setResultSet(
                SQLManager
                        .getDefault()
                        .select(NPSFluentSQLing.selectTable().begin().all().addTable(tableSelect.getTableSqlName())
                                .where("pk_" + tableSelect.getPrimaryKeyField().getField().getName(),
                                        tableSelect.getPrimaryKey()).end()));

        if (NPersistenceSelectPolicy.isRequired())
            NPersistenceSelectPolicy.addObject(
                    tableSelect.getCurrentClassDefinition(),
                    tableSelect.getCurrentInstanceOfClass(),
                    tableSelect.getPrimaryKey());
    }
}
