package SurveySystem.model;

import SurveySystem.model.User;

/**
 * 用户上下文类，用于管理当前登录用户状态
 */
public class UserContext {
    private static User currentUser;

    /**
     * 获取当前登录用户
     */
    public static User getCurrentUser() {
        return currentUser;
    }

    /**
     * 设置当前登录用户
     */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    /**
     * 清除当前用户（用于登出）
     */
    public static void clear() {
        currentUser = null;
    }

    /**
     * 检查用户是否已登录
     */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}