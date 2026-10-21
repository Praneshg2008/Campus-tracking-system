package campus.dao;

import campus.db.DBConnection;
import campus.models.CheckoutTransaction;
import campus.models.Resource;

import java.sql.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckoutDAO - Handles borrowing, returns, and DBMS/OOP late fee calculation.
 */
public class CheckoutDAO {
    private ResourceDAO resourceDAO = new ResourceDAO();

    /**
     * Issues an asset to a student.
     * Transactional: Updates resource status and inserts checkout row.
     */
    public boolean issueCheckout(String resourceId, String studentId, String studentName, int durationDays) {
        Resource res = resourceDAO.getResourceById(resourceId);
        if (res == null) {
            System.out.println("Error: Resource ID not found.");
            return false;
        }
        if (!res.isAvailable()) {
            System.out.println("Error: Resource is currently " + res.getStatus() + " and cannot be checked out.");
            return false;
        }

        LocalDate checkoutDate = LocalDate.now();
        LocalDate dueDate = checkoutDate.plusDays(durationDays);

        String insertSql = "INSERT INTO checkouts (resource_id, student_id, student_name, checkout_date, due_date, checkout_status) VALUES (?, ?, ?, ?, ?, 'ACTIVE')";
        String updateResSql = "UPDATE resources SET status = 'CHECKED_OUT' WHERE resource_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Transaction begin

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setString(1, resourceId);
                psInsert.setString(2, studentId);
                psInsert.setString(3, studentName);
                psInsert.setDate(4, Date.valueOf(checkoutDate));
                psInsert.setDate(5, Date.valueOf(dueDate));
                psInsert.executeUpdate();
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateResSql)) {
                psUpdate.setString(1, resourceId);
                psUpdate.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Transaction failed while checking out: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return false;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Return a checked-out resource, calculating late fees using both SQL datediff and OOP logic.
     */
    public double returnCheckout(int checkoutId, LocalDate returnDate) {
        String querySql = "SELECT c.*, r.daily_late_fee, r.category, DATEDIFF(?, c.due_date) as sql_overdue_days " +
                          "FROM checkouts c JOIN resources r ON c.resource_id = r.resource_id " +
                          "WHERE c.checkout_id = ? AND c.checkout_status = 'ACTIVE'";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String resourceId = null;
            LocalDate dueDate = null;
            long sqlOverdueDays = 0;

            try (PreparedStatement psQuery = conn.prepareStatement(querySql)) {
                psQuery.setDate(1, Date.valueOf(returnDate));
                psQuery.setInt(2, checkoutId);
                try (ResultSet rs = psQuery.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Error: Active checkout with ID #" + checkoutId + " not found.");
                        return -1.0;
                    }
                    resourceId = rs.getString("resource_id");
                    dueDate = rs.getDate("due_date").toLocalDate();
                    sqlOverdueDays = rs.getLong("sql_overdue_days");
                }
            }

            // OOP late fee calculation based on polymorphic asset type
            Resource res = resourceDAO.getResourceById(resourceId);
            long daysOverdue = ChronoUnit.DAYS.between(dueDate, returnDate);
            if (daysOverdue < 0) daysOverdue = 0;

            double calculatedFee = (res != null) ? res.calculateLateFee(daysOverdue) : 0.0;

            // Update checkout record
            String updateCheckoutSql = "UPDATE checkouts SET return_date = ?, late_fee = ?, checkout_status = 'RETURNED' WHERE checkout_id = ?";
            try (PreparedStatement psUpCheckout = conn.prepareStatement(updateCheckoutSql)) {
                psUpCheckout.setDate(1, Date.valueOf(returnDate));
                psUpCheckout.setDouble(2, calculatedFee);
                psUpCheckout.setInt(3, checkoutId);
                psUpCheckout.executeUpdate();
            }

            // Update resource status back to AVAILABLE
            String updateResSql = "UPDATE resources SET status = 'AVAILABLE' WHERE resource_id = ?";
            try (PreparedStatement psUpRes = conn.prepareStatement(updateResSql)) {
                psUpRes.setString(1, resourceId);
                psUpRes.executeUpdate();
            }

            conn.commit();
            return calculatedFee;

        } catch (SQLException e) {
            System.err.println("Return failed: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    System.err.println("Rollback failed: " + ex.getMessage());
                }
            }
            return -1.0;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException ignored) {}
            }
        }
    }

    /**
     * Get all active checkouts currently borrowed.
     */
    public List<CheckoutTransaction> getActiveCheckouts() {
        return getCheckoutsWhere("WHERE checkout_status = 'ACTIVE' ORDER BY checkout_id DESC");
    }

    /**
     * Get all checkout history.
     */
    public List<CheckoutTransaction> getAllCheckouts() {
        return getCheckoutsWhere("ORDER BY checkout_id DESC");
    }

    private List<CheckoutTransaction> getCheckoutsWhere(String clause) {
        List<CheckoutTransaction> list = new ArrayList<>();
        String sql = "SELECT * FROM checkouts " + clause;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("checkout_id");
                String resId = rs.getString("resource_id");
                String sId = rs.getString("student_id");
                String sName = rs.getString("student_name");
                LocalDate cDate = rs.getDate("checkout_date").toLocalDate();
                LocalDate dDate = rs.getDate("due_date").toLocalDate();
                Date rDateVal = rs.getDate("return_date");
                LocalDate rDate = (rDateVal != null) ? rDateVal.toLocalDate() : null;
                double fee = rs.getDouble("late_fee");
                String status = rs.getString("checkout_status");

                list.add(new CheckoutTransaction(id, resId, sId, sName, cDate, dDate, rDate, fee, status));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching checkouts: " + e.getMessage());
        }
        return list;
    }
}
