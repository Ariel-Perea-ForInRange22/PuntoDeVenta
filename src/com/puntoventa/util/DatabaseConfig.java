package com.puntoventa.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConfig {
    private static final String URL = "jdbc:sqlserver://localhost;databaseName=PuntoVenta;encrypt=false;trustServerCertificate=true";
    private static final String USER = "sa";
    private static final String PASSWORD = "123456";

    public static Connection getConnection() throws SQLException {
        try {
            // Cargar explícitamente el driver de SQL Server
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("Driver SQL Server cargado correctamente");
            
            System.out.println("Intentando conectar a la base de datos...");
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Conexión exitosa!");
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("Error al cargar el driver de SQL Server: " + e.getMessage());
            throw new SQLException("No se pudo cargar el driver de SQL Server", e);
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            throw e;
        }
    }
}