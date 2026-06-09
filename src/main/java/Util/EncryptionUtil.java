package Util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.logging.Logger;

public class EncryptionUtil {
    private static final Logger logger = Logger.getLogger(EncryptionUtil.class.getName());
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    // 使用SHA-256哈希生成固定长度的密钥
    private static final String KEY_SOURCE = "Survey__2025_Key";
    private static SecretKeySpec secretKey;

    static {
        try {
            // 使用SHA-256哈希生成固定长度的密钥
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(KEY_SOURCE.getBytes(StandardCharsets.UTF_8));
            key = java.util.Arrays.copyOf(key, 16); // 只取前16字节作为AES-128密钥
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            logger.severe("初始化加密密钥失败: " + e.getMessage());
        }
    }

    public static String encrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            logger.severe("加密失败: " + e.getMessage());
            return null;
        }
    }

    public static String decrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedValue = Base64.getDecoder().decode(value);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return new String(decryptedValue, StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.severe("解密失败: " + e.getMessage());
            return null;
        }
    }
}