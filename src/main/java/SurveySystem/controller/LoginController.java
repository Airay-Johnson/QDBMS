package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.User;
import SurveySystem.model.UserContext;
import SurveySystem.service.AuthService;
import Util.AutoLoginUtil;
import Util.EncryptionUtil;
import Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

public class LoginController {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

    @FXML
    private ComboBox<String> userSelector;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private CheckBox rememberPasswordCheckbox;
    @FXML
    private CheckBox autoLoginCheckbox;

    private AuthService authService;
    private List<AutoLoginUtil.SavedUser> savedUsers;

    @FXML
    public void initialize() {
        authService = AuthService.getInstance();

        // 加载保存的用户信息
        loadSavedUsers();

        // 尝试自动登录
        attemptAutoLogin();
    }

    private void loadSavedUsers() {
        // 读取保存的用户信息
        savedUsers = AutoLoginUtil.readAutoLoginFile();

        if (savedUsers != null && !savedUsers.isEmpty()) {
            // 填充用户选择器
            List<String> usernames = savedUsers.stream()
                    .map(AutoLoginUtil.SavedUser::toString)
                    .collect(Collectors.toList());

            userSelector.setItems(FXCollections.observableArrayList(usernames));

            // 添加选择监听器
            userSelector.getSelectionModel().selectedItemProperty().addListener(
                    (observable, oldValue, newValue) -> {
                        if (newValue != null) {
                            // 提取用户名（去掉后面的自动登录标记）
                            String username = newValue.replace(" (自动登录)", "");

                            // 查找对应的用户信息
                            AutoLoginUtil.SavedUser selectedUser = savedUsers.stream()
                                    .filter(user -> user.getUsername().equals(username))
                                    .findFirst()
                                    .orElse(null);

                            if (selectedUser != null) {
                                usernameField.setText(selectedUser.getUsername());

                                // 如果用户选择了记住密码，则填充密码
                                if (selectedUser.isRememberPassword()) {
                                    String decryptedPassword = EncryptionUtil.decrypt(selectedUser.getEncryptedPassword());
                                    passwordField.setText(decryptedPassword);
                                } else {
                                    passwordField.clear();
                                }

                                rememberPasswordCheckbox.setSelected(selectedUser.isRememberPassword());
                                autoLoginCheckbox.setSelected(selectedUser.isAutoLoginEnabled());
                            }
                        }
                    });
        }
    }

    private void attemptAutoLogin() {
        // 尝试自动登录
        if (authService.autoLogin()) {
            User user = authService.getCurrentUser();
            if (user != null) {
                // 设置会话
                SessionManager.setCurrentUser(user);
                UserContext.setCurrentUser(user);
                logger.info("用户 {} 自动登录成功", user.getUsername());
                MainAPP.showDashboardView();
            }
        }
    }

    /**
     * 处理登录按钮点击
     */
    @FXML
    private void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();
        boolean rememberPassword = rememberPasswordCheckbox.isSelected();
        boolean autoLogin = autoLoginCheckbox.isSelected();

        logger.debug("登录尝试 - 用户名: {}, 记住密码: {}, 自动登录: {}",
                username, rememberPassword, autoLogin);

        try {
            // 使用 authService 进行认证
            User user = authService.authenticate(username, password);
            if (user != null) {
                // 设置会话
                SessionManager.setCurrentUser(user);
                UserContext.setCurrentUser(user);

                // 保存登录信息
                authService.saveLoginStatus(username, password, rememberPassword, autoLogin);

                logger.info("用户 {} 登录成功", username);

                // 跳转到主界面
                MainAPP.showDashboardView();
            } else {
                showAlert("登录失败", "用户名或密码错误", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            logger.error("登录异常", e);
            showAlert("错误", "登录时发生异常: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    /**
     * 处理清除按钮点击
     */
    @FXML
    private void handleClearSaved() {
        String username = usernameField.getText();
        if (username != null && !username.isEmpty()) {
            boolean removed = AutoLoginUtil.deleteUserLoginInfo(username);
            if (removed) {
                showAlert("成功", "已删除用户 " + username + " 的保存信息", Alert.AlertType.INFORMATION);
                // 重新加载保存的用户
                loadSavedUsers();
            } else {
                showAlert("提示", "未找到用户 " + username + " 的保存信息", Alert.AlertType.INFORMATION);
            }
        } else {
            showAlert("提示", "请输入用户名", Alert.AlertType.WARNING);
        }
    }

    /**
     * 处理注册按钮点击
     */
    @FXML
    private void handleRegister() {
        MainAPP.showRegisterView();
    }

    /**
     * 显示警告对话框
     */
    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}