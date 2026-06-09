package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.Questionnaire;
import SurveySystem.model.Response;
import SurveySystem.model.Question;
import SurveySystem.service.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * 数据收集界面控制器
 * 展示问卷的收集进度和响应数据
 */
public class DataCollectionController implements Initializable {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // ===== FXML控件 =====
    @FXML
    private ComboBox<Questionnaire> questionnaireCombo;

    @FXML
    private TableView<Response> responseTable;

    @FXML
    private TableColumn<Response, Long> reidColumn;

    @FXML
    private TableColumn<Response, Long> ridColumn;

    @FXML
    private TableColumn<Response, Boolean> completedColumn;

    @FXML
    private TableColumn<Response, String> startTimeColumn;

    @FXML
    private TableColumn<Response, String> endTimeColumn;

    @FXML
    private Label totalResponsesLabel;

    @FXML
    private Label completedResponsesLabel;

    @FXML
    private Label completionRateLabel;

    @FXML
    private Label totalQuestionsLabel;

    @FXML
    private ProgressIndicator completionIndicator;

    // ===== 服务层 =====
    private QuestionnaireService questionnaireService = new QuestionnaireService();
    private ResponseService responseService = new ResponseService();
    private SurveyResponseService surveyResponseService = new SurveyResponseService();
    private AnalysisService analysisService = AnalysisService.getInstance();

    private ObservableList<Questionnaire> questionnaireList;
    private ObservableList<Response> responseList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        questionnaireService = new QuestionnaireService();
        responseService = new ResponseService();
        surveyResponseService = new SurveyResponseService();

        initTableColumns();
        loadQuestionnaires();

        questionnaireCombo.setItems(questionnaireList);
        questionnaireCombo.getSelectionModel().clearSelection();

        questionnaireCombo.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null) {
                        onQuestionnaireSelected(newVal);
                    }
                }
        );

        updateStatsLabels(null);
    }

    private void initTableColumns() {
        reidColumn.setCellValueFactory(new PropertyValueFactory<>("reid"));
        ridColumn.setCellValueFactory(new PropertyValueFactory<>("rid"));
        completedColumn.setCellValueFactory(
                cellData -> new javafx.beans.property.SimpleBooleanProperty(
                        cellData.getValue() != null && cellData.getValue().getEndTime() != null
                )
        );
        startTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getStartTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getStartTime().format(DATE_FORMAT)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        endTimeColumn.setCellValueFactory(cellData -> {
            if (cellData.getValue() != null && cellData.getValue().getEndTime() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                        cellData.getValue().getEndTime().format(DATE_FORMAT)
                );
            }
            return new javafx.beans.property.SimpleStringProperty("进行中");
        });

        // 格式化完成状态列
        completedColumn.setCellFactory(col -> new TableCell<Response, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "✅ 已完成" : "⏳ 进行中");
                    setTextFill(javafx.scene.paint.Color.web(item ? "#27ae60" : "#e67e22"));
                }
            }
        });
    }

    private void loadQuestionnaires() {
        List<Questionnaire> list = questionnaireService.getAllQuestionnaires();
        questionnaireList = FXCollections.observableArrayList(list);
    }

    private void onQuestionnaireSelected(Questionnaire questionnaire) {
        loadResponses(questionnaire.getId());
        updateStats(questionnaire);
    }

    private void loadResponses(Long questionnaireId) {
        List<Response> responses = responseService.getResponsesByQuestionnaire(questionnaireId);
        responseList = FXCollections.observableArrayList(responses);
        responseTable.setItems(responseList);
    }

    private void updateStats(Questionnaire questionnaire) {
        if (questionnaire == null) {
            updateStatsLabels(null);
            return;
        }

        int total = responseService.getResponsesByQuestionnaire(questionnaire.getId()).size();
        int completed = 0;
        for (Response r : responseList) {
            if (r.getEndTime() != null) completed++;
        }

        // 获取问题数量
        List<Question> questions = questionnaireService
                .getQuestionnaireWithQuestions(questionnaire.getId())
                .getQuestions();
        int qCount = questions != null ? questions.size() : 0;

        updateStatsLabels(new StatsData(total, completed, qCount));
    }

    private void updateStatsLabels(StatsData data) {
        if (data == null) {
            totalResponsesLabel.setText("0");
            completedResponsesLabel.setText("0");
            completionRateLabel.setText("0%");
            totalQuestionsLabel.setText("0");
            completionIndicator.setProgress(0);
            return;
        }

        totalResponsesLabel.setText(String.valueOf(data.total));
        completedResponsesLabel.setText(String.valueOf(data.completed));

        double rate = data.total > 0 ? (double) data.completed / data.total * 100 : 0;
        completionRateLabel.setText(String.format("%.1f%%", rate));
        totalQuestionsLabel.setText(String.valueOf(data.questions));
        completionIndicator.setProgress(data.total > 0 ? rate / 100.0 : 0);
    }

    @FXML
    private void handleStartCollection() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            // 打开问卷填写界面
            MainAPP.showSurveyResponseView(selected);
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleViewResponses() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadResponses(selected.getId());
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleExportData() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected == null) {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
            return;
        }

        // 调用报告导出
        MainAPP.showReportExportView();

        // 也可以直接在这里导出
        MainAPP.showAlert(Alert.AlertType.INFORMATION, "提示",
                "正在导出问卷【" + selected.getTitle() + "】的数据，请稍候...");
    }

    @FXML
    private void handleRefresh() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            loadResponses(selected.getId());
            updateStats(selected);
            MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "数据已刷新");
        }
    }

    @FXML
    private void handleBack() {
        MainAPP.showDashboardView();
    }

    private static class StatsData {
        int total, completed, questions;
        StatsData(int total, int completed, int questions) {
            this.total = total;
            this.completed = completed;
            this.questions = questions;
        }
    }
}
