// 文件路径：Util/StorageUtil.java
package Util;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * 本地存储工具类（使用Java Preferences API）
 */
public class StorageUtil {
    private static final String PREFS_NODE = "SurveySystem/LoginStatus";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "encryptedPassword";

    /**
     * 保存登录状态到本地
     */
    public static void saveLoginStatus(String username, String encryptedPassword) {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        prefs.put(KEY_USERNAME, username);
        prefs.put(KEY_PASSWORD, encryptedPassword);
    }

    /**
     * 读取本地保存的登录状态
     * @return 包含username和encryptedPassword的Map，无数据时返回null
     */
    public static Map<String, String> loadLoginStatus() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        String username = prefs.get(KEY_USERNAME, null);
        String password = prefs.get(KEY_PASSWORD, null);

        if (username != null && password != null) {
            Map<String, String> status = new HashMap<>();
            status.put(KEY_USERNAME, username);
            status.put(KEY_PASSWORD, password);
            return status;
        }
        return null;
    }

    /**
     * 清除本地保存的登录状态
     */
    public static void clearLoginStatus() {
        Preferences prefs = Preferences.userRoot().node(PREFS_NODE);
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_PASSWORD);
    }
}