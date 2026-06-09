package SurveySystem.dao;

import SurveySystem.model.Role;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户-角色关联数据访问层
 * 表：User_Role（user_id, role_id）
 */
public class UserRoleDao {

    /** 获取用户的角色ID列表 */
    public List<Long> getRoleIdsByUserId(Long userId) {
        List<Long> roleIds = new ArrayList<>();
        String sql = "SELECT role_id FROM User_Role WHERE user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) roleIds.add(rs.getLong("role_id"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roleIds;
    }

    /** 获取用户的角色对象列表 */
    public List<Role> getUserRoles(Long userId) {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.* FROM Role r " +
                "INNER JOIN User_Role ur ON r.roid = ur.role_id " +
                "WHERE ur.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Role r = new Role();
                r.setRoleId(rs.getLong("roid"));
                r.setRoleName(rs.getString("name"));
                r.setDescription(rs.getString("description"));
                roles.add(r);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /** 为用户分配角色 */
    public boolean assignRoleToUser(Long userId, Long roleId) {
        String sql = "INSERT INTO User_Role (user_id, role_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, roleId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /** 移除用户的角色 */
    public boolean removeRoleFromUser(Long userId, Long roleId) {
        String sql = "DELETE FROM User_Role WHERE user_id = ? AND role_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, roleId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 检查用户是否有某角色 */
    public boolean hasRole(Long userId, String roleName) {
        String sql = "SELECT COUNT(*) FROM User_Role ur " +
                "INNER JOIN Role r ON r.roid = ur.role_id " +
                "WHERE ur.user_id = ? AND r.name = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setString(2, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
