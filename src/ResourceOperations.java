import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ResourceOperations - Handles database operations for campus equipment (resources),
 * maintenance records, and system statistics.
 */
public class ResourceOperations {

    /**
     * Add a new equipment item to the resources table.
     */
    public boolean addResource(Resource resource) {
        String sql = "INSERT INTO resources (resource_id, name, category, department, status, daily_late_fee) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resource.getResourceId());
            ps.setString(2, resource.getName());
            ps.setString(3, resource.getCategory());
            ps.setString(4, resource.getDepartment());
            ps.setString(5, resource.getStatus());
            ps.setDouble(6, resource.getDailyLateFee());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding equipment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Retrieve all equipment ordered by ID.
     */
    public List<Resource> getAllResources() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources ORDER BY resource_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapRowToResource(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment list: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieve equipment filtered by status (AVAILABLE, BORROWED, MAINTENANCE).
     */
    public List<Resource> getResourcesByStatus(String status) {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE status = ? ORDER BY resource_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToResource(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error retrieving equipment by status: " + e.getMessage());
        }
        return list;
    }

    /**
     * Find a single equipment item by its ID.
     */
    public Resource getResourceById(String resourceId) {
        String sql = "SELECT * FROM resources WHERE resource_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToResource(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding equipment: " + e.getMessage());
        }
        return null;
    }

    /**
     * Search equipment by keyword matching ID, name, category, or department.
     */
    public List<Resource> searchResources(String keyword) {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM resources WHERE resource_id LIKE ? OR name LIKE ? OR category LIKE ? OR department LIKE ? ORDER BY resource_id";
        String pattern = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToResource(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching equipment: " + e.getMessage());
        }
        return list;
    }

    /**
     * Update an existing equipment item's details.
     */
    public boolean updateResource(Resource resource) {
        String sql = "UPDATE resources SET name = ?, category = ?, department = ?, status = ?, daily_late_fee = ? WHERE resource_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resource.getName());
            ps.setString(2, resource.getCategory());
            ps.setString(3, resource.getDepartment());
            ps.setString(4, resource.getStatus());
            ps.setDouble(5, resource.getDailyLateFee());
            ps.setString(6, resource.getResourceId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating equipment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Delete an equipment item by ID.
     */
    public boolean deleteResource(String resourceId) {
        String sql = "DELETE FROM resources WHERE resource_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting equipment: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add a maintenance record and update the equipment status in a single transaction.
     */
    public boolean addMaintenanceRecord(String resourceId, String issueDescription, String technicianName,
                                        double cost, LocalDate serviceDate, String statusAfter) {
        String insertSql = "INSERT INTO maintenance (resource_id, issue_description, technician_name, maintenance_cost, service_date, status_after) VALUES (?, ?, ?, ?, ?, ?)";
        String updateSql = "UPDATE resources SET status = ? WHERE resource_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
                psInsert.setString(1, resourceId);
                psInsert.setString(2, issueDescription);
                psInsert.setString(3, technicianName);
                psInsert.setDouble(4, cost);
                psInsert.setDate(5, Date.valueOf(serviceDate));
                psInsert.setString(6, statusAfter);
                psInsert.executeUpdate();
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateSql)) {
                psUpdate.setString(1, statusAfter);
                psUpdate.setString(2, resourceId);
                psUpdate.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving maintenance record: " + e.getMessage());
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
     * Display maintenance records using a JOIN between maintenance and resources tables.
     */
    public void displayMaintenanceRecords(String resourceIdFilter) {
        String baseSql = "SELECT m.maintenance_id, m.resource_id, r.name AS resource_name, " +
                         "m.issue_description, m.technician_name, m.maintenance_cost, m.service_date, m.status_after " +
                         "FROM maintenance m " +
                         "JOIN resources r ON m.resource_id = r.resource_id ";
        String sql = (resourceIdFilter == null || resourceIdFilter.isEmpty())
                ? baseSql + "ORDER BY m.maintenance_id DESC"
                : baseSql + "WHERE m.resource_id = ? ORDER BY m.maintenance_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (resourceIdFilter != null && !resourceIdFilter.isEmpty()) {
                ps.setString(1, resourceIdFilter);
            }
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("Record #%-3d | Equipment: %-28s (%s) | Date: %s%n",
                            rs.getInt("maintenance_id"),
                            rs.getString("resource_name"),
                            rs.getString("resource_id"),
                            rs.getDate("service_date"));
                    System.out.printf("            | Technician: %-18s | Cost: Rs. %-8.2f | Status After: %s%n",
                            rs.getString("technician_name"),
                            rs.getDouble("maintenance_cost"),
                            rs.getString("status_after"));
                    System.out.printf("            | Issue: %s%n", rs.getString("issue_description"));
                    System.out.println("--------------------------------------------------------------------------");
                }
                if (!found) {
                    System.out.println("No maintenance records found.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error viewing maintenance records: " + e.getMessage());
        }
    }

    /**
     * Display system statistics using SQL COUNT and GROUP BY queries.
     */
    public void displayStatistics() {
        String totalResourcesSql = "SELECT COUNT(*) AS total FROM resources";
        String byStatusSql = "SELECT status, COUNT(*) AS count FROM resources GROUP BY status";
        String byCategorySql = "SELECT category, COUNT(*) AS count FROM resources GROUP BY category";
        String totalStudentsSql = "SELECT COUNT(*) AS total FROM students";
        String checkoutStatsSql = "SELECT COUNT(*) AS total_checkouts, COALESCE(SUM(late_fee), 0) AS total_fines FROM checkouts";

        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(totalResourcesSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total Equipment Registered : " + rs.getInt("total"));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(totalStudentsSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Total Registered Students  : " + rs.getInt("total"));
                }
            }

            System.out.println("\nEquipment Count by Status (GROUP BY status):");
            try (PreparedStatement ps = conn.prepareStatement(byStatusSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  - %-15s : %d%n", rs.getString("status"), rs.getInt("count"));
                }
            }

            System.out.println("\nEquipment Count by Category (GROUP BY category):");
            try (PreparedStatement ps = conn.prepareStatement(byCategorySql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("  - %-15s : %d%n", rs.getString("category"), rs.getInt("count"));
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(checkoutStatsSql);
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.println("\nBorrowing Summary:");
                    System.out.println("  - Total Checkout Transactions : " + rs.getInt("total_checkouts"));
                    System.out.printf("  - Total Overdue Fines Assessed: Rs. %.2f%n", rs.getDouble("total_fines"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error loading statistics: " + e.getMessage());
        }
    }

    private Resource mapRowToResource(ResultSet rs) throws SQLException {
        return new Resource(
                rs.getString("resource_id"),
                rs.getString("name"),
                rs.getString("category"),
                rs.getString("department"),
                rs.getString("status"),
                rs.getDouble("daily_late_fee")
        );
    }
}
