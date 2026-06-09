package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionnaireService;
import SurveySystem.service.QuestionnaireService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;

/**
 * 问卷选择界面控制器
 * 负责展示已发布的问卷列表，供受访者选择并开始填写
 */
public class SurveySelectionController {

    @FXML
    private TableView<Questionnaire> questionnaireTable;

    @FXML
    private TableColumn<Questionnaire, String> titleColumn;

    @FXML
    private TableColumn<Questionnaire, String> statusColumn;

    @FXML
    private TableColumn<Questionnaire, String> descColumn;

    private QuestionnaireService questionnaireService;
    private ObservableList<Questionnaire> data;

    public void initialize() {
        questionnaireService = new QuestionnaireService();

        // 设置列与属性的绑定
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("statusDisplay"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));

        // 加载已发布的问卷
        loadPublishedQuestionnaires();
    }

    private void loadPublishedQuestionnaires() {
        List<Questionnaire> list = questionnaireService.getPublishedQuestionnaires();
        data = FXCollections.observableArrayList(list);
        questionnaireTable.setItems(data);
    }

    @FXML
    private void handleStartSurvey() {
        Questionnaire selected = questionnaireTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 将选中的问卷传递到填写界面
            MainAPP.showSurveyResponseView(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "提示", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) questionnaireTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
