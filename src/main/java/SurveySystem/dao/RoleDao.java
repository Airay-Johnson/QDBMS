package SurveySystem.dao;

import SurveySystem.model.Role;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色数据访问层
 * 表结构：Role（roid, name, description）
 */
public class RoleDao {

    /** 通过ROID查找 */
    public Role findById(Long id) {
        String sql = "SELECT * FROM Role WHERE roid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToRole(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 通过名称查找 */
    public Role findByName(String name) {
        String sql = "SELECT * FROM Role WHERE name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToRole(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询所有角色 */
    public List<Role> findAll() {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT * FROM Role ORDER BY roid";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) roles.add(mapToRole(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /** 查询某用户拥有的所有角色（通过User_Role关联表） */
    public List<Role> findRolesByUserId(Long userId) {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.* FROM Role r " +
                "INNER JOIN User_Role ur ON r.roid = ur.role_id " +
                "WHERE ur.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) roles.add(mapToRole(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /** 保存角色（返回自增roid） */
    public Long save(Role role) {
        String sql = "INSERT INTO Role (name, description) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, role.getRoleName());
            stmt.setString(2, role.getDescription());
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

    /** 更新角色 */
    public boolean update(Role role) {
        String sql = "UPDATE Role SET name = ?, description = ? WHERE roid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, role.getRoleName());
            stmt.setString(2, role.getDescription());
            stmt.setLong(3, role.getRoleId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 删除角色 */
    public boolean delete(Long roid) {
        String sql = "DELETE FROM Role WHERE roid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Role mapToRole(ResultSet rs) throws SQLException {
        Role role = new Role();
        role.setRoleId(rs.getLong("roid"));
        role.setRoleName(rs.getString("name"));
        role.setDescription(rs.getString("description"));
        return role;
    }
}
