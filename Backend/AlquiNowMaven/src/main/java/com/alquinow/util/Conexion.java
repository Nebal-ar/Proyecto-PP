package com.alquinow.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase utilitaria para obtener conexiones a la base de datos MySQL "AlquiNow".
 *
 * IMPORTANTE: ajustá USUARIO y PASSWORD a los de tu MySQL Workbench local.
 * El driver "com.mysql.cj.jdbc.Driver" viene en el .jar mysql-connector-j
 * que tenés que agregar en la carpeta /lib y en las librerías del proyecto.
 */
public class Conexion {

    // --- Parámetros de conexión (EDITAR según tu instalación) ---
    private static final String URL =
            "jdbc:mysql://localhost:3306/AlquiNow"
            + "?useSSL=false&serverTimezone=America/Argentina/Buenos_Aires"
            + "&allowPublicKeyRetrieval=true";
    private static final String USUARIO = "root";
    private static final String PASSWORD = "root"; // <-- poné tu contraseña

    /**
     * Devuelve una conexión nueva a la base de datos.
     * Quien la usa es responsable de cerrarla (try-with-resources).
     */
    public static Connection getConexion() throws SQLException {
        try {
            // Registramos el driver (a partir de MySQL Connector 8 no es
            // estrictamente necesario, pero lo dejamos por claridad).
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "No se encontró el driver de MySQL. "
                + "¿Agregaste mysql-connector-j al classpath?", e);
        }
        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
}
