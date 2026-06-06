package SurveySystem.service;

import SurveySystem.dao.RoleDao;
import SurveySystem.dao.RolePermissionDao;
import SurveySystem.dao.UserDao;
import SurveySystem.dao.UserPermissionDao;
import SurveySystem.dao.UserRoleDao;
import SurveySystem.model.Permission;
import SurveySystem.model.Role;
import SurveySystem.model.User;
import Util.AutoLoginUtil;
import Util.EncryptionUtil;
import Util.DBUtil;
import Util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * 认证服务类，处理登录、权限检查、自动登录等功能
 */
public class AuthService {
    static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private User currentUser; // 当前登录用户 - 改为实例变量
    private static AuthService instance;

    // DAO依赖
    private final UserDao userDao;
    private final UserPermissionDao userPermissionDao;
    private final RolePermissionDao rolePermissionDao;
    private final UserRoleDao userRoleDao;
    private final RoleDao roleDao;

    // 单例模式
    public AuthService() {
        this.userDao = new UserDao();
        this.userPermissionDao = new UserPermissionDao();
        this.rolePermissionDao = new RolePermissionDao();
        this.userRoleDao = new UserRoleDao();
        this.roleDao = new RoleDao();
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    /**
     * 普通登录：验证用户名密码并返回登录结果
     * @param username 用户名
     * @param password 密码
     * @param rememberPassword 是否记住密码
     * @param autoLogin 是否自动登录
     * @return 登录是否成功
     */
    public boolean login(String username, String password, boolean rememberPassword, boolean autoLogin) {
        if (username == null || password == null) {
            logger.warn("登录失败：用户名或密码为空");
            return false;
        }

        User user = userDao.findByUsername(username);
        if (user == null) {
            logger.info("登录失败：用户不存在 - {}", username);
            return false;
        }

        // 调试信息：输出密码验证详情
        logger.debug("验证密码: 输入={}, 存储={}", password, user.getPassword());

        if (PasswordUtil.verifyPassword(password, user.getPassword())) {
            currentUser = user;
            logger.info("登录成功：{}（UID={}）", username, user.getUid());

            // 如果是旧版SHA-256哈希，升级到bcrypt
            upgradePasswordIfNeeded(user, password);

            // 保存登录状态
            saveLoginStatus(username, password, rememberPassword, autoLogin);
            return true;
        } else {
            logger.warn("登录失败：密码错误 - {}", username);
            return false;
        }
    }

    /**
     * 如果用户密码还是旧版SHA-256哈希，升级到bcrypt
     */
    private void upgradePasswordIfNeeded(User user, String password) {
        String stored = user.getPassword();
        if (stored == null || stored.startsWith("$2a$") || stored.startsWith("$2b$")) {
            return; // 已经是bcrypt，无需升级
        }
        // 旧版SHA-256哈希，升级到bcrypt
        String bcryptHash = PasswordUtil.upgradeToBcrypt(password);
        String sql = "UPDATE \"User\" SET password = ? WHERE uid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, bcryptHash);
            stmt.setLong(2, user.getUid());
            stmt.executeUpdate();
            user.setPassword(bcryptHash);
            logger.info("用户 {} 密码已从旧版SHA-256升级到bcrypt", user.getUsername());
        } catch (SQLException e) {
            logger.error("密码升级失败: {}", e.getMessage());
        }
    }

    /**
     * 验证用户凭据并返回用户对象
     * @param username 用户名
     * @param password 密码
     * @return 验证成功返回用户对象，否则返回null
     */
    public User authenticate(String username, String password) {
        if (username == null || password == null) {
            logger.warn("认证失败：用户名或密码为空");
            return null;
        }

        User user = userDao.findByUsername(username);
        if (user == null) {
            logger.info("认证失败：用户不存在 - {}", username);
            return null;
        }

        if (PasswordUtil.verifyPassword(password, user.getPassword())) {
            logger.info("认证成功：{}（UID={}）", username, user.getUid());
            // 如果是旧版SHA-256哈希，升级到bcrypt
            upgradePasswordIfNeeded(user, password);
            return user;
        } else {
            logger.warn("认证失败：密码错误 - {}", username);
            return null;
        }
    }

