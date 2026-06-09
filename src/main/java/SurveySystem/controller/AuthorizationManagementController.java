package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.dao.PermissionDao;
import SurveySystem.dao.UserDao;
import SurveySystem.dao.RoleDao;
import SurveySystem.model.Permission;
import SurveySystem.model.Role;
import SurveySystem.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

/**
 * 授权管理控制器（与 PermissionManagementController 功能重复，保留用于 AuthorizationManagement.fxml）
 */
public class AuthorizationManagementController {

    @FXML
    private TableView<User> userTable;

    @FXML
    private TableView<Role> roleTable;

    @FXML
    private TableView<Permission> permissionTable;

    @FXML
    private ListView<String> userRolesList;

    @FXML
    private ListView<String> rolePermissionsList;

    @FXML
    private ComboBox<String> roleComboBox;

    @FXML
    private ComboBox<String> permissionComboBox;

    private UserDao userDao = new UserDao();
    private RoleDao roleDao = new RoleDao();
    private PermissionDao permissionDao = new PermissionDao();

    @FXML
    public void initialize() {
        setupColumns();
        loadData();
    }

    private void setupColumns() {
        TableColumn<User, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        userTable.getColumns().add(usernameCol);

        TableColumn<Role, String> roleCol = new TableColumn<>("角色");
        roleCol.setCellValueFactory(new PropertyValueFactory<>("roleName"));
        roleTable.getColumns().add(roleCol);

        TableColumn<Permission, String> permCol = new TableColumn<>("权限");
        permCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        permissionTable.getColumns().add(permCol);
    }

    private void loadData() {
        userTable.getItems().setAll(userDao.findAll());
        roleTable.getItems().setAll(roleDao.findAll());
        permissionTable.getItems().setAll(permissionDao.findAll());

        List<Role> roles = roleDao.findAll();
        roleComboBox.setItems(FXCollections.observableArrayList(
                roles.stream().map(Role::getRoleName).toList()));
        permissionComboBox.setItems(FXCollections.observableArrayList(
                permissionDao.findAll().stream().map(Permission::getName).toList()));
    }

    @FXML
    private void handleAssignRole() {
        User user = userTable.getSelectionModel().getSelectedItem();
        String roleName = roleComboBox.getSelectionModel().getSelectedItem();
        if (user != null && roleName != null) {
            boolean ok = permissionDao.assignRoleToUser(
                    Math.toIntExact(user.getUid()), roleName);
            if (ok) {
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "角色分配成功");
                loadUserRoles(user);
            }
        }
    }

    @FXML
    private void handleAssignPermission() {
        Role role = roleTable.getSelectionModel().getSelectedItem();
        String permName = permissionComboBox.getSelectionModel().getSelectedItem();
        if (role != null && permName != null) {
            boolean ok = permissionDao.assignPermissionToRole(role.getRoleName(), permName);
            if (ok) {
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "权限分配成功");
                loadRolePermissions(role);
            }
        }
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) userTable.getScene().getWindow();
        stage.close();
    }

    private void loadUserRoles(User user) {
        List<Role> roles = permissionDao.getUserRoles(Math.toIntExact(user.getUid()));
        userRolesList.setItems(FXCollections.observableArrayList(
                roles.stream().map(Role::getRoleName).toList()));
    }

    private void loadRolePermissions(Role role) {
        List<Permission> perms = permissionDao.getPermissionsByRoleId(role.getRoleId());
        rolePermissionsList.setItems(FXCollections.observableArrayList(
                perms.stream().map(Permission::getName).toList()));
    }
}
