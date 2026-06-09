package Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

public class AutoLoginUtil {
    private static final Logger logger = Logger.getLogger(AutoLoginUtil.class.getName());
    private static final String AUTO_LOGIN_DIR = System.getProperty("user.home") + "/.survey_db";
    private static final String AUTO_LOGIN_FILE = AUTO_LOGIN_DIR + "/auto_login.info";
    private static final Preferences PREFS = Preferences.userNodeForPackage(AutoLoginUtil.class);

    // SavedUser 内部类
    public static class SavedUser {
        private String username;
        private String encryptedPassword;
        private boolean rememberPassword;
        private boolean autoLoginEnabled;

        public SavedUser(String username, String encryptedPassword,
                         boolean rememberPassword, boolean autoLoginEnabled) {
            this.username = username;
            this.encryptedPassword = encryptedPassword;
            this.rememberPassword = rememberPassword;
            this.autoLoginEnabled = autoLoginEnabled;
        }

        // Getter 方法
        public String getUsername() { return username; }
        public String getEncryptedPassword() { return encryptedPassword; }
        public boolean isRememberPassword() { return rememberPassword; }
        public boolean isAutoLoginEnabled() { return autoLoginEnabled; }

        @Override
        public String toString() {
            return username + (autoLoginEnabled ? " (自动登录)" : "");
        }
    }

    // 创建自动登录文件
    public static boolean createAutoLoginFile(String username, String password) {
        try {
            // 加密凭据
            String encryptedUsername = EncryptionUtil.encrypt(username);
            String encryptedPassword = EncryptionUtil.encrypt(password);

            if (encryptedUsername == null || encryptedPassword == null) {
                logger.warning("加密失败，无法创建自动登录文件");
                return false;
            }

            // 创建目录
            Path dirPath = Paths.get(AUTO_LOGIN_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 写入文件
            Path filePath = Paths.get(AUTO_LOGIN_FILE);
            String content = encryptedUsername + ":" + encryptedPassword;
            Files.write(filePath, content.getBytes());

            logger.info("自动登录文件创建成功");
            return true;
        } catch (IOException e) {
            logger.severe("创建自动登录文件失败: " + e.getMessage());
            return false;
        }
    }

    // 读取自动登录文件 - 返回 SavedUser 列表
    public static List<SavedUser> readAutoLoginFile() {
        List<SavedUser> savedUsers = new ArrayList<>();
        try {
            Path filePath = Paths.get(AUTO_LOGIN_FILE);
            if (!Files.exists(filePath)) {
                logger.info("自动登录文件不存在");
                return savedUsers;
            }

            // 读取所有行
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                String[] parts = line.split(":");
                if (parts.length == 4) {
                    String username = EncryptionUtil.decrypt(parts[0]);
                    String encryptedPassword = parts[1]; // 已经是加密的
                    boolean rememberPassword = Boolean.parseBoolean(parts[2]);
                    boolean autoLoginEnabled = Boolean.parseBoolean(parts[3]);

                    if (username != null) {
                        savedUsers.add(new SavedUser(username, encryptedPassword,
                                rememberPassword, autoLoginEnabled));
                    }
                }
            }

            logger.info("成功读取自动登录文件，找到 " + savedUsers.size() + " 个用户");
            return savedUsers;
        } catch (IOException e) {
            logger.severe("读取自动登录文件失败: " + e.getMessage());
            return savedUsers;
        }
    }

