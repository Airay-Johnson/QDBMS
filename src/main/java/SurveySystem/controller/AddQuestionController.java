package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionService;
import SurveySystem.service.QuestionnaireService;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.List;

public class AddQuestionController {

    @FXML
    private ComboBox<Questionnaire> questionnaireCombo;

    @FXML
    private TextArea questionTextArea;

    @FXML
    private ComboBox<String> typeCombo;

    @FXML
    private TextField optionA;

    @FXML
    private TextField optionB;

    @FXML
    private TextField optionC;

    @FXML
    private TextField optionD;

    @FXML
    private CheckBox requiredCheck;

    private QuestionnaireService questionnaireService = new QuestionnaireService();
    private QuestionService questionService = new QuestionService();
    private ObservableList<Questionnaire> questionnaireList;

    @FXML
    public void initialize() {
        List<Questionnaire> list = questionnaireService.getAllQuestionnaires();
        questionnaireList = FXCollections.observableArrayList(list);
        questionnaireCombo.setItems(questionnaireList);

        typeCombo.getItems().addAll("SINGLE_CHOICE", "MULTIPLE_CHOICE", "TEXT");
        typeCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleAdd() {
        Questionnaire selectedQ = questionnaireCombo.getSelectionModel().getSelectedItem();
        String text = questionTextArea.getText();
        String type = typeCombo.getSelectionModel().getSelectedItem();
        boolean required = requiredCheck.isSelected();

        if (selectedQ == null || text == null || text.trim().isEmpty()) {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请填写完整信息");
            return;
        }

        // 构建选项JSON
        String optionsJson = null;
        if (!"TEXT".equals(type)) {
            StringBuilder sb = new StringBuilder("[");
            String[] opts = {optionA.getText(), optionB.getText(), optionC.getText(), optionD.getText()};
            String[] labels = {"A", "B", "C", "D"};
            boolean first = true;
            for (int i = 0; i < opts.length; i++) {
                if (opts[i] != null && !opts[i].trim().isEmpty()) {
                    if (!first) sb.append(",");
                    sb.append("{\"key\":\"").append(labels[i]).append("\",\"value\":\"").append(opts[i].trim()).append("\"}");
                    first = false;
                }
            }
            sb.append("]");
            optionsJson = sb.toString();
            if (optionsJson.equals("[]")) {
                optionsJson = null;
            }
        }

        Question question = new Question();
        question.setQuestionnaireId(selectedQ.getId());
        question.setQtext(text.trim());
        question.setType(type);
        question.setOptions(optionsJson);
        question.setIs_Required(required);

        if (questionService.addQuestion(question)) {
            MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问题添加成功");
            closeWindow();
        } else {
            MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "问题添加失败");
        }
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) questionnaireCombo.getScene().getWindow();
        stage.close();
    }
}
