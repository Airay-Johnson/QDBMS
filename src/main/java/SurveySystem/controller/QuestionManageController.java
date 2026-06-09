package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionService;
import SurveySystem.service.QuestionnaireService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import Util.LoggerUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class QuestionManageController implements Initializable {



    @FXML
    private TableView<Question> questionTable;



    @FXML
    private TableColumn<Question, Long> idColumn;

    @FXML
    private TableColumn<Question, String> contentColumn;

    @FXML
    private TableColumn<Question, String> typeColumn;

    @FXML
    private TableColumn<Question, String> optionsColumn;

    @FXML
    private Label questionnaireTitleLabel;

    @FXML
    private Button backButton;

    @FXML
    private ComboBox<Questionnaire> questionnaireCombo;

    @FXML
    private ProgressIndicator loadingIndicator;

    private Questionnaire currentQuestionnaire;
    private final QuestionService questionService = new QuestionService();
    private final QuestionnaireService questionnaireService = new QuestionnaireService();
    private ObservableList<Question> questionList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化表格列
        if (idColumn != null) {
            idColumn.setCellValueFactory(new PropertyValueFactory<>("qid"));
        }
        if (contentColumn != null) {
            contentColumn.setCellValueFactory(new PropertyValueFactory<>("qtext"));
        }
        if (typeColumn != null) {
            typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        }
        if (optionsColumn != null) {
            optionsColumn.setCellValueFactory(new PropertyValueFactory<>("options"));
        }

        // 初始化问题列表
        questionList = FXCollections.observableArrayList();
        questionTable.setItems(questionList);

        // 加载问卷列表
        loadQuestionnaires();
    }

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.currentQuestionnaire = questionnaire;
        questionnaireTitleLabel.setText(questionnaire.getTitle() + " - 问题管理");
        loadQuestions();
    }

    private void loadQuestions() {
        if (currentQuestionnaire != null) {
            List<Question> questions = questionService.getQuestionsByQuestionnaireId(currentQuestionnaire.getId());
            questionList.setAll(questions);
        }
    }

    private void loadQuestionnaires() {
        List<Questionnaire> questionnaires = questionnaireService.getAllQuestionnaires();
        questionnaireCombo.setItems(FXCollections.observableArrayList(questionnaires));
    }

    @FXML
    private void handleAddQuestion() {
        if (currentQuestionnaire == null) {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
            return;
        }

        final int nextSequence = calculateNextSequence();

        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("添加问题");
        dialog.setHeaderText("请填写问题信息");

        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textField = new TextField();
        textField.setPromptText("问题内容");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("SINGLE_CHOICE", "MULTIPLE_CHOICE", "TEXT");
        typeCombo.setValue("SINGLE_CHOICE");

        // 选项输入区域
        VBox optionsContainer = new VBox(5);
        optionsContainer.setPadding(new Insets(5));

        // 初始添加4个选项输入框
        for (int i = 0; i < 4; i++) {
            HBox optionRow = new HBox(5);
            Label optionLabel = new Label((char) ('A' + i) + ".");
            TextField optionField = new TextField();
            optionField.setPromptText("选项 " + (char) ('A' + i));
            optionField.setId("option_" + i);
            optionRow.getChildren().addAll(optionLabel, optionField);
            optionsContainer.getChildren().add(optionRow);
        }

        // 根据类型显示/隐藏选项区域
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            optionsContainer.setDisable("TEXT".equals(newVal));
            if ("TEXT".equals(newVal)) {
                for (int i = 0; i < 4; i++) {
                    TextField optionField = (TextField) optionsContainer.lookup("#option_" + i);
                    if (optionField != null) {
                        optionField.clear();
                    }
                }
            }
        });

        grid.add(new Label("问题内容:"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(new Label("问题类型:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("选项:"), 0, 2);
        grid.add(optionsContainer, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // 结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                String questionText = textField.getText().trim();
                String questionType = typeCombo.getValue();

                if (questionText.isEmpty()) {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "问题内容不能为空");
                    return null;
                }

                // 构建选项JSON
                JSONArray optionsArray = new JSONArray();
                if (!"TEXT".equals(questionType)) {
                    for (int i = 0; i < 4; i++) {
                        TextField optionField = (TextField) optionsContainer.lookup("#option_" + i);
                        if (optionField != null && !optionField.getText().trim().isEmpty()) {
                            JSONObject optionObj = new JSONObject();
                            optionObj.put("id", String.valueOf((char) ('A' + i)));
                            optionObj.put("text", optionField.getText().trim());
                            optionsArray.put(optionObj);
                        }
                    }

                    if (optionsArray.length() == 0) {
                        MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "选择题必须提供选项");
                        return null;
                    }
                }

                Question question = new Question();
                question.setQtext(questionText);
                question.setType(questionType);
                question.setOptions(optionsArray.toString());
                question.setSequence_Number(nextSequence);
                question.setQuestionnaireId(currentQuestionnaire.getId());

                return question;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(question -> {
            try {
                if (questionService.addQuestion(question)) {
                    MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问题添加成功");
                    loadQuestions();
                    LoggerUtil.log("ADD_QUESTION",
                            "QID=" + question.getQuestionnaireId() + ", QuestionnaireID=" + question.getQuestionnaireId());
                } else {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "添加问题失败");
                }
            } catch (Exception e) {
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "添加问题时发生异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private int calculateNextSequence() {
        if (questionList.isEmpty()) {
            return 1;
        }
        return questionList.stream()
                .mapToInt(Question::getSequence_Number)
                .max()
                .orElse(0) + 1;
    }


    @FXML
    private void handleEditQuestion() {
        Question selectedQuestion = questionTable.getSelectionModel().getSelectedItem();
        if (selectedQuestion == null) {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问题");
            return;
        }

        // 修复1：初始化对话框
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("编辑问题");
        dialog.setHeaderText("修改问题信息");

        // 修复2：定义保存按钮类型
        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // 创建表单
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textField = new TextField(selectedQuestion.getQtext());
        textField.setPromptText("问题内容");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("SINGLE_CHOICE", "MULTIPLE_CHOICE", "TEXT");
        typeCombo.setValue(selectedQuestion.getType());

        // 选项输入区域
        VBox optionsContainer = new VBox(5);
        optionsContainer.setPadding(new Insets(5));

        // 初始化选项输入框（最多4个）
        for (int i = 0; i < 4; i++) {
            HBox optionRow = new HBox(5);
            Label optionLabel = new Label((char) ('A' + i) + ".");
            TextField optionField = new TextField();
            optionField.setPromptText("选项 " + (char) ('A' + i));
            optionField.setId("edit_option_" + i);
            optionRow.getChildren().addAll(optionLabel, optionField);
            optionsContainer.getChildren().add(optionRow);
        }

        // 填充已有选项
        if (!"TEXT".equals(selectedQuestion.getType())) {
            JSONArray existingOptions = new JSONArray(selectedQuestion.getOptions());
            for (int i = 0; i < existingOptions.length() && i < 4; i++) {
                JSONObject option = existingOptions.getJSONObject(i);
                TextField optionField = (TextField) optionsContainer.lookup("#edit_option_" + i);
                if (optionField != null) {
                    optionField.setText(option.getString("text"));
                }
            }
        }

        // 根据类型显示/隐藏选项区域
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            optionsContainer.setDisable("TEXT".equals(newVal));
        });

        grid.add(new Label("问题内容:"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(new Label("问题类型:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("选项:"), 0, 2);
        grid.add(optionsContainer, 1, 2);

        dialog.getDialogPane().setContent(grid);

        // 结果转换器
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                String questionText = textField.getText().trim();
                String questionType = typeCombo.getValue();

                if (questionText.isEmpty()) {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "问题内容不能为空");
                    return null;
                }

                // 构建选项JSON
                JSONArray optionsArray = new JSONArray();
                if (!"TEXT".equals(questionType)) {
                    for (int i = 0; i < 4; i++) {
                        TextField optionField = (TextField) optionsContainer.lookup("#edit_option_" + i);
                        if (optionField != null && !optionField.getText().trim().isEmpty()) {
                            JSONObject optionObj = new JSONObject();
                            optionObj.put("id", String.valueOf((char) ('A' + i)));
                            optionObj.put("text", optionField.getText().trim());
                            optionsArray.put(optionObj);
                        }
                    }
                    if (optionsArray.length() == 0) {
                        MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "选择题必须至少有一个选项");
                        return null;
                    }
                }

                // 更新选中问题的属性
                selectedQuestion.setQtext(questionText);
                selectedQuestion.setType(questionType);
                selectedQuestion.setOptions(optionsArray.toString());

                return selectedQuestion;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(question -> {
            try {
                boolean isUpdated = questionService.updateQuestion(question);
                if (isUpdated) {
                    MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问题更新成功");
                    loadQuestions(); // 强制刷新列表
                    LoggerUtil.log("EDIT_QUESTION", "QID=" + question.getQuestionnaireId());
                } else {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "更新失败，数据库未变更");
                }
            } catch (Exception e) {
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "更新时发生异常: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    @FXML
    private void handleDeleteQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("确认删除");
            confirm.setHeaderText("确定要删除这个问题吗？");
            confirm.setContentText("此操作将永久删除问题及其所有回答。");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                if (questionService.deleteQuestion(selected.getid())) {
                    questionList.remove(selected);
                    MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问题删除成功");
                    LoggerUtil.log("DELETE_QUESTION", "QID=" + selected.getid());
                } else {
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "删除问题失败");
                }
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问题");
        }
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleLoadQuestions() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            this.currentQuestionnaire = selected;
            questionnaireTitleLabel.setText(selected.getTitle() + " - 问题管理");
            loadQuestions();
            MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问题列表已刷新");
            LoggerUtil.log("REFRESH_QUESTIONS",
                    "QuestionnaireID=" + currentQuestionnaire.getId());
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handlePublishQuestionnaire() {
        if (currentQuestionnaire != null) {
            if (questionnaireService.updateQuestionnaireStatus(currentQuestionnaire.getId(), true)) {
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问卷已发布");
                currentQuestionnaire.setStation(true);
            } else {
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "发布问卷失败");
            }
        }
    }

    @FXML
    private void handleUnpublishQuestionnaire() {
        if (currentQuestionnaire != null) {
            if (questionnaireService.updateQuestionnaireStatus(currentQuestionnaire.getId(), false)) {
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "问卷已取消发布");
                currentQuestionnaire.setStation(false);
            } else {
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "取消发布问卷失败");
            }
        }
    }

    @FXML
    private void handleViewResponses() {
        if (currentQuestionnaire != null) {
            MainAPP.showDataCollectionView();
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "没有可用的问卷");
        }
    }
}
