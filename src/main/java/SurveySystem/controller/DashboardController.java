package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.constant.PermissionType;
import SurveySystem.model.Questionnaire;
import SurveySystem.model.Role;
import SurveySystem.model.User;
import SurveySystem.model.UserContext;
import SurveySystem.service.PermissionService;
import SurveySystem.service.QuestionnaireService;
import Util.PermissionInterceptor;
import Util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import static SurveySystem.MainAPP.showAlert;

public class DashboardController implements Initializable {

    @FXML private Button createSurveyButton;
    @FXML private Button editSurveyButton;
    @FXML private Button viewResponsesButton;
    @FXML private Button dataAnalysisButton;
    @FXML private MenuItem userManagementMenuItem;
    @FXML private MenuItem permissionManagementMenuItem;
    @FXML private Label welcomeLabel;
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    PermissionService permissionService = new PermissionService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return;
    // 调试：输出用户权限
        permissionService.debugUserPermissions(currentUser.getUid());

        // 根据用户权限设置界面
        setupInterfaceBasedOnPermissions(currentUser);
    }

    private boolean isAdmin(User user) {
        // 检查用户是否是管理员
        // 这里可以根据您的业务逻辑实现
        List<Role> roles = permissionService.getUserRoles(user.getUid());
        return roles.stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role.getRoleName()));
    }

    private void setAllButtonsVisible(boolean visible) {
        if (createSurveyButton != null) createSurveyButton.setVisible(visible);
        if (editSurveyButton != null) editSurveyButton.setVisible(visible);
        if (viewResponsesButton != null) viewResponsesButton.setVisible(visible);
        if (dataAnalysisButton != null) dataAnalysisButton.setVisible(visible);
        if (userManagementMenuItem != null) userManagementMenuItem.setVisible(visible);
        if (permissionManagementMenuItem != null) permissionManagementMenuItem.setVisible(visible);
    }

    private void setupInterfaceBasedOnPermissions(User user) {
        PermissionService permissionService = new PermissionService();
    // 如果是管理员，显示所有功能
        if (isAdmin(user)) {
            setAllButtonsVisible(true);
            return;
        }


        // 检查各项权限
        boolean canCreate = permissionService.hasPermission(user.getUid(), PermissionType.SURVEY_CREATE.name());
        boolean canEdit = permissionService.hasPermission(user.getUid(), PermissionType.SURVEY_EDIT.name());
        boolean canViewResponses = permissionService.hasPermission(user.getUid(), PermissionType.RESPONSE_VIEW.name());
        boolean canAnalyzeData = permissionService.hasPermission(user.getUid(), PermissionType.DATA_ANALYZE.name());
        boolean canManageUsers = permissionService.hasPermission(user.getUid(), PermissionType.USER_MANAGE.name());
        boolean canGrantPermissions = permissionService.hasPermission(user.getUid(), PermissionType.PERMISSION_GRANT.name());

        // 添加调试输出
        logger.info("用户权限检查结果 - 创建: {}, 编辑: {}, 查看回答: {}, 分析数据: {}, 管理用户: {}, 授权: {}",
                canCreate, canEdit, canViewResponses, canAnalyzeData, canManageUsers, canGrantPermissions);

        // 设置按钮可见性
        if (createSurveyButton != null) {
            createSurveyButton.setVisible(canCreate);
            logger.info("设置创建问卷按钮可见性: {}", canCreate);
        }
        if (editSurveyButton != null) {
            editSurveyButton.setVisible(canEdit);
            logger.info("设置编辑问卷按钮可见性: {}", canEdit);
        }
        if (viewResponsesButton != null) {
            viewResponsesButton.setVisible(canViewResponses);
            logger.info("设置查看回答按钮可见性: {}", canViewResponses);
        }
        if (dataAnalysisButton != null) {
            dataAnalysisButton.setVisible(canAnalyzeData);
            logger.info("设置数据分析按钮可见性: {}", canAnalyzeData);
        }
        if (userManagementMenuItem != null) {
            userManagementMenuItem.setVisible(canManageUsers);
            logger.info("设置用户管理菜单项可见性: {}", canManageUsers);
        }
        if (permissionManagementMenuItem != null) {
            permissionManagementMenuItem.setVisible(canGrantPermissions);
            logger.info("设置权限管理菜单项可见性: {}", canGrantPermissions);
        }

        // 设置欢迎消息
        String roleName = getPrimaryRoleName(user.getUid());
        if (welcomeLabel != null) {
            welcomeLabel.setText("欢迎, " + (roleName != null ? roleName + " " : "") + user.getUsername());
        }
    }

    private String getPrimaryRoleName(Long userId) {
        // 获取用户的主要角色名称
        List<Role> roles = permissionService.getUserRoles(userId);
        if (roles.isEmpty()) return null;

        // 简单的实现：返回第一个角色的名称
        return roles.get(0).getRoleName();
    }

    // 加载新窗口的通用方法
    public void loadWindow(String fxmlPath, String title) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                fxmlUrl = getClass().getResource("/view/" + fxmlPath);
                if (fxmlUrl == null) {
                    throw new IOException("FXML文件未找到：" + fxmlPath);
                }
            }

            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            logger.error("加载界面失败", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("加载界面失败" + e.getMessage());
            alert.show();
        }
    }

    @FXML
    private void handleSurveyResponse() {
        if (UserContext.getCurrentUser() == null || SessionManager.getCurrentUser() == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "请先登录！");
            return;
        }

        logger.info("Checking user login status: {}", SessionManager.getCurrentUser() != null ? "logged in" : "not logged in");
        try {
            Long questionnaireId = 1L; // 测试用问卷ID
            QuestionnaireService questionnaireService = new QuestionnaireService();
            Questionnaire questionnaire = questionnaireService.getQuestionnaireWithQuestions(questionnaireId);

            if (questionnaire != null && questionnaire.getQuestions() != null && !questionnaire.getQuestions().isEmpty()) {
                openSurveyResponse(questionnaire);
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "无法加载问卷信息或问卷没有问题");
            }
        } catch (Exception e) {
            logger.error("打开问卷填写界面时发生错误", e);
            showAlert(Alert.AlertType.ERROR, "错误", "打开问卷填写界面时发生错误 " + e.getMessage());
        }
    }

    private void openSurveyResponse(Questionnaire questionnaire) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SurveyResponse.fxml"));
            Parent root = loader.load();

            root.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());

            SurveyResponseController controller = loader.getController();
            controller.setQuestionnaire(questionnaire);
            controller.setCurrentUser(SessionManager.getCurrentUser());

            Stage stage = new Stage();
            stage.setTitle("填写问卷 - " + questionnaire.getTitle());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("无法打开问卷填写界面", e);
            showAlert(Alert.AlertType.ERROR, "错误", "无法打开问卷填写界面: " + e.getMessage());
        }
    }

    // 辅助方法：检查权限并执行操作
    private boolean checkPermissionAndExecute(String permission, Runnable action) {
        if (!PermissionInterceptor.checkPermission(permission)) {
            showAlert(Alert.AlertType.WARNING, "权限不足", "您没有执行此操作的权限");
            return false;
        }
        action.run();
        return true;
    }

    @FXML
    private void handleQuestionnaireManagement(ActionEvent event) {
        loadWindow("/view/questionnaire_manage.fxml", "问卷管理");
    }

    @FXML
    private void handleQuestionManage(ActionEvent event) {
        checkPermissionAndExecute("QUESTION_MANAGE",
                () -> loadWindow("/view/QuestionManage.fxml", "问题管理"));
    }

    @FXML
    private void handleDataCollection(ActionEvent event) {
        loadWindow("/view/DataCollection.fxml", "数据收集");
    }

    @FXML
    private void handleDataAnalysis(ActionEvent event) {
        if (UserContext.getCurrentUser() == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "请先登录！");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/DataAnalysis.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("数据分析");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            logger.error("加载数据分析界面失败", e);
            showAlert(Alert.AlertType.ERROR, "错误", "无法加载数据分析界面");
        }
    }

    @FXML
    private void handleReportExport(ActionEvent event) {
        loadWindow("/view/ReportExport.fxml", "报告导出");
    }

    @FXML
    private void handlePermissionManagement(ActionEvent event) {
        MainAPP.showPermissionManagementView();
    }

    @FXML
    private void handleLogout(ActionEvent event) {
        MainAPP.showLoginView();
    }

    @FXML
    private void handleExit(ActionEvent event) {
        if (event.getSource() instanceof MenuItem source) {
            Stage stage = (Stage) source.getParentPopup().getOwnerWindow();
            stage.close();
        } else if (event.getSource() instanceof javafx.scene.Node node) {
            Stage stage = (Stage) node.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleCreateSurvey(ActionEvent event) {
        loadWindow("/view/questionnaire_manage.fxml", "创建问卷");
    }

    @FXML
    private void handleViewSurveys(ActionEvent event) {
        loadWindow("/view/ViewSurveys.fxml", "查看问卷");
    }

    @FXML
    private void handleEditSurvey(ActionEvent event) {
        loadWindow("/view/EditSurvey.fxml", "编辑问卷");
    }

    @FXML
    private void handleAddQuestion(ActionEvent event) {
        checkPermissionAndExecute("QUESTION_MANAGE",
                () -> loadWindow("/view/AddQuestion.fxml", "添加问题"));
    }

    @FXML
    private void handleManageQuestions(ActionEvent event) {
        checkPermissionAndExecute("QUESTION_MANAGE",
                () -> loadWindow("/view/QuestionBankManage.fxml", "管理问题库"));
    }

    @FXML
    private void handleViewResponses(ActionEvent event) {
        loadWindow("/view/ViewResponses.fxml", "查看回答");
    }

    @FXML
    private void handlePermissionManage(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/Permission_management.fxml"));
            Scene scene = new Scene(root, 1024, 768);
            Stage stage = new Stage();
            stage.setTitle("权限管理");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "无法加载权限管理界面");
        }
    }

    @FXML
    private void handleUserManagement(ActionEvent event) {
        checkPermissionAndExecute("MANAGE_USERS",
                () -> loadWindow("/view/UserManagement.fxml", "用户管理"));
    }

    @FXML
    private void handleViewLogs(ActionEvent event) {
        checkPermissionAndExecute("VIEW_LOGS",
                () -> loadWindow("/view/ViewLogs.fxml", "查看日志"));
    }

    @FXML
    private void handleAbout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText("社会调查数据分析系统");
        alert.showAndWait();
    }

    @FXML
    private void handleDeleteSurvey(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "提示", "删除问卷功能尚未实现");
    }
}