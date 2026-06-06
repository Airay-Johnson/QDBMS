// PermissionInterceptor.java
package Util;

import javafx.scene.control.Alert;
import SurveySystem.service.AuthService;

public class PermissionInterceptor {

    private static AuthService authService=AuthService.getInstance();
    public static boolean checkPermission(String permission) {
        if (authService.getCurrentUser() == null) {
            showAlert("请先登录");
            return false;
        }

        if (!authService.hasPermission(authService.getCurrentUser().getUid(), permission)) {
            showAlert("权限不足");
            return false;
        }

        return true;
    }

    public static void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("权限警告");
        alert.setContentText(message);
        alert.showAndWait();
    }
}