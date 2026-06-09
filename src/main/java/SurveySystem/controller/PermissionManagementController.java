package SurveySystem.controller;

import SurveySystem.dao.PermissionDao;
import SurveySystem.dao.UserDao;
import SurveySystem.model.Permission;
import SurveySystem.model.Role;
import SurveySystem.model.User;
import SurveySystem.service.PermissionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class PermissionManagementController {
    @FXML private TableView<User> userTable;
    @FXML private TableView<Role> roleTable;
    @FXML private TableView<Permission> permissionTable;
    @FXML private ListView<Role> userRolesListView;
    @FXML private ListView<Permission> rolePermissionsListView;

    private PermissionService permissionService = new PermissionService();
    private UserDao userDao = new UserDao();
    private PermissionDao permissionDao = new PermissionDao();

    @FXML
    public void initialize() {
        setupTables();
        loadData();
        setupSelectionListeners();
    }

    private void setupTables() {
        // 设置用户表格
        TableColumn<User, String> userCol = new TableColumn<>("用户名");
        userCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userTable.getColumns().add(userCol);

        // 设置角色表格
        TableColumn<Role, String> roleCol = new TableColumn<>("角色名");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        roleTable.getColumns().add(roleCol);

        // 设置权限表格
        TableColumn<Permission, String> permCol = new TableColumn<>("权限名");
        permCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        permissionTable.getColumns().add(permCol);
    }

    private void loadData() {
        // 加载数据
        userTable.getItems().setAll(userDao.findAll());
        roleTable.getItems().setAll(permissionService.getAllRoles());
        permissionTable.getItems().setAll(permissionService.getAllPermissions());
    }

    private void setupSelectionListeners() {
        // 用户选择监听
        userTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onUserSelected(newVal));

        // 角色选择监听
        roleTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> onRoleSelected(newVal));
    }

    private void onUserSelected(User user) {
        if (user != null) {
            // 加载用户的角色
            List<Role> userRoles = permissionService.getUserRoles(user.getUid());
            userRolesListView.getItems().setAll(userRoles);
        }
    }

    private void onRoleSelected(Role role) {
        if (role != null) {
            // 加载角色的权限
            List<Permission> rolePermissions = permissionService.getRolePermissions(role.getRoleId());
            rolePermissionsListView.getItems().setAll(rolePermissions);
        }
    }

    @FXML
    private void handleAssignRoleToUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        Role selectedRole = roleTable.getSelectionModel().getSelectedItem();

        if (selectedUser != null && selectedRole != null) {
            boolean success = permissionService.assignRoleToUser(
                    selectedUser.getUid(), selectedRole.getRoleName());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "角色分配成功");
                onUserSelected(selectedUser); // 刷新显示
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "角色分配失败");
            }
        }
    }

    @FXML
    private void handleRemoveRoleFromUser() {
        User selectedUser = userTable.getSelectionModel().getSelectedItem();
        Role selectedRole = userRolesListView.getSelectionModel().getSelectedItem();

        if (selectedUser != null && selectedRole != null) {
            boolean success = permissionService.removeRoleFromUser(
                    selectedUser.getUid(), selectedRole.getRoleName());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "角色移除成功");
                onUserSelected(selectedUser); // 刷新显示
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "角色移除失败");
            }
        }
    }

    @FXML
    private void handleAssignPermissionToRole() {
        Role selectedRole = roleTable.getSelectionModel().getSelectedItem();
        Permission selectedPermission = permissionTable.getSelectionModel().getSelectedItem();

        if (selectedRole != null && selectedPermission != null) {
            boolean success = permissionService.assignPermissionToRole(
                    selectedRole.getRoleName(), selectedPermission.getName());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "权限分配成功");
                onRoleSelected(selectedRole); // 刷新显示
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "权限分配失败");
            }
        }
    }

    @FXML
    private void handleRemovePermissionFromRole() {
        Role selectedRole = roleTable.getSelectionModel().getSelectedItem();
        Permission selectedPermission = rolePermissionsListView.getSelectionModel().getSelectedItem();

        if (selectedRole != null && selectedPermission != null) {
            boolean success = permissionService.removePermissionFromRole(
                    selectedRole.getRoleName(), selectedPermission.getName());

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "成功", "权限移除成功");
                onRoleSelected(selectedRole); // 刷新显示
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "权限移除失败");
            }
        }
    }

    // 添加关闭窗口的方法
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) userTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}