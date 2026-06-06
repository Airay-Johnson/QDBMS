// QuestionnaireManageController.java - 更新以支持完整的问卷管理
package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionnaireService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuestionnaireManageController implements Initializable {

    @FXML
    private TableView<Questionnaire> questionnaireTable;
    @FXML
    private TableColumn<Questionnaire, Long> idColumn;
    @FXML
    private TableColumn<Questionnaire, String> titleColumn;
    @FXML
    private TableColumn<Questionnaire, String> descriptionColumn;
    @FXML
    private TableColumn<Questionnaire, String> statusColumn;
    @FXML
    private TextField searchField;

    private QuestionnaireService questionnaireService;
    private ObservableList<Questionnaire> questionnaireList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        questionnaireService = new QuestionnaireService();
        loadQuestionnaires();

        // 设置列与对象属性的关联
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));

        // 设置表格数据
        questionnaireTable.setItems(questionnaireList);
    }

    private void loadQuestionnaires() {
        List<Questionnaire> questionnaires = questionnaireService.getAllQuestionnaires();
        questionnaireList = FXCollections.observableArrayList(questionnaires);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) {
            loadQuestionnaires();
        } else {
            // 实现搜索功能
            List<Questionnaire> filtered = questionnaireList.filtered(q ->
                    q.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                            q.getDescription().toLowerCase().contains(keyword.toLowerCase())
            );
            questionnaireTable.setItems(FXCollections.observableArrayList(filtered));
        }
    }

    @FXML
    private void handleCreateQuestionnaire() {
        // 创建对话框
        Dialog<Questionnaire> dialog = new Dialog<>();
        dialog.setTitle("新建问卷");
        dialog.setHeaderText("请输入问卷信息");

        // 设置按钮
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
        descriptionArea.setPrefRowCount(3);

        grid.add(new Label("标题:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("描述:"), 0, 1);
        grid.add(descriptionArea, 1, 1);

        dialog.getDialogPane().setContent(grid);

        // 设置结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String title = titleField.getText().trim();
                String description = descriptionArea.getText().trim();

                // 验证输入
                if (title.isEmpty()) {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "问卷标题不能为空");
                    return null;
                }

                Questionnaire questionnaire = new Questionnaire();
                questionnaire.setTitle(title);
                questionnaire.setDescription(description);
                questionnaire.setStation(false); // 默认未发布

                // 设置用户ID（这里需要根据实际登录用户设置）
                questionnaire.setUid(1L); // 临时使用固定值

                return questionnaire;
            }
            return null;
        });

        // 显示对话框并处理结果
        Optional<Questionnaire> result = dialog.showAndWait();
        result.ifPresent(questionnaire -> {
            // 保存问卷到数据库
            if (questionnaireService.createQuestionnaire(questionnaire)) {
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问卷创建成功");
                loadQuestionnaires(); // 刷新表格
            } else {
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "创建问卷失败");
            }
        });
    }

    @FXML
    private void handleEditQuestionnaire() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 创建编辑对话框
            Dialog<Questionnaire> dialog = new Dialog<>();
            dialog.setTitle("编辑问卷");
            dialog.setHeaderText("编辑问卷信息");

            // 设置按钮类型
            ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

            // 创建表单
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField titleField = new TextField(selected.getTitle());
            titleField.setPromptText("问卷标题");
            TextArea descriptionArea = new TextArea(selected.getDescription());
            descriptionArea.setPromptText("问卷描述");

            grid.add(new Label("标题:"), 0, 0);
            grid.add(titleField, 1, 0);
            grid.add(new Label("描述:"), 0, 1);
            grid.add(descriptionArea, 1, 1);

            dialog.getDialogPane().setContent(grid);

            // 设置结果转换器
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == saveButtonType) {
                    String title = titleField.getText().trim();
                    String description = descriptionArea.getText().trim();

                    // 验证输入
                    if (title.isEmpty()) {
                        MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "问卷标题不能为空");
                        return null;
                    }

                    selected.setTitle(title);
                    selected.setDescription(description);
                    return selected;
                }
                return null;
            });

            Optional<Questionnaire> result = dialog.showAndWait();
            result.ifPresent(questionnaire -> {
                if (questionnaireService.updateQuestionnaire(questionnaire)) {
                    MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问卷更新成功");
                    loadQuestionnaires(); // 刷新表格
                } else {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "更新问卷失败");
                }
            });
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
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

    @FXML
    private void handleDeleteQuestionnaire() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认删除");
            confirm.setHeaderText("删除问卷");
            confirm.setContentText("确定要删除问卷 '" + selected.getTitle() + "' 吗？");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (questionnaireService.deleteQuestionnaire(selected.getId())) {
                    MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问卷已删除");
                    loadQuestionnaires();
                } else {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "删除问卷失败");
                }
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handlePublishQuestionnaire() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (questionnaireService.updateQuestionnaireStatus(selected.getId(), true)) {
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问卷已发布");
                loadQuestionnaires();
            } else {
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "发布问卷失败");
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleUnpublishQuestionnaire() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (questionnaireService.updateQuestionnaireStatus(selected.getId(), false)) {
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问卷已取消发布");
                loadQuestionnaires();
            } else {
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "取消发布问卷失败");
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleViewQuestions() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 打开问题管理界面
            MainAPP.showQuestionManageView(selected);
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleViewResults() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 打开查看结果界面
            MainAPP.showDataAnalysisView();
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleBack() {
        MainAPP.showDashboardView();
    }
}