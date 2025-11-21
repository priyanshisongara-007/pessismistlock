import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBManager {
    private static final String URL = "jdbc:mysql://10.65.134.76:3306/airline_booking?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root"; // your db user
    private static final String PASS = "****"; // your db password


    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return DriverManager.getConnection(URL, USER, PASS);
    }
}
