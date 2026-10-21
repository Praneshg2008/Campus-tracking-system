package campus.dao;

import campus.db.DBConnection;
import campus.models.LabKit;
import campus.models.MediaAsset;
import campus.models.Resource;
import campus.models.SportsAsset;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ResourceDAO - Data Access Object for high-value campus resources.
 * Demonstrates:
 * 1. Polymorphic SQL object mapping (joining parent table with specialized child tables)
 * 2. Cascading Updates (DBMS foreign key ON UPDATE CASCADE verification)
 * 3. Database Transactions with setAutoCommit
 */
public class ResourceDAO {

    /**
     * Retrieve all resources, constructing appropriate specialized OOP instances
     * (LabKit, SportsAsset, or MediaAsset) based on the category.
     */
    public List<Resource> getAllResources() {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT r.*, " +
                "       lk.components_count, lk.microcontroller_type, lk.requires_calibration, " +
                "       sa.sport_name, sa.wear_condition, sa.tournament_grade, " +
                "       ma.lens_model, ma.shutter_count, ma.max_resolution " +
                "FROM resources r " +
                "LEFT JOIN lab_kits lk ON r.resource_id = lk.resource_id " +
                "LEFT JOIN sports_assets sa ON r.resource_id = sa.resource_id " +
                "LEFT JOIN media_assets ma ON r.resource_id = ma.resource_id " +
                "ORDER BY r.resource_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String id = rs.getString("resource_id");
                String name = rs.getString("name");
                String dept = rs.getString("department");
                String category = rs.getString("category");
                String status = rs.getString("status");
                double lateFee = rs.getDouble("daily_late_fee");

                Resource res = null;
                switch (category) {
                    case "LAB_KIT":
                        int count = rs.getInt("components_count");
                        String mc = rs.getString("microcontroller_type");
                        boolean calib = rs.getBoolean("requires_calibration");
                        res = new LabKit(id, name, dept, status, lateFee, count, mc, calib);
                        break;
                    case "SPORTS_ASSET":
                        String sport = rs.getString("sport_name");
                        String wear = rs.getString("wear_condition");
                        boolean tourney = rs.getBoolean("tournament_grade");
                        res = new SportsAsset(id, name, dept, status, lateFee, sport, wear, tourney);
                        break;
                    case "MEDIA_ASSET":
                        String lens = rs.getString("lens_model");
                        int shutter = rs.getInt("shutter_count");
                        String resStr = rs.getString("max_resolution");
                        res = new MediaAsset(id, name, dept, status, lateFee, lens, shutter, resStr);
                        break;
                }
                if (res != null) {
                    list.add(res);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching resources: " + e.getMessage());
        }
        return list;
    }

    /**
     * Retrieve a single resource by its ID
     */
    public Resource getResourceById(String resourceId) {
        String sql = "SELECT r.*, " +
                "       lk.components_count, lk.microcontroller_type, lk.requires_calibration, " +
                "       sa.sport_name, sa.wear_condition, sa.tournament_grade, " +
                "       ma.lens_model, ma.shutter_count, ma.max_resolution " +
                "FROM resources r " +
                "LEFT JOIN lab_kits lk ON r.resource_id = lk.resource_id " +
                "LEFT JOIN sports_assets sa ON r.resource_id = sa.resource_id " +
                "LEFT JOIN media_assets ma ON r.resource_id = ma.resource_id " +
                "WHERE r.resource_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String id = rs.getString("resource_id");
                    String name = rs.getString("name");
                    String dept = rs.getString("department");
                    String category = rs.getString("category");
                    String status = rs.getString("status");
                    double lateFee = rs.getDouble("daily_late_fee");

                    switch (category) {
                        case "LAB_KIT":
                            return new LabKit(id, name, dept, status, lateFee,
                                    rs.getInt("components_count"),
                                    rs.getString("microcontroller_type"),
                                    rs.getBoolean("requires_calibration"));
                        case "SPORTS_ASSET":
                            return new SportsAsset(id, name, dept, status, lateFee,
                                    rs.getString("sport_name"),
                                    rs.getString("wear_condition"),
                                    rs.getBoolean("tournament_grade"));
                        case "MEDIA_ASSET":
                            return new MediaAsset(id, name, dept, status, lateFee,
                                    rs.getString("lens_model"),
                                    rs.getInt("shutter_count"),
                                    rs.getString("max_resolution"));
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching resource by ID: " + e.getMessage());
        }
        return null;
    }

