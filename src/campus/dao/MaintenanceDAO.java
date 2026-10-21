package campus.dao;

import campus.db.DBConnection;
import campus.models.MaintenanceLog;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * MaintenanceDAO - Handles logging maintenance, service cost tracking,
 * and equipment state transitions.
 */
public class MaintenanceDAO {

    /**
     * Records a maintenance event and updates the resource's operational status.
     * Uses transaction to ensure consistency.
     */
    public boolean logMaintenance(MaintenanceLog log) {
        String insertSql = "INSERT INTO maintenance_logs (resource_id, issue_description, technician_name, maintenance_cost, service_date, status_after) VALUES (?, ?, ?, ?, ?, ?)";
        String updateStatusSql = "UPDATE resources SET status = ? WHERE resource_id = ?";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psInsert = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                psInsert.setString(1, log.getResourceId());
                psInsert.setString(2, log.getIssueDescription());
                psInsert.setString(3, log.getTechnicianName());
                psInsert.setDouble(4, log.getMaintenanceCost());
                psInsert.setDate(5, Date.valueOf(log.getServiceDate()));
                psInsert.setString(6, log.getStatusAfter());
                psInsert.executeUpdate();

                try (ResultSet generatedKeys = psInsert.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        log.setLogId(generatedKeys.getInt(1));
                    }
                }
            }

            try (PreparedStatement psUpdate = conn.prepareStatement(updateStatusSql)) {
                psUpdate.setString(1, log.getStatusAfter());
                psUpdate.setString(2, log.getResourceId());
                psUpdate.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            System.err.println("Error recording maintenance log: " + e.getMessage());
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
     * Retrieve all maintenance logs in chronological order.
     */
    public List<MaintenanceLog> getAllLogs() {
        List<MaintenanceLog> list = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_logs ORDER BY log_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("log_id");
                String resId = rs.getString("resource_id");
                String desc = rs.getString("issue_description");
                String tech = rs.getString("technician_name");
                double cost = rs.getDouble("maintenance_cost");
                LocalDate date = rs.getDate("service_date").toLocalDate();
                String statusAfter = rs.getString("status_after");

                list.add(new MaintenanceLog(id, resId, desc, tech, cost, date, statusAfter));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching maintenance logs: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieve maintenance history for a specific resource ID.
     */
    public List<MaintenanceLog> getLogsForResource(String resourceId) {
        List<MaintenanceLog> list = new ArrayList<>();
        String sql = "SELECT * FROM maintenance_logs WHERE resource_id = ? ORDER BY log_id DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("log_id");
                    String resId = rs.getString("resource_id");
                    String desc = rs.getString("issue_description");
                    String tech = rs.getString("technician_name");
                    double cost = rs.getDouble("maintenance_cost");
                    LocalDate date = rs.getDate("service_date").toLocalDate();
                    String statusAfter = rs.getString("status_after");

                    list.add(new MaintenanceLog(id, resId, desc, tech, cost, date, statusAfter));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching logs for resource " + resourceId + ": " + e.getMessage());
        }
        return list;
    }
}
