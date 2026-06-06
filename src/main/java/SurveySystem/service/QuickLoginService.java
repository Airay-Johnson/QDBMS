package SurveySystem.service;

import SurveySystem.model.User;
import Util.DBUtil;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.Optional;

/**
 * 用户快捷登录服务类
 * 支持：密码验证登录、记住密码（自动登录）、清除登录状态
 */
public class QuickLoginService {

    // 存储自动登录信息的本地文件（建议放在项目根目录或用户目录）
    private static final String AUTO_LOGIN_FILE = "auto_login.info";

    /**
     * 用户名密码登录
     * @param username 用户名
     * @param password 密码（明文，实际项目建议传输前加密）
     * @param remember 是否记住登录状态
     * @return 登录结果（包含用户信息或错误消息）
     */
    public LoginResult login(String username, String password, boolean remember) {
        // 1. 参数校验
        if (username == null || username.trim().isEmpty() || password == null) {
            return new LoginResult(false, "用户名或密码不能为空", null);
        }

        // 2. 数据库验证
        User user = verifyUser(username, password);
        if (user == null) {
            return new LoginResult(false, "用户名或密码错误", null);
        }

        // 3. 记住登录状态（加密存储到本地文件）
        if (remember) {
            saveAutoLoginInfo(username, password);
        } else {
            clearAutoLoginInfo(); // 不记住则清除之前的记录
        }

        return new LoginResult(true, "登录成功", user);
    }

    /**
     * 自动登录（从本地文件读取保存的凭证）
     * @return 登录结果
     */
    public LoginResult autoLogin() {
        try {
            // 1. 读取本地存储的登录信息
            String content = new String(Files.readAllBytes(Paths.get(AUTO_LOGIN_FILE)));
            String[] credentials = decrypt(content).split(":", 2);
            if (credentials.length != 2) {
                return new LoginResult(false, "自动登录信息损坏", null);
            }

            // 2. 验证凭证
            User user = verifyUser(credentials[0], credentials[1]);
            return user != null
                    ? new LoginResult(true, "自动登录成功", user)
                    : new LoginResult(false, "自动登录失败，请重新登录", null);
        } catch (Exception e) {
            // 文件不存在或读取失败（首次登录/未记住密码时正常）
            return new LoginResult(false, "无自动登录信息", null);
        }
    }

    /**
     * 清除自动登录状态（删除本地存储的凭证）
     */
    public void clearAutoLoginInfo() {
        try {
            Files.deleteIfExists(Paths.get(AUTO_LOGIN_FILE));
        } catch (Exception e) {
            System.err.println("清除自动登录信息失败：" + e.getMessage());
        }
    }

    /**
     * 数据库验证用户凭证
     */
    private User verifyUser(String username, String password) {
        String sql = "SELECT * FROM \"user\" WHERE username = ? AND password = ?"; // 注意表名与字段名是否匹配你的库

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // 实际项目建议用MD5/BCrypt加密后再查询
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                User user = new User();
                user.setUid(rs.getLong("id"));
                user.setUsername(rs.getString("username"));
                user.setRole(rs.getString("role")); // 假设用户表有角色字段
                // 按需设置其他用户信息（如昵称、权限等）
                return user;
            }
        } catch (SQLException e) {
            System.err.println("用户验证失败：" + e.getMessage());
        }
        return null;
    }

    /**
     * 保存自动登录信息（简单加密存储，实际项目建议用更安全的加密方式）
     */
    private void saveAutoLoginInfo(String username, String password) {
        try {
            String content = encrypt(username + ":" + password);
            Files.write(
                    Paths.get(AUTO_LOGIN_FILE),
                    content.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );
        } catch (Exception e) {
            System.err.println("保存自动登录信息失败：" + e.getMessage());
        }
    }

    /**
     * 简单加密（Base64，防明文泄露，实际项目建议用AES等加密算法）
     */
    private String encrypt(String content) {
        return Base64.getEncoder().encodeToString(content.getBytes());
    }

    /**
     * 解密（对应encrypt方法）
     */
    private String decrypt(String encrypted) {
        return new String(Base64.getDecoder().decode(encrypted));
    }

    /**
     * 登录结果封装类
     */
    public static class LoginResult {
        private boolean success;
        private String message;
        private User user;

        public LoginResult(boolean success, String message, User user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        // Getter方法
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public User getUser() { return user; }
    }
}