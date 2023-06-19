package org.ns.npersistence;

import java.sql.PreparedStatement;
import java.sql.SQLException;

class NUpdateAtomic {
    private StringBuilder sqlQueryBuilder;

    private NEntityUpdateHandlerProperty entityUpdateHandlerProperty;

    public NEntityUpdateHandlerProperty getEntityUpdateHandlerProperty() {
        return entityUpdateHandlerProperty;
    }

    public String getSqlQuery() {
        return this.sqlQueryBuilder.toString();
    }

    public NUpdateAtomic(NEntityUpdateHandlerProperty entityUpdateHandlerProperty) {
        this.sqlQueryBuilder = new StringBuilder();
        this.entityUpdateHandlerProperty = entityUpdateHandlerProperty;
    }

    public NUpdateAtomic buildQuery(String columnName) {
        this.sqlQueryBuilder =
                this.sqlQueryBuilder
                        .append("UPDATE ")
                        .append(GeneralToolKit.sha1(this.getEntityUpdateHandlerProperty().getInstanceSqlName()))
                        .append(" SET ")
                        .append(columnName)
                        .append(" = ")
                        .append("?")
                        .append(" WHERE ")
                        .append(this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyName())
                        .append(" = ")
                        .append("?");

        return this;
    }

    public void write(Object data) throws SQLException {
        PreparedStatement preparedStatement =
                SQLManager.getDefault().getConnection().prepareStatement(this.getSqlQuery());
        preparedStatement.setObject(2, this.getEntityUpdateHandlerProperty().getInstancePrimaryKeyValue());
        preparedStatement.setObject(1, data);
        preparedStatement.executeUpdate();
    }


    public static NUpdateAtomic getInstance(NEntityUpdateHandlerProperty entityUpdateHandlerProperty) {
        return new NUpdateAtomic(entityUpdateHandlerProperty);
    }

}
