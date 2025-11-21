import java.sql.*;

//No multithreading all the seats will be booked
public class SequentialBookingPessimisticLocking {

    private static final String JDBC_URL = "jdbc:mysql://10.65.134.76:3306/airline_booking?useSSL=false&serverTimezone=UTC";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    public static void main(String[] args) {
        SequentialBookingPessimisticLocking booking = new SequentialBookingPessimisticLocking();

        for (int userId = 1; userId <= 120; userId++) {
            boolean success = booking.bookSeat(userId);
            if (!success) {
                System.out.println("User " + userId + ": No available seats to book.");
                break; // All seats booked, stop trying
            }
        }
    }

    public boolean bookSeat(int userId) {
        Connection conn = null;
        try {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            conn = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
            conn.setAutoCommit(false);  // Start transaction

            String selectSQL = "SELECT id FROM seats WHERE is_booked = FALSE LIMIT 1 FOR UPDATE";
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSQL)) {
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    int seatId = rs.getInt("id");

                    // Book the seat
                    String updateSQL = "UPDATE seats SET is_booked = TRUE WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSQL)) {
                        updateStmt.setInt(1, seatId);
                        int updatedRows = updateStmt.executeUpdate();

                        if (updatedRows == 1) {
                            conn.commit();
                            System.out.println("User " + userId + " successfully booked seat " + seatId);
                            return true;
                        } else {
                            conn.rollback();
                            System.out.println("User " + userId + " failed to book the seat.");
                        }
                    }
                } else {
                    conn.rollback();
                    return false; // No seats available
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        } finally {
            try {
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}

