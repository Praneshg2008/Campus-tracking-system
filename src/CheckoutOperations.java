import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckoutOperations - Handles student registration/management, issuing equipment,
 * recording returns, calculating overdue fines, and displaying borrowing history.
 */
public class CheckoutOperations {
    private final ResourceOperations resourceOps = new ResourceOperations();

    // =========================================================================
    // Student Database Operations
    // =========================================================================

    /**
     * Register a new student in the database.
     */
    public boolean addStudent(Student student) {
        String sql = "INSERT INTO students (student_id, name, department, email, phone) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getStudentId());
            ps.setString(2, student.getName());
            ps.setString(3, student.getDepartment());
            ps.setString(4, student.getEmail());
            ps.setString(5, student.getPhone());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error registering student: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve all registered students.
     */
    public List<Student> getAllStudents() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT * FROM students ORDER BY student_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToStudent(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving students: " + e.getMessage());
        }
        return list;
    }

    /**
     * Find a student by ID.
     */
    public Student getStudentById(String studentId) {
        String sql = "SELECT * FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToStudent(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding student: " + e.getMessage());
        }
        return null;
    }

    /**
     * Update an existing student's details.
     */
    public boolean updateStudent(Student student) {
        String sql = "UPDATE students SET name = ?, department = ?, email = ?, phone = ? WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, student.getName());
            ps.setString(2, student.getDepartment());
            ps.setString(3, student.getEmail());
            ps.setString(4, student.getPhone());
            ps.setString(5, student.getStudentId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating student: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a student currently has unreturned borrowed equipment.
     */
    public boolean studentHasActiveCheckouts(String studentId) {
        String sql = "SELECT COUNT(*) FROM checkouts WHERE student_id = ? AND status = 'ACTIVE'";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking active borrowings: " + e.getMessage());
        }
        return false;
    }

    /**
     * Delete a student record by ID.
     */
    public boolean deleteStudent(String studentId) {
        String sql = "DELETE FROM students WHERE student_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, studentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting student: " + e.getMessage());
            return false;
        }
    }

    // =========================================================================
    // Equipment Checkout & Return Operations
    // =========================================================================

    /**
     * Issue an available piece of equipment to a registered student.
     * Uses a database transaction so both checkouts and resources tables update together.
     */
    public boolean issueEquipment(String resourceId, String studentId, int borrowDays) {
        LocalDate checkoutDate = LocalDate.now();
        LocalDate dueDate = checkoutDate.plusDays(borrowDays);

        String insertCheckoutSql = "INSERT INTO checkouts (resource_id, student_id, checkout_date, due_date, status) VALUES (?, ?, ?, ?, 'ACTIVE')";
        String updateResourceSql = "UPDATE resources SET status = 'BORROWED' WHERE resource_id = ? AND status = 'AVAILABLE'";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psUpdate = conn.prepareStatement(updateResourceSql)) {
                psUpdate.setString(1, resourceId);
                int updated = psUpdate.executeUpdate();
                if (updated == 0) {
                    conn.rollback();
                    return false;
                }
            }

            try (PreparedStatement psInsert = conn.prepareStatement(insertCheckoutSql)) {
                psInsert.setString(1, resourceId);
                psInsert.setString(2, studentId);
                psInsert.setDate(3, Date.valueOf(checkoutDate));
                psInsert.setDate(4, Date.valueOf(dueDate));
                psInsert.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error issuing equipment: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
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
     * Record the return of borrowed equipment and calculate any overdue fine.
     * Returns the calculated fine amount, or -1.0 if the checkout record is invalid.
     */
    public double returnEquipment(int checkoutId, LocalDate returnDate) {
        String selectSql = "SELECT resource_id, due_date FROM checkouts WHERE checkout_id = ? AND status = 'ACTIVE'";
        String updateCheckoutSql = "UPDATE checkouts SET return_date = ?, late_fee = ?, status = 'RETURNED' WHERE checkout_id = ?";
        String updateResourceSql = "UPDATE resources SET status = 'AVAILABLE' WHERE resource_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            String resourceId;
            LocalDate dueDate;

            try (PreparedStatement psSelect = conn.prepareStatement(selectSql)) {
                psSelect.setInt(1, checkoutId);
                try (ResultSet rs = psSelect.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return -1.0;
                    }
                    resourceId = rs.getString("resource_id");
                    dueDate = rs.getDate("due_date").toLocalDate();
                }
            }

            Resource resource = resourceOps.getResourceById(resourceId);
            long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
            if (overdueDays < 0) {
                overdueDays = 0;
            }
            double fine = (resource != null) ? resource.calculateLateFee(overdueDays) : 0.0;

            try (PreparedStatement psUpdateCheckout = conn.prepareStatement(updateCheckoutSql)) {
                psUpdateCheckout.setDate(1, Date.valueOf(returnDate));
                psUpdateCheckout.setDouble(2, fine);
                psUpdateCheckout.setInt(3, checkoutId);
                psUpdateCheckout.executeUpdate();
            }

            try (PreparedStatement psUpdateResource = conn.prepareStatement(updateResourceSql)) {
                psUpdateResource.setString(1, resourceId);
                psUpdateResource.executeUpdate();
            }

            conn.commit();
            return fine;
        } catch (SQLException e) {
            System.err.println("Error returning equipment: " + e.getMessage());
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ignored) {}
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
     * Display currently borrowed (ACTIVE) equipment using a JOIN query.
     * Returns true if at least one active checkout was found.
     */
    public boolean displayActiveCheckouts() {
        String sql = getBaseJoinQuery() + " WHERE c.status = 'ACTIVE' ORDER BY c.checkout_id DESC";
        return printCheckoutRecords(sql, null);
    }

    /**
     * Display borrowing history (all records or filtered by student ID) using a JOIN query.
     */
    public void displayBorrowingHistory(String studentIdFilter) {
        String sql;
        String param = null;
        if (studentIdFilter == null || studentIdFilter.isEmpty()) {
            sql = getBaseJoinQuery() + " ORDER BY c.checkout_id DESC";
        } else {
            sql = getBaseJoinQuery() + " WHERE c.student_id = ? ORDER BY c.checkout_id DESC";
            param = studentIdFilter;
        }
        boolean found = printCheckoutRecords(sql, param);
        if (!found) {
            System.out.println("No borrowing records found.");
        }
    }

    private String getBaseJoinQuery() {
        return "SELECT c.checkout_id, c.resource_id, r.name AS resource_name, " +
               "c.student_id, s.name AS student_name, " +
               "c.checkout_date, c.due_date, c.return_date, c.late_fee, c.status " +
               "FROM checkouts c " +
               "JOIN resources r ON c.resource_id = r.resource_id " +
               "JOIN students s ON c.student_id = s.student_id";
    }

    private boolean printCheckoutRecords(String sql, String param) {
        boolean found = false;
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (param != null) {
                ps.setString(1, param);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    found = true;
                    Date returnDateSql = rs.getDate("return_date");
                    String returnedStr = (returnDateSql != null) ? returnDateSql.toString() : "Not Returned";
                    System.out.printf("Checkout #%-3d | Equipment: %-28s (%s) | Student: %-18s (%s)%n",
                            rs.getInt("checkout_id"),
                            rs.getString("resource_name"),
                            rs.getString("resource_id"),
                            rs.getString("student_name"),
                            rs.getString("student_id"));
                    System.out.printf("              | Issued: %s | Due: %s | Returned: %-12s | Status: %-8s | Fine: Rs. %.2f%n",
                            rs.getDate("checkout_date"),
                            rs.getDate("due_date"),
                            returnedStr,
                            rs.getString("status"),
                            rs.getDouble("late_fee"));
                    System.out.println("--------------------------------------------------------------------------");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading checkout records: " + e.getMessage());
        }
        return found;
    }

    private Student mapRowToStudent(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("student_id"),
                rs.getString("name"),
                rs.getString("department"),
                rs.getString("email"),
                rs.getString("phone")
        );
    }
}
