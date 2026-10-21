package campus.db;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DBConnection - Handles MySQL JDBC Database Connection
 * Loads credentials from db.properties file.
 */
public class DBConnection {
    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            Properties props = new Properties();
            InputStream in = null;
            try {
                in = new FileInputStream("db.properties");
                props.load(in);
            } catch (Exception e) {
                // Fallback to defaults if properties file not found
                props.setProperty("db.url", "jdbc:mysql://localhost:3306/campus_resource_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
                props.setProperty("db.user", "root");
                props.setProperty("db.password", "jojo123");
            } finally {
                if (in != null) in.close();
            }

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            // Register MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (Exception e) {
            System.err.println("Database Driver Init Error: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }
}
