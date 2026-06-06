package SurveySystem.service;

import SurveySystem.dao.PermissionDao;
import SurveySystem.dao.RoleDao;
import SurveySystem.dao.RolePermissionDao;
import SurveySystem.dao.UserRoleDao;
import SurveySystem.model.Permission;
import SurveySystem.model.Role;
import Util.DBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PermissionService {
    private static final Logger logger = LoggerFactory.getLogger(PermissionService.class);

    private final RoleDao roleDao;
    private final PermissionDao permissionDao;
    private final RolePermissionDao rolePermissionDao;
    private final UserRoleDao userRoleDao;

    public PermissionService() {
        this.roleDao = new RoleDao();
        this.permissionDao = new PermissionDao();
        this.rolePermissionDao = new RolePermissionDao();
        this.userRoleDao = new UserRoleDao();
    }

    /**
     * 获取所有角色
     */
    public List<Role> getAllRoles() {
        try {
            return roleDao.findAll();
        } catch (Exception e) {
            logger.error("获取所有角色失败", e);
            return List.of();
        }
    }

    /**
     * 获取所有权限
     */
    public List<Permission> getAllPermissions() {
        try {
            return permissionDao.findAll();
        } catch (Exception e) {
            logger.error("获取所有权限失败", e);
            return List.of();
        }
    }

    /**
     * 获取用户的所有角色
     */
    public List<Role> getUserRoles(Long userId) {
        List<Role> roles = new ArrayList<>();
        String sql = "SELECT r.roid, r.name, r.description " +
                "FROM role r " +
                "JOIN user_role ur ON r.roid = ur.role_id " +
                "WHERE ur.user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Role role = new Role();
                    role.setRoleId(rs.getLong("roid"));
                    role.setRoleName(rs.getString("name"));
                    role.setDescription(rs.getString("description"));
                    roles.add(role);
                }
            }
        } catch (SQLException e) {
            logger.error("获取用户角色时发生错误", e);
        }
        return roles;
    }

    /**
     * 获取角色的所有权限
     */
    public List<Permission> getRolePermissions(Long roleId) {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT p.pid, p.name, p.description " +
                "FROM permission p " +
                "JOIN role_permission rp ON p.pid = rp.permission_id " +
                "WHERE rp.role_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, roleId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Permission permission = new Permission();
                    permission.setPid(rs.getLong("pid"));
                    permission.setName(rs.getString("name"));
                    permission.setDescription(rs.getString("description"));
                    permissions.add(permission);
                }
            }
        } catch (SQLException e) {
            logger.error("获取角色权限时发生错误", e);
        }
        return permissions;
    }
    /**
     * 为用户分配角色
     */
    public boolean assignRoleToUser(Long userId, String roleName) {
        try {
            // 首先通过角色名获取角色
            Role role = roleDao.findByName(roleName);
            if (role == null) {
                logger.error("角色不存在: {}", roleName);
                return false;
            }

            return userRoleDao.assignRoleToUser(userId, role.getRoleId());
        } catch (Exception e) {
            logger.error("为用户分配角色失败，用户ID: {}, 角色名: {}", userId, roleName, e);
            return false;
        }
    }

    /**
     * 从用户移除角色
     */
    public boolean removeRoleFromUser(Long userId, String roleName) {
        try {
            // 首先通过角色名获取角色
            Role role = roleDao.findByName(roleName);
            if (role == null) {
                logger.error("角色不存在: {}", roleName);
                return false;
            }

            return userRoleDao.removeRoleFromUser(userId, role.getRoleId());
        } catch (Exception e) {
            logger.error("从用户移除角色失败，用户ID: {}, 角色名: {}", userId, roleName, e);
            return false;
        }
    }

    /**
     * 为角色分配权限
     */
    public boolean assignPermissionToRole(String roleName, String permissionName) {
        try {
            // 获取角色
            Role role = roleDao.findByName(roleName);
            if (role == null) {
                logger.error("角色不存在: {}", roleName);
                return false;
            }

            // 获取权限
            Permission permission = permissionDao.findByName(permissionName);
            if (permission == null) {
                logger.error("权限不存在: {}", permissionName);
                return false;
            }

            return rolePermissionDao.assignPermissionToRole(role.getRoleId(), permission.getPid());
        } catch (Exception e) {
            logger.error("为角色分配权限失败，角色名: {}, 权限名: {}", roleName, permissionName, e);
            return false;
        }
    }

    /**
     * 从角色移除权限
     */
    public boolean removePermissionFromRole(String roleName, String permissionName) {
        try {
            // 获取角色
            Role role = roleDao.findByName(roleName);
            if (role == null) {
                logger.error("角色不存在: {}", roleName);
                return false;
            }

            // 获取权限
            Permission permission = permissionDao.findByName(permissionName);
            if (permission == null) {
                logger.error("权限不存在: {}", permissionName);
                return false;
            }

            return rolePermissionDao.removePermissionFromRole(role.getRoleId(), permission.getPid());
        } catch (Exception e) {
            logger.error("从角色移除权限失败，角色名: {}, 权限名: {}", roleName, permissionName, e);
            return false;
        }
    }

    /**
     * 检查用户是否有特定权限
     */
    public boolean hasPermission(Long userId, String permissionName) {
        String sql = "SELECT COUNT(*) > 0 " +
                "FROM \"User\" u " +
                "JOIN user_role ur ON u.uid = ur.user_id " +
                "JOIN role r ON ur.role_id = r.roid " +
                "JOIN role_permission rp ON r.roid = rp.role_id " +
                "JOIN permission p ON rp.permission_id = p.pid " +
                "WHERE u.uid = ? AND p.name = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, userId);
            stmt.setString(2, permissionName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean(1);
                }
            }
        } catch (SQLException e) {
            logger.error("检查权限时发生错误", e);
        }
        return false;
    }

    /**
     * 创建新角色
     */
    public boolean createRole(String name, String description) {
        try {
            // 检查角色是否已存在
            Role existingRole = roleDao.findByName(name);
            if (existingRole != null) {
                logger.warn("角色已存在: {}", name);
                return false;
            }

            // 创建新角色
            Role role = new Role();
            role.setRoleName(name);
            role.setDescription(description);

            return roleDao.save(role) > 0;
        } catch (Exception e) {
            logger.error("创建角色失败，角色名: {}", name, e);
            return false;
        }
    }

    /**
     * 创建新权限
     */
    public boolean createPermission(String name, String description) {
        try {
            // 检查权限是否已存在
            Permission existingPermission = permissionDao.findByName(name);
            if (existingPermission != null) {
                logger.warn("权限已存在: {}", name);
                return false;
            }

            // 创建新权限
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);

            return permissionDao.save(permission) > 0;
        } catch (Exception e) {
            logger.error("创建权限失败，权限名: {}", name, e);
            return false;
        }
    }

    /**
     * 删除角色
     */
    public boolean deleteRole(String roleName) {
        try {
            // 获取角色
            Role role = roleDao.findByName(roleName);
            if (role == null) {
                logger.error("角色不存在: {}", roleName);
                return false;
            }

            return roleDao.delete(role.getRoleId());
        } catch (Exception e) {
            logger.error("删除角色失败，角色名: {}", roleName, e);
            return false;
        }
    }

    /**
     * 删除权限
     */
    public boolean deletePermission(String permissionName) {
        try {
            // 获取权限
            Permission permission = permissionDao.findByName(permissionName);
            if (permission == null) {
                logger.error("权限不存在: {}", permissionName);
                return false;
            }

            return permissionDao.delete(permission.getPid());
        } catch (Exception e) {
            logger.error("删除权限失败，权限名: {}", permissionName, e);
            return false;
        }
    }

    /**
     * 为用户分配默认角色
     */
    public void assignDefaultRole(Long userId) {
        boolean success = assignRoleToUser(userId, "VIEWER");
        if (success) {
            logger.info("成功为用户分配默认角色");
        } else {
            logger.error("分配角色失败");
        }
    }

    public void debugUserPermissions(Long userId) {
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT p.name FROM user_permission up " +
                             "JOIN permission p ON up.permission_id = p.pid " +
                             "WHERE up.user_id = ?")) {
            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            logger.info("用户 {} 的权限:", userId);
            while (rs.next()) {
                logger.info(" - {}", rs.getString("name"));
            }
        } catch (SQLException e) {
            logger.error("检查用户权限时出错", e);
        }
    }

}