package com.airline.tools;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class HiKariCPHandler {
    static HikariConfig config = new HikariConfig("/hikaricp.properties");
    static HikariDataSource ds = new HikariDataSource(config);

    public static Connection getConn() throws SQLException {

        return ds.getConnection();
    }
}