    /**
     * 快速登录：使用保存的凭据登录
     * @param username 用户名
     * @param encryptedPassword 加密的密码
     * @return 登录是否成功
     */
    public boolean quickLogin(String username, String encryptedPassword) {
        if (username == null || encryptedPassword == null) {
            logger.warn("快速登录失败：用户名或密码为空");
            return false;
        }

        // 解密密码
        String password = EncryptionUtil.decrypt(encryptedPassword);
        if (password == null) {
            logger.warn("快速登录失败：密码解密失败");
            return false;
        }

        logger.info("尝试快速登录，用户名: {}", username);

        // 使用 authenticate 方法验证用户凭据
        User user = authenticate(username, password);
        if (user != null) {
            currentUser = user;
            logger.info("快速登录成功：{}（UID={}）", username, user.getUid());
            return true;
        } else {
            logger.warn("快速登录失败：凭据验证失败");
            return false;
        }
    }

    /**
     * 自动登录：尝试使用保存的自动登录凭据
     * @return 登录是否成功
     */
    public boolean autoLogin() {
        try {
            // 获取启用了自动登录的用户
            AutoLoginUtil.SavedUser autoLoginUser = AutoLoginUtil.getAutoLoginUser();

            if (autoLoginUser == null) {
                logger.info("自动登录失败：未找到自动登录用户");
                return false;
            }

            return quickLogin(autoLoginUser.getUsername(), autoLoginUser.getEncryptedPassword());
        } catch (Exception e) {
            logger.error("自动登录过程中发生异常: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 保存登录状态
     */
    public void saveLoginStatus(String username, String password, boolean rememberPassword, boolean autoLogin) {
        if (username == null || password == null) {
            logger.warn("保存登录状态失败：用户名或密码为空");
            return;
        }

        AutoLoginUtil.saveUserLoginInfo(username, password, rememberPassword, autoLogin);
    }

    /**
     * 用户注册
     */
    public boolean register(String username, String password, String email) {
        if (username == null || username.trim().isEmpty()
                || password == null || password.trim().isEmpty()
                || email == null || email.trim().isEmpty()) {
            logger.warn("注册失败：用户名、密码、邮箱不能为空");
            return false;
        }

        if (userDao.findByUsername(username) != null) {
            logger.warn("注册失败：用户名 '{}' 已被占用", username);
            return false;
        }

        String encryptedPassword = PasswordUtil.encryptPassword(password);
        if (encryptedPassword == null) {
            logger.warn("注册失败：密码加密出错");
            return false;
        }

        String sql = "INSERT INTO \"User\" (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, encryptedPassword);
            pstmt.setString(3, email);
            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                logger.info("用户注册成功：{}", username);
                return true;
            }
            logger.warn("用户注册失败：数据库插入失败");
            return false;
        } catch (SQLException e) {
            logger.error("注册时数据库操作出错：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 权限检查
     */
    public boolean hasPermission(Long userId, String permissionName) {
        if (userId == null || permissionName == null || permissionName.isEmpty()) {
            logger.warn("权限检查失败：参数无效");
            return false;
        }

        // 1. 检查用户直接权限
        List<Permission> directPermissions = userPermissionDao.getDirectPermissions(userId);
        for (Permission p : directPermissions) {
            if (permissionName.equals(p.getName())) {
                return true;
            }
        }

        // 2. 检查用户角色关联的权限
        List<Long> roleIds = userRoleDao.getRoleIdsByUserId(userId);
        for (Long roleId : roleIds) {
            List<Permission> rolePermissions = rolePermissionDao.getRolePermissions(roleId);
            for (Permission p : rolePermissions) {
                if (permissionName.equals(p.getName())) {
                    return true;
                }
            }
        }

        return false;
    }

    // 其他工具方法
    public void logout() {
        if (currentUser != null) {
            logger.info("用户已退出登录：{}", currentUser.getUsername());
        }
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        if (currentUser == null) return false;
        // 从数据库查询用户角色列表
        List<Role> roles = roleDao.findRolesByUserId(currentUser.getUid());
        return roles.stream().anyMatch(r -> "ADMIN".equals(r.getRoleName()));
    }
}