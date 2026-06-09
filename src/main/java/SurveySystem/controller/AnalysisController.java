package SurveySystem.controller;

import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.AnalysisService;
import SurveySystem.service.QuestionService;
import SurveySystem.service.QuestionnaireService;
import SurveySystem.service.TextAnalysisService;
import Util.LoggerUtil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AnalysisController {
    @FXML
    private ComboBox<Questionnaire> questionnaireCombo;
    @FXML
    private ComboBox<Question> questionCombo;
    @FXML
    private PieChart distributionChart;
    @FXML
    private BarChart<String, Number> crossChart;
    @FXML
    private ChoiceBox<String> demographicChoice;
    @FXML
    private Label responseCountLabel;

    private QuestionnaireService questionnaireService = new QuestionnaireService();
    private QuestionService questionService = new QuestionService();
    private AnalysisService analysisService = new AnalysisService();

    @FXML
    public void initialize() {
        // 初始化问卷下拉框
        questionnaireCombo.setItems(FXCollections.observableArrayList(
                questionnaireService.getAllQuestionnaires()
        ));

        // 问卷选择变化时更新问题下拉框
        questionnaireCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                questionCombo.setItems(FXCollections.observableArrayList(
                        questionService.getQuestionsByQuestionnaireId(newVal.getId())
                ));
                updateResponseCount(newVal.getId());
            }
        });

        // 问题选择变化时更新图表
        questionCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateCharts(newVal.getQuestionnaireId());

                // 如果是文本类型问题，更新文本分析
                if ("text".equals(newVal.getType())) {
                    updateTextAnalysis(newVal.getQuestionnaireId());
                }
            }
        });

        // 人口统计选择变化时更新交叉分析图表
        demographicChoice.getItems().addAll("age", "sex");
        demographicChoice.setValue("age");
        demographicChoice.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (questionCombo.getValue() != null) {
                updateCrossChart(questionCombo.getValue().getQuestionnaireId(), newVal);
            }
        });
    }

    private void updateResponseCount(Long questionnaireId) {
        int count = analysisService.getResponseCount(questionnaireId);
        responseCountLabel.setText("总回答数: " + count);
    }

    private void updateCharts(Long questionId) {
        updateDistributionChart(questionId);
        updateCrossChart(questionId, demographicChoice.getValue());
    }

    private void updateDistributionChart(Long questionId) {
        Map<String, Integer> distribution = analysisService.getAnswerDistribution(questionId);
        ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

        for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
            pieChartData.add(new PieChart.Data(entry.getKey(), entry.getValue()));
        }

        distributionChart.setData(pieChartData);
        distributionChart.setTitle("答案分布");
    }

    private void updateCrossChart(Long questionId, String demographic) {
        Map<String, Map<String, Integer>> crossData = analysisService.getCrossAnalysis(questionId, demographic);

        // 清空现有数据
        crossChart.getData().clear();

        // 获取所有可能的答案
        Set<String> allAnswers = new HashSet<>();
        for (Map<String, Integer> groupData : crossData.values()) {
            allAnswers.addAll(groupData.keySet());
        }

        // 为每个答案创建一个数据系列
        for (String answer : allAnswers) {
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            series.setName(answer);

            for (String group : crossData.keySet()) {
                int count = crossData.get(group).getOrDefault(answer, 0);
                series.getData().add(new XYChart.Data<>(group, count));
            }

            crossChart.getData().add(series);
        }

        crossChart.setTitle("按" + ("age".equals(demographic) ? "年龄" : "性别") + "交叉分析");
    }

    // AnalysisController.java - 添加文本分析功能
    private void updateTextAnalysis(Long questionId) {
        List<String> answers = analysisService.getTextAnswers(questionId);
        if (answers == null || answers.isEmpty()) {
            return;
        }

        String allText = String.join(" ", answers);

        // 提取关键词
        List<String> keywords = TextAnalysisService.extractKeywords(allText, 10);

        // 词频统计
        Map<String, Integer> wordFreq = TextAnalysisService.wordFrequency(allText);

        // 更新UI显示
        updateKeywordChart(keywords);
        updateWordCloud(wordFreq);
    }

    // 更新关键词图表
    private void updateKeywordChart(List<String> keywords) {
        // 实现关键词图表更新逻辑
        // 这里可以根据需要创建条形图或其他图表显示关键词
        System.out.println("关键词: " + keywords);
    }

    // 更新词云显示
    private void updateWordCloud(Map<String, Integer> wordFreq) {
        // 实现词云更新逻辑
        // 这里可以根据词频数据生成词云
        System.out.println("词频: " + wordFreq);
    }

    @FXML
    private void handleExportData() {
        if (questionCombo.getValue() == null) {
            showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问题");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("导出数据");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV文件", "*.csv")
        );

        File file = fileChooser.showSaveDialog(distributionChart.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(file)) {
                // 写入分布数据
                Map<String, Integer> distribution = analysisService.getAnswerDistribution(
                        questionCombo.getValue().getQuestionnaireId()
                );

                writer.println("答案,数量,百分比");
                int total = distribution.values().stream().mapToInt(Integer::intValue).sum();

                for (Map.Entry<String, Integer> entry : distribution.entrySet()) {
                    double percent = total > 0 ? (entry.getValue() * 100.0 / total) : 0;
                    writer.printf("%s,%d,%.2f%%%n",
                            entry.getKey(), entry.getValue(), percent);
                }

                showAlert(Alert.AlertType.INFORMATION, "成功", "数据已导出到: " + file.getAbsolutePath());
                LoggerUtil.log("EXPORT_DATA", "QID=" + questionCombo.getValue().getQuestionnaireId());
            } catch (IOException e) {
                e.printStackTrace();
                showAlert(Alert.AlertType.ERROR, "错误", "导出数据失败: " + e.getMessage());
            }
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}