    // 保存用户登录信息
    public static void saveUserLoginInfo(String username, String password,
                                         boolean rememberPassword, boolean autoLoginEnabled) {
        try {
            // 读取现有用户
            List<SavedUser> savedUsers = readAutoLoginFile();

            // 移除同名用户（如果存在）
            savedUsers.removeIf(user -> user.getUsername().equals(username));

            // 加密密码
            String encryptedPassword = EncryptionUtil.encrypt(password);
            if (encryptedPassword == null) {
                logger.warning("密码加密失败");
                return;
            }

            // 添加新用户
            savedUsers.add(new SavedUser(username, encryptedPassword, rememberPassword, autoLoginEnabled));

            // 创建目录（如果不存在）
            Path dirPath = Paths.get(AUTO_LOGIN_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 写入文件
            Path filePath = Paths.get(AUTO_LOGIN_FILE);
            List<String> lines = new ArrayList<>();
            for (SavedUser user : savedUsers) {
                String encryptedUsername = EncryptionUtil.encrypt(user.getUsername());
                String line = encryptedUsername + ":" + user.getEncryptedPassword() + ":" +
                        user.isRememberPassword() + ":" + user.isAutoLoginEnabled();
                lines.add(line);
            }

            Files.write(filePath, lines);
            logger.info("用户登录信息已保存: " + username);
        } catch (IOException e) {
            logger.severe("保存用户登录信息失败: " + e.getMessage());
        }
    }

    // 获取自动登录用户
    public static SavedUser getAutoLoginUser() {
        List<SavedUser> savedUsers = readAutoLoginFile();
        for (SavedUser user : savedUsers) {
            if (user.isAutoLoginEnabled()) {
                return user;
            }
        }
        return null;
    }

    // 删除用户登录信息
    public static boolean deleteUserLoginInfo(String username) {
        try {
            List<SavedUser> savedUsers = readAutoLoginFile();
            int initialSize = savedUsers.size();

            // 移除指定用户
            savedUsers.removeIf(user -> user.getUsername().equals(username));

            if (savedUsers.size() < initialSize) {
                // 重新写入文件
                Path filePath = Paths.get(AUTO_LOGIN_FILE);
                List<String> lines = new ArrayList<>();
                for (SavedUser user : savedUsers) {
                    String encryptedUsername = EncryptionUtil.encrypt(user.getUsername());
                    String line = encryptedUsername + ":" + user.getEncryptedPassword() + ":" +
                            user.isRememberPassword() + ":" + user.isAutoLoginEnabled();
                    lines.add(line);
                }

                Files.write(filePath, lines);
                logger.info("用户登录信息已删除: " + username);
                return true;
            }

            return false;
        } catch (IOException e) {
            logger.severe("删除用户登录信息失败: " + e.getMessage());
            return false;
        }
    }

    // 删除自动登录文件
    public static boolean deleteAutoLoginFile() {
        try {
            Path filePath = Paths.get(AUTO_LOGIN_FILE);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                logger.info("自动登录文件已删除");
            }
            return true;
        } catch (IOException e) {
            logger.severe("删除自动登录文件失败: " + e.getMessage());
            return false;
        }
    }

    // 保存登录信息到Preferences（备用方案）
    public static void saveLoginInfoToPreferences(String username, String password, boolean rememberMe) {
        if (rememberMe) {
            String encryptedUsername = EncryptionUtil.encrypt(username);
            String encryptedPassword = EncryptionUtil.encrypt(password);

            if (encryptedUsername != null && encryptedPassword != null) {
                PREFS.put("username", encryptedUsername);
                PREFS.put("password", encryptedPassword);
                PREFS.putBoolean("rememberMe", true);
            }
        } else {
            PREFS.remove("username");
            PREFS.remove("password");
            PREFS.putBoolean("rememberMe", false);
        }
    }

    // 从Preferences获取登录信息（备用方案）
    public static Map<String, String> getLoginInfoFromPreferences() {
        boolean rememberMe = PREFS.getBoolean("rememberMe", false);
        if (!rememberMe) {
            return null;
        }

        String encryptedUsername = PREFS.get("username", "");
        String encryptedPassword = PREFS.get("password", "");

        if (encryptedUsername.isEmpty() || encryptedPassword.isEmpty()) {
            return null;
        }

        String username = EncryptionUtil.decrypt(encryptedUsername);
        String password = EncryptionUtil.decrypt(encryptedPassword);

        if (username == null || password == null) {
            return null;
        }

        Map<String, String> loginInfo = new HashMap<>();
        loginInfo.put("username", username);
        loginInfo.put("password", password);

        return loginInfo;
    }
}