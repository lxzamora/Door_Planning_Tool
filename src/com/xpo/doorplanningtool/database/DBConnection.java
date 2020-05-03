package com.xpo.doorplanningtool.database;

import java.sql.*;

public class DBConnection {

    private Connection connection;
    private String url;
    private String username;
    private String password;

    public DBConnection() {}

    public DBConnection(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public void createConnection()
    {
        try
        {
            Class.forName("org.netezza.Driver").newInstance();
            this.connection = DriverManager.getConnection(this.url, this.username, this.password);
            this.connection.setAutoCommit(false);
        }
        catch (Exception e)
        {
            System.out.println
                    ("Error, connection not made.\n");
        }
    }

    public void close() throws SQLException {
        this.connection.close();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public ResultSet executeQuery(String query) throws SQLException {
        Statement stmt = this.connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        return rs;
    }

    public void executeUpdate(String update) throws SQLException {
        Statement stmt = this.connection.createStatement();
        stmt.executeUpdate(update);
    }

}
