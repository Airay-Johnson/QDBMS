package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.Question;
import SurveySystem.service.QuestionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class QuestionBankManageController {

    @FXML
    private TableView<Question> questionTable;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> typeFilter;

    private QuestionService questionService = new QuestionService();
    private ObservableList<Question> questionList;

    @FXML
    public void initialize() {
        TableColumn<Question, Long> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("qid"));

        TableColumn<Question, String> textCol = new TableColumn<>("问题内容");
        textCol.setCellValueFactory(new PropertyValueFactory<>("qtext"));
        textCol.setPrefWidth(350);

        TableColumn<Question, String> typeCol = new TableColumn<>("类型");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));

        questionTable.getColumns().addAll(idCol, textCol, typeCol);

        typeFilter.getItems().addAll("全部", "SINGLE_CHOICE", "MULTIPLE_CHOICE", "TEXT");
        typeFilter.getSelectionModel().selectFirst();

        loadQuestions();
    }

    private void loadQuestions() {
        // 加载所有问卷的问题（简化版：从第一个问卷获取）
        List<Question> questions = questionService.getQuestionsByQuestionnaireId(1L);
        questionList = FXCollections.observableArrayList(questions);
        questionTable.setItems(questionList);
    }

    @FXML
    private void handleSearch() {
        // 简化搜索：刷新表格
        loadQuestions();
    }

    @FXML
    private void handleAdd() {
        MainAPP.showAlert(Alert.AlertType.INFORMATION, "提示", "请在问题管理界面中添加问题到问卷");
    }

    @FXML
    private void handleAddToQuestionnaire() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            MainAPP.showAlert(Alert.AlertType.INFORMATION, "提示",
                    "问题【" + selected.getQtext() + "】已添加到问卷问题库");
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问题");
        }
    }

    @FXML
    private void handleEdit() {
        MainAPP.showAlert(Alert.AlertType.INFORMATION, "提示", "编辑功能开发中");
    }

    @FXML
    private void handleDelete() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认删除");
            confirm.setContentText("确定要删除此问题吗？");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    if (questionService.deleteQuestion(selected.getid())) {
                        questionList.remove(selected);
                        MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "删除成功");
                    } else {
                        MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "删除失败");
                    }
                }
            });
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问题");
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) questionTable.getScene().getWindow();
        stage.close();
    }
}
