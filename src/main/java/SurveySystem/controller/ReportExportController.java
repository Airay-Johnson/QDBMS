package SurveySystem.controller;

import SurveySystem.service.AnalysisService;
import SurveySystem.service.QuestionService;
import SurveySystem.MainAPP;
import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionnaireService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ReportExportController implements Initializable {

    @FXML
    private ComboBox<Questionnaire> questionnaireCombo;

    // 实例化服务（非静态调用）
    private final QuestionnaireService questionnaireService = new QuestionnaireService();
    private final QuestionService questionService = new QuestionService();
    private final AnalysisService analysisService = new AnalysisService();
    private ObservableList<Questionnaire> questionnaireList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 加载问卷列表
        loadQuestionnaires();
        questionnaireCombo.setItems(questionnaireList);
    }

    private void loadQuestionnaires() {
        List<Questionnaire> questionnaires = questionnaireService.getAllQuestionnaires();
        questionnaireList = FXCollections.observableArrayList(questionnaires);
    }

    @FXML
    private void handleLoadData() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // 修复：用实例调用非静态方法
                List<Question> questions = questionService.getQuestionsByQuestionnaireId(selected.getId());
                int totalResponses = analysisService.getTotalResponses(selected.getId());

                // 显示加载结果
                MainAPP.showAlert(Alert.AlertType.INFORMATION, "加载成功",
                        String.format("问卷《%s》加载完成，共%d个问题，收到%d份回复",
                                selected.getTitle(), questions.size(), totalResponses));
            } catch (Exception e) {
                e.printStackTrace();
                MainAPP.showAlert(Alert.AlertType.ERROR, "加载失败", "获取问卷数据时出错");
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handlePreview() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                // 生成HTML预览内容
                String reportHtml = generateReportHtml(selected);

                // 修复：导入WebView并使用容器包装
                WebView webView = new WebView();
                WebEngine engine = webView.getEngine();
                engine.loadContent(reportHtml);

                // 修复：将WebView放入VBox（Parent子类）
                VBox container = new VBox(webView);
                Stage stage = new Stage();
                stage.setTitle("报告预览：" + selected.getTitle());
                stage.setScene(new Scene(container, 1000, 800)); // 正确传递Parent类型
                stage.show();
            } catch (Exception e) {
                e.printStackTrace();
                MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "生成预览失败");
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    @FXML
    private void handleExport() {
        Questionnaire selected = questionnaireCombo.getSelectionModel().getSelectedItem();
        if (selected != null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("导出报告");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel", "*.xlsx"));
            File file = fileChooser.showSaveDialog(null);

            if (file != null) {
                try {
                    // 生成Excel
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    XSSFSheet sheet = workbook.createSheet("问卷报告");

                    // 写入标题行
                    XSSFRow row = sheet.createRow(0);
                    row.createCell(0).setCellValue("问卷标题");
                    row.createCell(1).setCellValue(selected.getTitle());

                    // 写入问题与统计（简化版）
                    // 修复：用实例调用非静态方法，且Question用getQtext()获取内容
                    List<Question> questions = questionService.getQuestionsByQuestionnaireId(selected.getId());
                    int rowIdx = 2;
                    for (Question question : questions) {
                        row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue("问题");
                        row.createCell(1).setCellValue(question.getQtext()); // 修复：使用正确的getter方法
                    }

                    // 保存文件
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        workbook.write(fos);
                    }
                    MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "报告已导出至：" + file.getPath());
                } catch (Exception e) {
                    e.printStackTrace();
                    MainAPP.showAlert(Alert.AlertType.ERROR, "错误", "导出失败");
                }
            }
        } else {
            MainAPP.showAlert(Alert.AlertType.WARNING, "警告", "请先选择一个问卷");
        }
    }

    // 生成HTML报告（简化版）
    private String generateReportHtml(Questionnaire q) {
        return String.format("""
        <html>
            <body>
                <h1>问卷报告：%s</h1>
                <p>创建时间：%s</p>
                <p>总回复数：%d</p>
                <!-- 实际项目中补充问题统计图表 -->
            </body>
        </html>
        """, q.getTitle(), q.getCreateTime() != null ? q.getCreateTime().toString() : "未知",
                analysisService.getTotalResponses(q.getId()));
    }

    @FXML
    private void handleBack() {
        MainAPP.showDashboardView();
    }
}
