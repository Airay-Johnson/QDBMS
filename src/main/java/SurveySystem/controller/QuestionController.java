package SurveySystem.controller;


import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import Util.LoggerUtil;
import javafx.scene.layout.GridPane;

import java.util.Optional;

public class QuestionController {
    @FXML
    private TableView<Question> questionTable;
    @FXML
    private Label questionnaireTitleLabel;

    private Questionnaire currentQuestionnaire;
    private QuestionService questionService = new QuestionService();

    public void setQuestionnaire(Questionnaire questionnaire) {
        this.currentQuestionnaire = questionnaire;
        questionnaireTitleLabel.setText(questionnaire.getTitle());
        loadQuestions();
    }

    @FXML
    public void initialize() {
        // 设置表格列
        TableColumn<Question, Long> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("qid"));

        TableColumn<Question, String> textColumn = new TableColumn<>("问题内容");
        textColumn.setCellValueFactory(new PropertyValueFactory<>("qtext"));

        TableColumn<Question, String> typeColumn = new TableColumn<>("类型");
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        questionTable.getColumns().setAll(idColumn, textColumn, typeColumn);
    }

    private void loadQuestions() {
        if (currentQuestionnaire != null) {
            ObservableList<Question> questions = FXCollections.observableArrayList(
                    questionService.getQuestionsByQuestionnaireId(currentQuestionnaire.getId())
            );
            questionTable.setItems(questions);
        }
    }

    @FXML
    private void handleAddQuestion() {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("添加问题");

        ButtonType addButtonType = new ButtonType("添加", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textField = new TextField();
        textField.setPromptText("问题内容");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("singleChoice", "multiple_choices", "text");
        typeCombo.setValue("singleChoice");

        TextArea optionsArea = new TextArea();
        optionsArea.setPromptText("选项(每行一个，文本题可留空)");

        grid.add(new Label("问题:"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(new Label("类型:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("选项:"), 0, 2);
        grid.add(optionsArea, 1, 2);

        // 根据类型显示/隐藏选项区域
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            optionsArea.setDisable("text".equals(newVal));
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Question q = new Question();
                q.setQtext(textField.getText());
                q.setType(typeCombo.getValue());

                if (!"text".equals(q.getType())) {
                    String[] options = optionsArea.getText().split("\n");
                    q.setOptions(String.join(",", options)); // 简单用逗号分隔
                }

                q.setQuestionnaireId(currentQuestionnaire.getId());
                // 生成问题ID，这里简单实现，实际应该从数据库获取最大ID+1
                q.setQuestionnaireId(System.currentTimeMillis());

                return q;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(question -> {
            if (questionService.addQuestion(question)) {
                loadQuestions();
                LoggerUtil.log("ADD_QUESTION",
                        "QID=" + question.getQuestionnaireId() + ", QuestionnaireID=" + question.getQuestionnaireId());
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "添加问题失败");
            }
        });
    }

    @FXML
    private void handleEditQuestion() {
        Question selected = questionTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 打开编辑对话框
            openQuestionEditor(selected);
        } else {
            showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问题");
        }
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
                if (questionService.deleteQuestion(selected.getQuestionnaireId())) {
                    questionTable.getItems().remove(selected);
                    LoggerUtil.log("DELETE_QUESTION", "QID=" + selected.getQuestionnaireId());
                } else {
                    showAlert(Alert.AlertType.ERROR, "错误", "删除问题失败");
                }
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问题");
        }
    }

    private void openQuestionEditor(Question question) {
        Dialog<Question> dialog = new Dialog<>();
        dialog.setTitle("编辑问题");

        ButtonType saveButtonType = new ButtonType("保存", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField textField = new TextField(question.getQtext());
        textField.setPromptText("问题内容");

        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("singleChoice", "multiple_choices", "text");
        typeCombo.setValue(question.getType());

        TextArea optionsArea = new TextArea(question.getOptions());
        optionsArea.setPromptText("选项(每行一个，文本题可留空)");
        optionsArea.setDisable("text".equals(question.getType()));

        grid.add(new Label("问题:"), 0, 0);
        grid.add(textField, 1, 0);
        grid.add(new Label("类型:"), 0, 1);
        grid.add(typeCombo, 1, 1);
        grid.add(new Label("选项:"), 0, 2);
        grid.add(optionsArea, 1, 2);

        // 根据类型显示/隐藏选项区域
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            optionsArea.setDisable("text".equals(newVal));
        });

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                question.setQtext(textField.getText());
                question.setType(typeCombo.getValue());

                if (!"text".equals(question.getType())) {
                    String[] options = optionsArea.getText().split("\n");
                    question.setOptions(String.join(",", options));
                } else {
                    question.setOptions("");
                }

                return question;
            }
            return null;
        });

        Optional<Question> result = dialog.showAndWait();
        result.ifPresent(updatedQuestion -> {
            if (questionService.updateQuestion(updatedQuestion)) {
                questionTable.refresh();
                LoggerUtil.log("UPDATE_QUESTION", "QID=" + updatedQuestion.getQuestionnaireId());
            } else {
                showAlert(Alert.AlertType.ERROR, "错误", "更新问题失败");
            }
        });
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}