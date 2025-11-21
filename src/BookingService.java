import java.sql.*;

public class BookingService {

    // Change this to your actual DB connection utility
    private Connection getConnection() throws SQLException {
        Connection conn = DBManager.getConnection();
        conn.setAutoCommit(false);
        conn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        return conn;
    }

    //some seats will be booked in random order
    public boolean bookSeatForUpdate(int userId) {
        String selectSql = "SELECT id FROM seats WHERE is_booked = 0 LIMIT 1 FOR UPDATE";
        String updateSql = "UPDATE seats SET is_booked = TRUE WHERE id = ?";
        return bookSeat(userId, selectSql, updateSql, "FOR UPDATE");
    }

    //some seatswill be booked and there can be race condition happening
    public boolean bookSeatForUpdateSkipLocked(int userId) {
        // Requires MySQL 8+ for SKIP LOCKED support
        String selectSql = "SELECT id FROM seats WHERE is_booked = 0 LIMIT 1 FOR UPDATE SKIP LOCKED";
        String updateSql = "UPDATE seats SET is_booked = TRUE WHERE id = ?";
        return bookSeat(userId, selectSql, updateSql, "FOR UPDATE SKIP LOCKED");
    }
 //all the seats will be booked in random order
    public boolean bookSeatNoLock(int userId) {
        String selectSql = "SELECT id FROM seats WHERE is_booked = 0 LIMIT 1";
        String updateSql = "UPDATE seats SET is_booked = TRUE WHERE id = ?";
        return bookSeat(userId, selectSql, updateSql, "NO LOCK");
    }

    private boolean bookSeat(int userId, String selectSql, String updateSql, String lockType) {
        try (Connection conn = getConnection();
             PreparedStatement selectStmt = conn.prepareStatement(selectSql);
             PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {


            ResultSet rsCount = selectStmt.executeQuery("SELECT COUNT(*) FROM seats WHERE is_booked = 0");
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                int seatId = rs.getInt("id");
                updateStmt.setInt(1, seatId);
                int updateCount = updateStmt.executeUpdate();

                if (updateCount == 1) {
                    conn.commit();
                    System.out.printf("User %d booked seat %d using %s locking%n", userId, seatId, lockType);
                    return true;
                } else {
                    conn.rollback();
                }
            } else {
                conn.rollback();
                System.out.printf("User %d: No seats available for %s locking%n", userId, lockType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // If exception occurs, transaction will be rolled back on close or can roll back explicitly
        }
        return false;
    }
}
