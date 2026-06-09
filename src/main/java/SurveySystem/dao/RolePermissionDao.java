package SurveySystem.dao;

import SurveySystem.model.Permission;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 角色-权限关联数据访问层
 * 表：Role_Permission（role_id, permission_id）
 *     Permission（pid, name, description）
 */
public class RolePermissionDao {

    /** 获取某角色的所有权限 */
    public List<Permission> getRolePermissions(Long roleId) {
        List<Permission> perms = new ArrayList<>();
        String sql = "SELECT p.* FROM Permission p " +
                "INNER JOIN Role_Permission rp ON p.pid = rp.permission_id " +
                "WHERE rp.role_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
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

    /** 为角色分配权限 */
    public boolean assignPermissionToRole(Long roleId, Long permissionId) {
        String sql = "INSERT INTO Role_Permission (role_id, permission_id) VALUES (?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
            stmt.setLong(2, permissionId);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // 可能已存在，忽略
            return false;
        }
    }

    /** 移除角色的权限 */
    public boolean removePermissionFromRole(Long roleId, Long permissionId) {
        String sql = "DELETE FROM Role_Permission WHERE role_id = ? AND permission_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, roleId);
            stmt.setLong(2, permissionId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
