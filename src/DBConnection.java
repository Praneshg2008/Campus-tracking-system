import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection - Manages the JDBC connection to the MySQL database.
 * Reads database configuration from db.properties or environment variables.
 */
public class DBConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            try (FileInputStream in = new FileInputStream("db.properties")) {
                props.load(in);
            } catch (IOException e) {
                System.err.println("Warning: Could not read db.properties file. Checking environment variables.");
            }

            url = System.getenv("DB_URL") != null ? System.getenv("DB_URL") : props.getProperty("db.url");
            user = System.getenv("DB_USER") != null ? System.getenv("DB_USER") : props.getProperty("db.user");
            password = System.getenv("DB_PASSWORD") != null ? System.getenv("DB_PASSWORD") : props.getProperty("db.password");

            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        if (url == null || user == null || password == null) {
            throw new SQLException("Database credentials are not configured. Please check db.properties.");
        }
        return DriverManager.getConnection(url, user, password);
    }
}
