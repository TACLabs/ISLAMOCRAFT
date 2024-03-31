package islamocraft.islamocraft.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Databaise {
    private static final String URL = "jdbc:mysql://localhost:3306/islamocraft";
    private static final String USER = "root";
    private static final String PASSWORD = "root";
    private static Connection connection = null;

    private Databaise() {
        // Constructeur privé pour empêcher l'instanciation directe depuis l'extérieur.
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

}
