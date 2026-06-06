package UI;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AuthUtil {

    // 模拟数据库存储的用户名和密码（实际应用应从数据库获取）
    private static final String VALID_USERNAME = "admin";
    private static final String VALID_PASSWORD_HASH = "jZae727K08KaOmKSgOaGzww/XVqGr/PKEgIMkjrcbJI="; // "123456"的SHA-256哈希值

    // 用户认证方法
    public static boolean authenticate(String username, String password) {
        // 1. 验证用户名
        if (!VALID_USERNAME.equals(username)) {
            return false;
        }

        // 2. 验证密码（对比哈希值）
        String inputHash = hashPassword(password);
        return VALID_PASSWORD_HASH.equals(inputHash);
    }

    // 密码哈希处理方法（SHA-256）
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
}