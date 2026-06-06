package SurveySystem.controller;


import SurveySystem.MainAPP;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionnaireService;
import Util.LoggerUtil;
import Util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import SurveySystem.model.Questionnaire;
import Util.LoggerUtil;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Optional;

public class QuestionnaireController {
    @FXML
    private TableView<Questionnaire> questionnaireTable;
    @FXML
    private TableColumn<Questionnaire, Long> idColumn;
    @FXML
    private TableColumn<Questionnaire, String> titleColumn;
    @FXML
    private TableColumn<Questionnaire, Timestamp> dateColumn;
    @FXML
    private TableColumn<Questionnaire, Boolean> statusColumn;

    private QuestionnaireService questionnaireService = new QuestionnaireService();
    @FXML private TextField titleField;
    @FXML private TextArea descriptionArea;
    @FXML
    public void initialize() {
        // 绑定表格列与模型属性
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("createTime"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("station"));

        loadQuestionnaires(); // 加载问卷数据
    }



    private void loadQuestionnaires() {
        ObservableList<Questionnaire> questionnaires = FXCollections.observableArrayList(
                questionnaireService.getAllQuestionnaires()
        );
        questionnaireTable.setItems(questionnaires);
    }

    @FXML
    private void handleAddQuestions() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // 加载问题管理界面
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/QuestionManage.fxml"));
                Parent root = loader.load();

                // 获取控制器并设置问卷
                QuestionManageController controller = loader.getController();
                controller.setQuestionnaire(selected);

                // 创建新窗口
                Stage stage = new Stage();
                stage.setTitle("问题管理 - " + selected.getTitle());
                stage.setScene(new Scene(root));
                stage.show();

            } catch (IOException e) {
                e.printStackTrace();
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "无法打开问题管理界面");
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }


    private void openQuestionnaireEditor(Questionnaire questionnaire) {
        try {
            // 加载问卷编辑界面
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditSurvey.fxml"));
            Parent root = loader.load();

            // 传递当前问卷到编辑器控制器
            QuestionnaireEditorController controller = loader.getController();
            controller.initData(questionnaire);

            Stage stage = new Stage();
            stage.setTitle("编辑问卷：" + questionnaire.getTitle());
            stage.setScene(new Scene(root, 800, 600));
            stage.show();

            // 关闭时刷新列表
            stage.setOnHidden(e -> loadQuestionnaires());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "错误", "无法打开问卷编辑器");
        }
    }

    @FXML
    private void handleCreateQuestionnaire() {
        // 打开创建问卷对话框
        Dialog<Questionnaire> dialog = new Dialog<>();
        dialog.setTitle("创建新问卷");

        // 设置对话框按钮
        ButtonType createButtonType = new ButtonType("创建", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("问卷标题");
        TextArea descriptionArea = new TextArea();
        descriptionArea.setPromptText("问卷描述");

        grid.add(new Label("标题:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("描述:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                Questionnaire q = new Questionnaire();
                q.setTitle(titleField.getText());
                q.setDescription(descriptionArea.getText());
                q.setStation(false);
                // 使用当前登录用户的ID
                SurveySystem.model.User currentUser = SessionManager.getCurrentUser();
                q.setUid(currentUser != null ? currentUser.getUid() : 1L);
                return q;
            }
            return null;
        });

        Optional<Questionnaire> result = dialog.showAndWait();
        result.ifPresent(questionnaire -> {
            if (questionnaireService.createQuestionnaire(questionnaire)) {
                loadQuestionnaires(); // 刷新表格
                LoggerUtil.log("CREATE_QUESTIONNAIRE", "ID=" + questionnaire.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "创建问卷失败");
            }
        });
    }

    @FXML
    private void handleEditQuestionnaire() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 打开编辑界面
            openQuestionnaireEditor(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleToggleStatus() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            boolean newStatus = !selected.getStation();
            if (questionnaireService.updateQuestionnaireStatus(selected.getId(), newStatus)) {
                selected.setStation(newStatus);
                questionnaireTable.refresh();
                String action = newStatus ? "PUBLISH" : "UNPUBLISH";
                LoggerUtil.log(action + "_QUESTIONNAIRE", "ID=" + selected.getId());
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "更新状态失败");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleDeleteQuestionnaire() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认删除");
            confirm.setHeaderText("确定要删除这个问卷吗？");
            confirm.setContentText("此操作将永久删除问卷及其所有问题。");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (questionnaireService.deleteQuestionnaire(selected.getId())) {
                    questionnaireTable.getItems().remove(selected);
                    LoggerUtil.log("DELETE_QUESTIONNAIRE", "ID=" + selected.getId());
                } else {
                    showAlert(Alert.AlertType.ERROR, "错误", "删除问卷失败");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }


    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleCancel() {
        clearForm();
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
    }
}