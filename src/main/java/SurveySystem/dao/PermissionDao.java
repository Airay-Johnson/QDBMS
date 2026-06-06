package SurveySystem.dao;

import SurveySystem.model.Permission;
import SurveySystem.model.Role;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 权限数据访问层
 * 表：Permission（pid, name, description）
 *     Role（roid, name, description）
 *     Role_Permission（role_id, permission_id）
 *     User_Role（user_id, role_id）
 *     User_Permission（user_id, permission_id）
 */
public class PermissionDao {

    /** 根据PID查找 */
    public Permission findById(Long pid) {
        String sql = "SELECT * FROM Permission WHERE pid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, pid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToPermission(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 根据名称查找 */
    public Permission findByName(String name) {
        String sql = "SELECT * FROM Permission WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToPermission(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询所有权限 */
    public List<Permission> findAll() {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT * FROM Permission ORDER BY pid";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapToPermission(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 保存权限（返回自增pid） */
    public Long save(Permission permission) {
        String sql = "INSERT INTO Permission (name, description) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, permission.getName());
            stmt.setString(2, permission.getDescription());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 更新权限 */
    public boolean update(Permission permission) {
        String sql = "UPDATE Permission SET name = ?, description = ? WHERE pid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, permission.getName());
            stmt.setString(2, permission.getDescription());
            stmt.setLong(3, permission.getPid());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 删除权限（事务：先删关联，再删自身） */
    public boolean delete(Long pid) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement s = conn.prepareStatement(
                    "DELETE FROM Role_Permission WHERE permission_id = ?")) {
                s.setLong(1, pid); s.executeUpdate();
            }
            try (PreparedStatement s = conn.prepareStatement(
                    "DELETE FROM User_Permission WHERE permission_id = ?")) {
                s.setLong(1, pid); s.executeUpdate();
            }
            try (PreparedStatement s = conn.prepareStatement(
                    "DELETE FROM Permission WHERE pid = ?")) {
                s.setLong(1, pid);
                boolean ok = s.executeUpdate() > 0;
                conn.commit();
                return ok;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            e.printStackTrace();
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
            }
        }
        return false;
    }

    // ===== 角色操作（委托 RoleDao 更合适，这里保留简化版） =====

    /** 查询所有角色 */
    public List<Role> getAllRoles() {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT * FROM Role ORDER BY roid";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Role r = new Role();
                r.setRoleId(rs.getLong("roid"));
                r.setRoleName(rs.getString("name"));
                r.setDescription(rs.getString("description"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 查询用户的角色 */
    public List<Role> getUserRoles(int userId) {
        List<Role> list = new ArrayList<>();
        String sql = "SELECT r.* FROM Role r " +
                "INNER JOIN User_Role ur ON r.roid = ur.role_id " +
                "WHERE ur.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Role r = new Role();
                r.setRoleId(rs.getLong("roid"));
                r.setRoleName(rs.getString("name"));
                r.setDescription(rs.getString("description"));
                list.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 为用户分配角色 */
    public boolean assignRoleToUser(int userId, String roleName) {
        String sql = "INSERT INTO User_Role (user_id, role_id) " +
                "SELECT ?, roid FROM Role WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, roleName);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /** 从用户移除角色 */
    public boolean removeRoleFromUser(int userId, String roleName) {
        String sql = "DELETE FROM User_Role " +
                "WHERE user_id = ? AND role_id = (SELECT roid FROM Role WHERE name = ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setString(2, roleName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 为角色分配权限 */
    public boolean assignPermissionToRole(String roleName, String permissionName) {
        String sql = "INSERT INTO Role_Permission (role_id, permission_id) " +
                "SELECT r.roid, p.pid FROM Role r, Permission p " +
                "WHERE r.name = ? AND p.name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            stmt.setString(2, permissionName);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /** 从角色移除权限 */
    public boolean removePermissionFromRole(String roleName, String permissionName) {
        String sql = "DELETE FROM Role_Permission " +
                "WHERE role_id = (SELECT roid FROM Role WHERE name = ?) " +
                "AND permission_id = (SELECT pid FROM Permission WHERE name = ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roleName);
            stmt.setString(2, permissionName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 根据角色ID查询该角色的权限列表 */
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        List<Permission> list = new ArrayList<>();
        String sql = "SELECT p.* FROM Permission p " +
                "INNER JOIN Role_Permission rp ON p.pid = rp.permission_id " +
                "WHERE rp.role_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapToPermission(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Permission mapToPermission(ResultSet rs) throws SQLException {
        Permission p = new Permission();
        p.setPid(rs.getLong("pid"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        return p;
    }
}
