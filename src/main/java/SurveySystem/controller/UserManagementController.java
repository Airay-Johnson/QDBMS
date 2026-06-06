package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.dao.UserDao;
import SurveySystem.model.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class UserManagementController {

    @FXML
    private TableView<User> userTable;

    private UserDao userDao = new UserDao();
    private ObservableList<User> userList;

    @FXML
    public void initialize() {
        TableColumn<User, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("uid"));

        TableColumn<User, String> usernameCol = new TableColumn<>("用户名");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));

        TableColumn<User, String> emailCol = new TableColumn<>("邮箱");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

        userTable.getColumns().addAll(idCol, usernameCol, emailCol);
        loadUsers();
    }

    private void loadUsers() {
        List<User> users = userDao.findAll();
        userList = FXCollections.observableArrayList(users);
        userTable.setItems(userList);
    }

    @FXML
    private void handleRefresh() {
        loadUsers();
        MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "用户列表已刷新");
    }

    @FXML
    private void handleDisableUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            MainAPP.showAlert(Alert.AlertType.INFORMATION, "提示", "禁用用户功能：用户 " + selected.getUsername() + " 已标记禁用");
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个用户");
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) userTable.getScene().getWindow();
        stage.close();
    }
}
