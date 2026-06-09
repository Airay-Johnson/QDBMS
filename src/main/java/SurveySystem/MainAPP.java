package SurveySystem;

import SurveySystem.constant.PermissionType;
import SurveySystem.controller.QuestionManageController;
import SurveySystem.controller.SurveyResponseController;
import SurveySystem.dao.PermissionDao;
import SurveySystem.model.Questionnaire;
import SurveySystem.model.User;
import SurveySystem.service.AuthService;
import SurveySystem.service.PermissionService;
import Util.DBUtil;
import Util.SessionManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainAPP extends Application {
    private static final Logger logger = Logger.getLogger(MainAPP.class.getName());
    private static Stage primaryStage;
    private static AuthService authService;


    public static void showPermissionManageView() {
        try {
            Parent root = FXMLLoader.load(MainAPP.class.getResource("/view/Permission-management.fxml"));
            Scene scene = new Scene(root, 1024, 768);
            Stage stage = new Stage();
            stage.setTitle("权限管理");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载权限管理界面: " + e.getMessage());
        }
    }

    public static void showQuestionnaireManageView() {
        try {
            Parent root = FXMLLoader.load(MainAPP.class.getResource("/view/QuestionnaireManage.fxml"));
            Scene scene = new Scene(root, 1024, 768);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载问卷管理界面: " + e.getMessage());
        }
    }
    @Override
    public void start(Stage stage) {
        try {
            primaryStage = stage;
            authService = AuthService.getInstance();

            // 测试数据库连接
            if (testDatabaseConnection()) {
                // 初始化权限数据
                initializePermissions();

                // 尝试快速登录
                boolean quickLoginSuccess;
                try {
                    // 使用 autoLogin() 而不是 quickLogin()
                    quickLoginSuccess = authService.autoLogin();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "快速登录失败，将跳转到登录页", e);
                    showAlert(Alert.AlertType.WARNING, "登录异常", "快速登录失败，将跳转到登录页：" + e.getMessage());
                    quickLoginSuccess = false;
                }

                if (quickLoginSuccess) {
                    showDashboardView();
                } else {
                    showLoginView();
                }
                primaryStage.setTitle("社会调查数据分析系统");
                primaryStage.show();
            } else {
                showAlert(Alert.AlertType.ERROR, "数据库连接错误",
                        "无法连接到数据库。请检查数据库配置和运行状态。");
                System.exit(1);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "应用程序启动失败", e);
            showAlert(Alert.AlertType.ERROR, "启动错误", "应用程序启动失败: " + e.getMessage());
            System.exit(1);
        }
    }

    public static void showAuthorizationManagementView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/AuthorizationManagement.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到授权管理界面资源: /view/AuthorizationManagement.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = new Stage();
            stage.setTitle("用户权限管理");
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法加载授权管理界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载授权管理界面: " + e.getMessage());
        }
    }

    // 初始化权限数据
    private void initializePermissions() {
        PermissionDao permissionDao = new PermissionDao();

        // 1. 插入所有权限（PermissionType枚举值）
        for (PermissionType permission : PermissionType.values()) {
            if (permissionDao.findByName(permission.name()) == null) {
                try {
                    SurveySystem.model.Permission perm = new SurveySystem.model.Permission();
                    perm.setName(permission.name());
                    perm.setDescription(permission.getDescription());
                    Long pid = permissionDao.save(perm);
                    logger.info("权限已添加: " + permission.name() + " (PID=" + pid + ")");
                } catch (Exception e) {
                    logger.log(Level.WARNING, "添加权限失败: " + permission.name(), e);
                }
            }
        }

        // 2. 确保ADMIN角色拥有全部权限
        try {
            SurveySystem.model.Role adminRole = new SurveySystem.dao.RoleDao().findByName("ADMIN");
            if (adminRole != null) {
                for (PermissionType permission : PermissionType.values()) {
                    SurveySystem.model.Permission perm = permissionDao.findByName(permission.name());
                    if (perm != null) {
                        // 尝试分配，已存在则忽略
                        try {
                            new SurveySystem.dao.RolePermissionDao().assignPermissionToRole(
                                    adminRole.getRoleId(), perm.getPid());
                        } catch (Exception ignored) {
                            // ON CONFLICT 忽略
                        }
                    }
                }
                logger.info("ADMIN角色权限已分配完毕");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "分配ADMIN权限失败", e);
        }

        // 3. 确保USER角色拥有基础权限
        try {
            SurveySystem.dao.RoleDao roleDao = new SurveySystem.dao.RoleDao();
            SurveySystem.model.Role userRole = roleDao.findByName("USER");
            if (userRole != null) {
                SurveySystem.dao.RolePermissionDao rpDao = new SurveySystem.dao.RolePermissionDao();
                // USER可用的权限
                PermissionType[] userPerms = {
                    PermissionType.SURVEY_CREATE,
                    PermissionType.SURVEY_EDIT,
                    PermissionType.SURVEY_PUBLISH,
                    PermissionType.QUESTION_MANAGE,
                    PermissionType.RESPONSE_VIEW,
                    PermissionType.DATA_ANALYZE,
                    PermissionType.DATA_EXPORT
                };
                for (PermissionType pt : userPerms) {
                    SurveySystem.model.Permission perm = permissionDao.findByName(pt.name());
                    if (perm != null) {
                        try {
                            rpDao.assignPermissionToRole(userRole.getRoleId(), perm.getPid());
                        } catch (Exception ignored) {}
                    }
                }
                logger.info("USER角色权限已分配完毕");
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "分配USER权限失败", e);
        }
    }

    /**
     * 显示登录界面
     */
    public static void showLoginView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/login.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到登录界面资源: /view/login.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "无法加载登录界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载登录界面: " + e.getMessage());
        }
    }

    /**
     * 显示注册界面
     */
    public static void showRegisterView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/register.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到注册界面资源: /view/register.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 800, 600);
            primaryStage.setScene(scene);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "无法加载注册界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载注册界面: " + e.getMessage());
        }
    }

    /**
     * 显示主仪表板界面
     */
    public static void showDashboardView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/dashboard.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到主界面资源: /view/dashboard.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 1024, 768);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "无法加载主界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载主界面: " + e.getMessage());
        }
    }

    /**
     * 显示问题管理界面
     */
    public static void showQuestionManageView(Questionnaire questionnaire) {
        if (questionnaire == null) {
            showAlert(Alert.AlertType.ERROR, "参数错误", "问卷数据不能为空");
            return;
        }

        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/QuestionManage.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到问题管理界面资源: /view/QuestionManage.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            QuestionManageController controller = loader.getController();
            if (controller == null) {
                throw new RuntimeException("问题管理界面控制器未绑定");
            }
            controller.setQuestionnaire(questionnaire);

            Scene scene = new Scene(root, 800, 600);
            Stage stage = new Stage();
            stage.setTitle("问题管理 - " + questionnaire.getTitle());
            stage.initOwner(primaryStage);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "无法加载问题管理界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载问题管理界面: " + e.getMessage());
        }
    }

    /**
     * 显示数据分析界面
     */
    public static void showDataAnalysisView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/DataAnalysis.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到数据分析界面资源: /view/DataAnalysis.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 1024, 768);
            Stage stage = new Stage();
            stage.setTitle("数据分析");
            stage.initOwner(primaryStage);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "无法加载数据分析界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载数据分析界面: " + e.getMessage());
        }
    }

    /**
     * 显示数据收集界面
     */
    public static void showDataCollectionView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/DataCollection.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到数据收集界面资源: /view/DataCollection.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Scene scene = new Scene(root, 1024, 768);
            Stage stage = new Stage();
            stage.setTitle("数据收集");
            stage.initOwner(primaryStage);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "无法加载数据收集界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载数据收集界面: " + e.getMessage());
        }
    }

    /**
     * 显示权限管理界面
     */
    public static void showPermissionManagementView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/Permission-management.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到权限管理界面资源: /view/Permission-management.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = new Stage();
            stage.setTitle("权限管理");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法加载权限管理界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载权限管理界面: " + e.getMessage());
        }
    }

    /**
     * 显示问卷填写界面（无参数版本，供其他模块调用）
     */
    public static void showSurveyResponseView() {
        showSurveyResponseView(null);
    }

    /**
     * 显示问卷填写界面（带问卷参数）
     * @param questionnaire 要填写的问卷，null则从选择界面选择
     */
    public static void showSurveyResponseView(Questionnaire questionnaire) {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/SurveyResponse.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到问卷填写界面资源: /view/SurveyResponse.fxml");
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // 将问卷数据传递给控制器
            SurveyResponseController controller = loader.getController();
            controller.setQuestionnaire(questionnaire);

            Stage stage = new Stage();
            stage.setTitle("填写问卷" + (questionnaire != null ? " - " + questionnaire.getTitle() : ""));
            stage.setScene(new Scene(root, 800, 600));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法加载问卷填写界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载问卷填写界面: " + e.getMessage());
        }
    }

    /**
     * 显示报告导出界面
     */
    public static void showReportExportView() {
        try {
            URL fxmlUrl = MainAPP.class.getResource("/view/ReportExport.fxml");
            if (fxmlUrl == null) {
                throw new IOException("未找到报告导出界面资源: /view/ReportExport.fxml");
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            Stage stage = new Stage();
            stage.setTitle("报告导出");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法加载报告导出界面", e);
            showAlert(Alert.AlertType.ERROR, "界面加载错误", "无法加载报告导出界面: " + e.getMessage());
        }
    }

    /**
     * 显示警告对话框
     */
    public static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);

        if (primaryStage != null && primaryStage.isShowing()) {
            alert.initOwner(primaryStage);
        }

        alert.showAndWait();
    }

    /**
     * 获取主舞台实例
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void stop() {
        try {
            if (authService != null) {
                authService.logout();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "退出时清理资源失败", e);
        }
    }

    public static void switchContent(String fxmlPath) {
        try {
            URL fxmlUrl = MainAPP.class.getResource(fxmlPath);
            if (fxmlUrl == null) {
                throw new IOException("未找到界面资源: " + fxmlPath);
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            primaryStage.getScene().setRoot(root);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "无法加载界面: " + fxmlPath, e);
            showAlert(Alert.AlertType.ERROR, "错误", "无法加载界面: " + fxmlPath + "，原因：" + e.getMessage());
        }
    }

    /**
     * 测试数据库连接
     */
    private boolean testDatabaseConnection() {
        try {
            return DBUtil.testConnection();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "测试数据库连接失败", e);
            showAlert(Alert.AlertType.ERROR, "数据库测试异常", "测试连接时发生错误: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}