    /**
     * Add a new resource into database with transactional integrity.
     */
    public boolean addResource(Resource res) {
        String baseSql = "INSERT INTO resources (resource_id, name, department, category, status, daily_late_fee) VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false); // Begin transaction

            try (PreparedStatement psBase = conn.prepareStatement(baseSql)) {
                psBase.setString(1, res.getResourceId());
                psBase.setString(2, res.getName());
                psBase.setString(3, res.getDepartment());
                psBase.setString(4, res.getCategory());
                psBase.setString(5, res.getStatus());
                psBase.setDouble(6, res.getDailyLateFee());
                psBase.executeUpdate();
            }

            // Insert specialized child attributes
            if (res instanceof LabKit) {
                LabKit kit = (LabKit) res;
                String childSql = "INSERT INTO lab_kits (resource_id, components_count, microcontroller_type, requires_calibration) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psChild = conn.prepareStatement(childSql)) {
                    psChild.setString(1, kit.getResourceId());
                    psChild.setInt(2, kit.getComponentsCount());
                    psChild.setString(3, kit.getMicrocontrollerType());
                    psChild.setBoolean(4, kit.isRequiresCalibration());
                    psChild.executeUpdate();
                }
            } else if (res instanceof SportsAsset) {
                SportsAsset sport = (SportsAsset) res;
                String childSql = "INSERT INTO sports_assets (resource_id, sport_name, wear_condition, tournament_grade) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psChild = conn.prepareStatement(childSql)) {
                    psChild.setString(1, sport.getResourceId());
                    psChild.setString(2, sport.getSportName());
                    psChild.setString(3, sport.getWearCondition());
                    psChild.setBoolean(4, sport.isTournamentGrade());
                    psChild.executeUpdate();
                }
            } else if (res instanceof MediaAsset) {
                MediaAsset media = (MediaAsset) res;
                String childSql = "INSERT INTO media_assets (resource_id, lens_model, shutter_count, max_resolution) VALUES (?, ?, ?, ?)";
                try (PreparedStatement psChild = conn.prepareStatement(childSql)) {
                    psChild.setString(1, media.getResourceId());
                    psChild.setString(2, media.getLensModel());
                    psChild.setInt(3, media.getShutterCount());
                    psChild.setString(4, media.getMaxResolution());
                    psChild.executeUpdate();
                }
            }

            conn.commit(); // Commit transaction
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding resource: " + e.getMessage());
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
     * Update Resource Status (e.g. AVAILABLE, CHECKED_OUT, MAINTENANCE)
     */
    public boolean updateStatus(String resourceId, String status) {
        String sql = "UPDATE resources SET status = ? WHERE resource_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, resourceId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating status: " + e.getMessage());
            return false;
        }
    }

    /**
     * DEMONSTRATES DBMS CASCADING UPDATE:
     * Updating primary key resource_id in parent table 'resources'.
     * The child tables (lab_kits, sports_assets, media_assets, maintenance_logs, checkouts)
     * are configured with `ON UPDATE CASCADE`, so MySQL automatically updates foreign keys.
     */
    public boolean updateResourceIdCascading(String oldId, String newId) {
        String sql = "UPDATE resources SET resource_id = ? WHERE resource_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newId);
            ps.setString(2, oldId);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.err.println("Error executing cascading update: " + e.getMessage());
            return false;
        }
    }
}
