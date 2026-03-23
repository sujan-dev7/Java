package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * dbconnection — all SQL operations.
 * Username comparisons use BINARY (case-sensitive): "Admin" != "admin".
 */
public class dbconnection {

    private final String url      = "jdbc:mysql://localhost:3306/gym_equipment_management";
    private final String user     = "root";
    private final String password = "password";   // ← your MySQL password

    // ── Connection ────────────────────────────────────────────────────────────────
    public Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Database connected successfully!");
            return conn;
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return null;
        }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  USERS  (BINARY = case-sensitive username matching)
    // ════════════════════════════════════════════════════════════════════════════

    /** Returns true if username + password match (case-sensitive username). */
    public boolean loginUser(String username, String password) {
        String sql = "SELECT id FROM users WHERE BINARY username = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeQuery().next();
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    /** Returns full UserModel — case-sensitive username lookup. */
    public UserModel getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE BINARY username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                UserModel u = new UserModel();
                u.setId(rs.getInt("id"));
                u.setUsername(rs.getString("username"));
                u.setEmail(rs.getString("email"));
                u.setFullName(rs.getString("full_name"));
                u.setRole(rs.getString("role"));
                return u;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public String getUserRole(String username) {
        String sql = "SELECT role FROM users WHERE BINARY username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("role");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    /** Case-sensitive username existence check. */
    public boolean usernameExists(String username) {
        String sql = "SELECT id FROM users WHERE BINARY username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean emailExists(String email) {
        String sql = "SELECT id FROM users WHERE email = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            return stmt.executeQuery().next();
        } catch (SQLException e) { return false; }
    }

    public boolean createUser(String username, String password,
                              String email, String fullName, String role) {
        String sql = "INSERT INTO users (username,password,email,full_name,role) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username); stmt.setString(2, password);
            stmt.setString(3, email);   stmt.setString(4, fullName);
            stmt.setString(5, role);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public String readUser(String username) {
        String sql = "SELECT password FROM users WHERE BINARY username = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("password");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public ResultSet getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY created_at DESC";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    public boolean updateUserPassword(String username, String newPassword) {
        String sql = "UPDATE users SET password=? WHERE BINARY username=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword); stmt.setString(2, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteUser(String username) {
        String sql = "DELETE FROM users WHERE BINARY username=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  EQUIPMENT
    // ════════════════════════════════════════════════════════════════════════════

    public boolean addEquipment(String name, String category, String serialNumber,
                                String purchaseDate, String status, String notes, int addedBy) {
        String sql = "INSERT INTO equipment (name,category,serial_number,purchase_date,status,condition_notes,added_by) VALUES(?,?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);    stmt.setString(2, category);
            stmt.setString(3, serialNumber); stmt.setString(4, purchaseDate);
            stmt.setString(5, status);  stmt.setString(6, notes);
            stmt.setInt(7, addedBy);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public ResultSet getAllEquipment() {
        String sql = "SELECT e.*, u.full_name AS added_by_name FROM equipment e "
                   + "LEFT JOIN users u ON e.added_by=u.id ORDER BY e.created_at DESC";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    public ResultSet getEquipmentByStatus(String status) {
        String sql = "SELECT * FROM equipment WHERE status=? ORDER BY name";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, status);
            return stmt.executeQuery();
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    public boolean updateEquipment(int id, String name, String category,
                                   String serialNumber, String purchaseDate,
                                   String status, String notes) {
        String sql = "UPDATE equipment SET name=?,category=?,serial_number=?,purchase_date=?,status=?,condition_notes=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);    stmt.setString(2, category);
            stmt.setString(3, serialNumber); stmt.setString(4, purchaseDate);
            stmt.setString(5, status);  stmt.setString(6, notes);
            stmt.setInt(7, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean deleteEquipment(int id) {
        String sql = "DELETE FROM equipment WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public int countAllEquipment() {
        String sql = "SELECT COUNT(*) FROM equipment";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int countEquipmentByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM equipment WHERE status=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  MAINTENANCE LOGS
    // ════════════════════════════════════════════════════════════════════════════

    public boolean addMaintenanceLog(int equipmentId, int performedBy, String type,
                                     String description, String scheduledDate, String status) {
        String sql = "INSERT INTO maintenance_logs (equipment_id,performed_by,maintenance_type,description,scheduled_date,status) VALUES(?,?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, equipmentId); stmt.setInt(2, performedBy);
            stmt.setString(3, type);     stmt.setString(4, description);
            stmt.setString(5, scheduledDate); stmt.setString(6, status);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public ResultSet getAllMaintenanceLogs() {
        String sql = "SELECT ml.*, e.name AS equipment_name, u.full_name AS performed_by_name "
                   + "FROM maintenance_logs ml "
                   + "LEFT JOIN equipment e ON ml.equipment_id=e.id "
                   + "LEFT JOIN users u ON ml.performed_by=u.id "
                   + "ORDER BY ml.scheduled_date DESC";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }

    public boolean updateMaintenanceStatus(int id, String status, String completedDate) {
        String sql = "UPDATE maintenance_logs SET status=?,completed_date=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status); stmt.setString(2, completedDate);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // ════════════════════════════════════════════════════════════════════════════
    //  ACTIVITY LOG
    // ════════════════════════════════════════════════════════════════════════════

    public void logActivity(int userId, String action, String details) {
        String sql = "INSERT INTO activity_log (user_id,action,details) VALUES(?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); stmt.setString(2, action); stmt.setString(3, details);
            stmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public ResultSet getRecentActivityLogs(int limit) {
        String sql = "SELECT al.*, u.full_name FROM activity_log al "
                   + "LEFT JOIN users u ON al.user_id=u.id "
                   + "ORDER BY al.created_at DESC LIMIT ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, limit);
            return stmt.executeQuery();
        } catch (SQLException e) { e.printStackTrace(); return null; }
    }
}
