package Conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private final String URL = "jdbc:mysql://localhost/db_mundial";
    private final String USER = "root";
    private final String PASSWORD = "";
    private final String DRIVER = "com.mysql.jdbc.Driver";
    protected Connection conn = null;

    protected Connection getConexion() {

        try {

            Class.forName(DRIVER);
            conn = DriverManager.getConnection(URL, USER, PASSWORD);

            System.out.println("Se Conecto a la base de datos");

        } catch (SQLException ex) {

            System.out.println("Error de conexión a la base de datos");
            getErrorSQL(ex);
            conn = null;

        } catch (ClassNotFoundException ex) {

            System.out.println("Error al cargar Driver " + ex);
            conn = null;

        }
        return conn;
    }

    protected void desconectar() {
        try {
            if (conn != null) {
                conn.close();
            }
            System.out.println("Se cerro correctamente la conexión");

        } catch (SQLException ex) {
            System.out.println("Error al cerrar conexión");
            getErrorSQL(ex);
        }
    }

    protected void getErrorSQL(SQLException error) {
        System.out.println("Error mensaje " + error.getMessage());
        System.out.println("Codigo " + error.getErrorCode());
        System.out.println("Estado " + error.getSQLState());
    }
}
