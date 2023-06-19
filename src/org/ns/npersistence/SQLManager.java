package org.ns.npersistence;

import java.lang.reflect.Array;
import java.sql.*;
import java.util.ArrayList;

/**
 * Allgemeine Klasse die SQLite-Schnittstellen zur Verfügung stellt.
 */

class SQLManager {
    private Connection connection;

    private ArrayList<String> currentTables;

    public void connectLocal(String root, String dbName) throws SQLException {
        connection = DriverManager.getConnection("jdbc:sqlite:" + root + "/" + dbName);
        currentTables = null;
    }

    public void connectRemote(NPersistenceSession.SQLDriver driver, String host, String schema,
                              String userName, String userPassword) throws SQLException {

        connection =
                DriverManager.getConnection("jdbc:" + driver.toString().toLowerCase() + "://" + host + "/" + schema + "?user=" + userName + "&password=" + userPassword);
        currentTables = null;
    }

    public Connection getConnection() {
        return connection;
    }

    public void createTable(String query) {
        Statement stmt;
        try {
            stmt = connection.createStatement();
            stmt.executeUpdate(query);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public void insert(String sql, Object... values) throws SQLException {
        PreparedStatement prepStmt = connection.prepareStatement(sql);
        for (int i = 0; i < values.length; i++) {
            Object currentValue = values[i];
            if (currentValue.getClass().isArray())
                prepStmt.setBytes(i + 1, (byte[]) currentValue);
            else
                prepStmt.setObject(i + 1, values[i]);
        }
        prepStmt.executeUpdate();
    }

    public ResultSet select(String selectQuery) throws SQLException {
        Statement statement = this.getConnection().createStatement();
        ResultSet resultSet = null;

        try {
            resultSet = statement.executeQuery(selectQuery);
        } catch (Exception ex) {

        }

        return resultSet;

    }

    public ArrayList<String> getCurrentTables() throws SQLException {

        if (currentTables == null) {
            currentTables = new ArrayList<>();
            DatabaseMetaData md = connection.getMetaData();
            ResultSet rs = md.getTables(null, null, "%", null);
            while (rs.next()) {
                currentTables.add(rs.getString(3));
            }
        }

        return currentTables;
    }

    public ArrayList<String> getCurrentTableAt(String root) throws SQLException {
        ArrayList<String> filteredTables = new ArrayList<>();
        for (String table : this.getCurrentTables()) {
            if (table.contains(root)) filteredTables.add(table);
        }

        return filteredTables;
    }

    private static SQLManager mSQLiteManager;

    public static SQLManager getDefault() {
        if (mSQLiteManager == null) mSQLiteManager = new SQLManager();
        return mSQLiteManager;
    }


}
