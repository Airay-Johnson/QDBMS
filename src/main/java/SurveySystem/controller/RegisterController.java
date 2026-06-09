package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.User;
import SurveySystem.service.AuthService;
import SurveySystem.service.PermissionService;
import SurveySystem.dao.UserDao;
import Util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterController {
    private static final Logger logger = LoggerFactory.getLogger(RegisterController.class);

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField emailField;
    @FXML
    private CheckBox rememberPasswordCheckbox;

    private final AuthService authService = AuthService.getInstance();
    private final PermissionService permissionService = new PermissionService();
    private final UserDao userDao = new UserDao();

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String email = emailField.getText();

        try {
            // 调用AuthService的注册方法
            boolean success = authService.register(username, password, email);
            if (success) {
                // 注册成功后，获取用户并分配默认角色
                User newUser = userDao.findByUsername(username);
                if (newUser != null) {
                    assignDefaultRole(newUser);
                }
                showAlert("注册成功", "用户注册成功，请登录", Alert.AlertType.INFORMATION);
                MainAPP.showLoginView();
            } else {
                showAlert("注册失败", "用户名可能已被占用或注册信息无效", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            logger.error("注册异常", e);
            showAlert("错误", "注册时发生异常: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void assignDefaultRole(User newUser) {
        // 分配默认角色：USER
        boolean success = permissionService.assignRoleToUser(newUser.getUid(), "USER");

        // 如果是内部邮箱，分配EDITOR角色（可选扩展）
        // if (isInternalEmail(newUser.getEmail())) {
        //     permissionService.assignRoleToUser(newUser.getUid(), "EDITOR");
        // }

        // 如果是第一个用户，分配ADMIN角色
        if (userDao.getTotalUserCount() == 1) {
            permissionService.assignRoleToUser(newUser.getUid(), "ADMIN");
            logger.info("第一个注册用户 {} 被自动设置为管理员", newUser.getUsername());
        }

        if (success) {
            logger.info("成功为用户 {} 分配默认角色", newUser.getUsername());
        } else {
            logger.error("为用户 {} 分配角色失败", newUser.getUsername());
        }
    }

    private boolean isInternalEmail(String email) {
        // 请将"yourcompany.com"替换为实际的域名
        return email != null && email.endsWith("@yourcompany.com");
    }

    @FXML
    private void handleBackToLogin() {
        MainAPP.showLoginView();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}