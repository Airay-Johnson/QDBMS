package SurveySystem.dao;

import SurveySystem.model.Permission;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户-权限直接关联数据访问层
 * 表：User_Permission（user_id, permission_id）
 *     Permission（pid, name, description）
 */
public class UserPermissionDao {

    /** 获取用户直接拥有的权限 */
    public List<Permission> getDirectPermissions(Long userId) {
        List<Permission> perms = new ArrayList<>();
        String sql = "SELECT p.* FROM Permission p " +
                "INNER JOIN User_Permission up ON p.pid = up.permission_id " +
                "WHERE up.user_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Permission p = new Permission();
                p.setPid(rs.getLong("pid"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                perms.add(p);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return perms;
    }

    /** 添加直接权限 */
    public boolean addPermission(Long userId, Long permissionId) {
        String sql = "INSERT INTO User_Permission (user_id, permission_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, permissionId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /** 移除直接权限 */
    public boolean removePermission(Long userId, Long permissionId) {
        String sql = "DELETE FROM User_Permission WHERE user_id = ? AND permission_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, userId);
            stmt.setLong(2, permissionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
