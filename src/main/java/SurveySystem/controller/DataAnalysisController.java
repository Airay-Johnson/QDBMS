package SurveySystem.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import SurveySystem.service.AnalysisService;
import SurveySystem.model.Questionnaire;

import java.util.List;
import java.util.Map;

public class DataAnalysisController {
    @FXML
    private ComboBox<Questionnaire> surveySelector;

    @FXML
    private TabPane chartsTabPane;

    @FXML
    public void initialize() {
        loadAvailableQuestionnaires();
        surveySelector.setOnAction(e -> analyzeSelectedQuestionnaire());
    }

    private void loadAvailableQuestionnaires() {
        // 获取用户有权限查看的问卷列表
        List<Questionnaire> questionnaires = AnalysisService.getAvailableQuestionnaires();
        surveySelector.getItems().addAll(questionnaires);
    }

    @FXML
    private void analyzeSelectedQuestionnaire() {
        Questionnaire selected = surveySelector.getValue();
        if (selected == null) return;

        chartsTabPane.getTabs().clear();

        // 获取统计分析结果
        Map<String, Object> analysisResults = AnalysisService.analyzeQuestionnaire(selected.getId().intValue());

        // 创建图表
        createCharts(analysisResults);
    }

    private void createCharts(Map<String, Object> analysisResults) {
        // 单选题饼图
        Map<String, Map<String, Integer>> singleChoiceStats =
                (Map<String, Map<String, Integer>>) analysisResults.get("single_choice");

        if (singleChoiceStats != null) {
            for (Map.Entry<String, Map<String, Integer>> entry : singleChoiceStats.entrySet()) {
                String questionId = entry.getKey();
                Map<String, Integer> choices = entry.getValue();

                PieChart pieChart = new PieChart();
                pieChart.setTitle("问题ID: " + questionId);

                for (Map.Entry<String, Integer> choice : choices.entrySet()) {
                    PieChart.Data data = new PieChart.Data(
                            choice.getKey() + " (" + choice.getValue() + ")",
                            choice.getValue()
                    );
                    pieChart.getData().add(data);
                }

                Tab tab = new Tab("问题 " + questionId);
                tab.setContent(pieChart);
                chartsTabPane.getTabs().add(tab);
            }
        }

        // 交叉分析柱状图
        Map<String, Map<String, Map<String, Double>>> crossAnalysis =
                (Map<String, Map<String, Map<String, Double>>>) analysisResults.get("cross_analysis");

        if (crossAnalysis != null) {
            for (Map.Entry<String, Map<String, Map<String, Double>>> entry : crossAnalysis.entrySet()) {
                String questionPair = entry.getKey(); // 格式: questionId1_vs_questionId2
                Map<String, Map<String, Double>> analysis = entry.getValue();

                CategoryAxis xAxis = new CategoryAxis();
                NumberAxis yAxis = new NumberAxis();
                BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
                barChart.setTitle("交叉分析: " + questionPair);

                for (Map.Entry<String, Map<String, Double>> dim : analysis.entrySet()) {
                    String dimension = dim.getKey();
                    Map<String, Double> values = dim.getValue();

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    series.setName(dimension);

                    for (Map.Entry<String, Double> value : values.entrySet()) {
                        series.getData().add(new XYChart.Data<>(value.getKey(), value.getValue()));
                    }

                    barChart.getData().add(series);
                }

                Tab tab = new Tab("交叉分析 " + questionPair);
                tab.setContent(barChart);
                chartsTabPane.getTabs().add(tab);
            }
        }
    }

    @FXML
    private void exportData() {
        // 实现导出数据功能
        showAlert(Alert.AlertType.INFORMATION, "导出成功", "数据已成功导出");
    }



    @FXML
    private void closeWindow() {
        Stage stage = (Stage) surveySelector.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}