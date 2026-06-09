package Util;

import org.mindrot.jbcrypt.BCrypt;

import java.security.MessageDigest;
import java.util.Base64;
import java.util.logging.Logger;

public class PasswordUtil {
    private static final Logger logger = Logger.getLogger(PasswordUtil.class.getName());

    // 使用 bcrypt 加密密码
    public static String encryptPassword(String password) {
        if (password == null) return null;
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // 验证密码（兼容 bcrypt 和旧的 SHA-256 哈希）
    public static boolean verifyPassword(String inputPassword, String storedPassword) {
        if (inputPassword == null || storedPassword == null) return false;

        // 1. 尝试 bcrypt 验证
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$")) {
            try {
                return BCrypt.checkpw(inputPassword, storedPassword);
            } catch (Exception e) {
                logger.warning("BCrypt验证异常: " + e.getMessage());
            }
        }

        // 2. 尝试旧版 SHA-256 验证（兼容老数据）
        try {
            String sha256Hash = sha256Hex(inputPassword);
            if (sha256Hash.equalsIgnoreCase(storedPassword)) {
                logger.info("旧版SHA-256密码验证成功，准备升级到bcrypt");
                return true;
            }

            // 3. 尝试旧版 Base64 SHA-256 验证
            String sha256Base64 = sha256Base64(inputPassword);
            if (sha256Base64.equals(storedPassword)) {
                logger.info("旧版Base64 SHA-256密码验证成功，准备升级到bcrypt");
                return true;
            }
        } catch (Exception e) {
            logger.warning("SHA-256验证异常: " + e.getMessage());
        }

        return false;
    }

    /**
     * 将密码升级为 bcrypt 哈希（用于旧密码迁移）
     */
    public static String upgradeToBcrypt(String password) {
        if (password == null) return null;
        return BCrypt.hashpw(password, BCrypt.gensalt());
    }

    // SHA-256 十六进制编码
    private static String sha256Hex(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) hexString.append(String.format("%02x", b));
        return hexString.toString();
    }

    // SHA-256 Base64 编码
    private static String sha256Base64(String input) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }
}
