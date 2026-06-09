package Util;

import SurveySystem.constant.PermissionType;
import SurveySystem.model.User;
import SurveySystem.model.UserContext;
import SurveySystem.service.PermissionService;

import java.util.prefs.Preferences;

public class SessionManager {
    private static final String USER_KEY = "current_user";
    private static final String REMEMBER_ME_KEY = "remember_me";
    private static final Preferences prefs = Preferences.userNodeForPackage(SessionManager.class);
    private static PermissionService permissionService = new PermissionService();
    public static User currentUser;

    public static void login(User user, boolean rememberMe) {
        currentUser = user;
        if (rememberMe) {
            String userJson = JsonUtil.toJson(user);
            prefs.put(USER_KEY, userJson);
            prefs.putBoolean(REMEMBER_ME_KEY, true);
        }
    }

    public static void logout() {
        currentUser = null;
        prefs.remove(USER_KEY);
        prefs.remove(REMEMBER_ME_KEY);
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
        // 确保也设置到 UserContext（如果使用）
        UserContext.setCurrentUser(user);
    }

    public static User getCurrentUser() {
        if (currentUser == null) {
            String userJson = prefs.get(USER_KEY, null);
            if (userJson != null) {
                currentUser = JsonUtil.fromJson(userJson, User.class);
            }
        }
        return currentUser;
    }

    public static boolean isRemembered() {
        return prefs.getBoolean(REMEMBER_ME_KEY, false);
    }

    public static boolean hasPermission(PermissionType permission) {
        User user = getCurrentUser();
        if (user == null) return false;

        // 修复：使用正确的参数类型
        return permissionService.hasPermission(user.getUid(), permission.name());
    }
}