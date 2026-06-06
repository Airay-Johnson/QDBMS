package SurveySystem.service;

import SurveySystem.dao.QuestionDao;
import SurveySystem.dao.QuestionnaireDao;
import SurveySystem.dao.ResponseDao;
import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 报告导出服务类（支持Excel、HTML格式及图表导出）
 */
public class ReportExportService {
    private final QuestionnaireDao questionnaireDao = new QuestionnaireDao();
    private final QuestionDao questionDao = new QuestionDao();
    private final AnalysisService analysisService = AnalysisService.getInstance();
    private final ResponseDao responseDao = new ResponseDao();

    // 日期时间格式化器（统一格式）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出问卷分析报告为Excel
     * @param questionnaireId 问卷ID（非空）
     * @param targetFile 目标文件（父目录需存在）
     * @return 导出是否成功
     */
    public boolean exportToExcel(Long questionnaireId, File targetFile) {
        if (questionnaireId == null || questionnaireId <= 0 || targetFile == null) {
            System.err.println("Invalid parameters for exportToExcel");
            return false;
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            Questionnaire questionnaire = questionnaireDao.getQuestionnaireById(Math.toIntExact(questionnaireId));
            if (questionnaire == null) {
                System.err.println("Questionnaire not found: " + questionnaireId);
                return false;
            }

            XSSFSheet infoSheet = workbook.createSheet("问卷信息表");
            fillInfoSheet(infoSheet, questionnaire);

            XSSFSheet dataSheet = workbook.createSheet("问题统计表");
            List<Question> questions = questionDao.getQuestionsByQuestionnaireId(questionnaireId);
            fillDataSheet(dataSheet, questions, questionnaireId);

            adjustSheetColumnWidth(infoSheet);
            adjustSheetColumnWidth(dataSheet);

            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                workbook.write(fos);
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error in exportToExcel: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 生成HTML格式报告（带基础样式）
     */
    public String generateHtmlReport(Long questionnaireId) {
        if (questionnaireId == null || questionnaireId <= 0) {
            return "<html><body><h3>参数错误：无效的问卷ID</h3></body></html>";
        }

        Questionnaire questionnaire = questionnaireDao.getQuestionnaireById(Math.toIntExact(questionnaireId));
        if (questionnaire == null) {
            return "<html><body><h3>问卷不存在或已被删除</h3></body></html>";
        }

        List<Question> questions = questionDao.getQuestionsByQuestionnaireId(questionnaireId);
        StringBuilder questionsHtml = new StringBuilder();

        for (Question question : questions) {
            questionsHtml.append("<div class='question-block'>");
            questionsHtml.append("<h3 class='question-text'>").append(escapeHtml(question.getQtext())).append("</h3>");

            if ("SINGLE_CHOICE".equals(question.getType()) || "MULTIPLE_CHOICE".equals(question.getType())) {
                Map<String, Integer> distribution = analysisService.getAnswerDistribution(question.getid());
                questionsHtml.append("<ul class='option-list'>");
                distribution.forEach((option, count) ->
                        questionsHtml.append("<li>").append(escapeHtml(option)).append(": ")
                                .append(count).append(" 次(")
                                .append(calculatePercentage(count, analysisService.getResponseCount(questionnaireId)))
                                .append(")</li>")
                );
                questionsHtml.append("</ul>");
            } else if ("TEXT".equals(question.getType())) {
                List<String> answers = analysisService.getTextAnswers(question.getid());
                questionsHtml.append("<div class='text-answers'>");
                questionsHtml.append("<p>文本回复: ").append(answers.size()).append(" 条</p>");
                if (!answers.isEmpty()) {
                    questionsHtml.append("<ul>");
                    answers.forEach(answer ->
                            questionsHtml.append("<li>").append(escapeHtml(answer)).append("</li>")
                    );
                    questionsHtml.append("</ul>");
                }
                questionsHtml.append("</div>");
            }
            questionsHtml.append("</div>");
        }

        return String.format("""
            <!DOCTYPE html>
            <html>
                <head>
                    <meta charset="UTF-8">
                    <title>问卷报告 - %s</title>
                    <style>
                        body { font-family: Arial, sans-serif; margin: 20px; line-height: 1.6; }
                        .header { border-bottom: 2px solid #333; padding-bottom: 10px; margin-bottom: 20px; }
                        .question-block { margin: 25px 0; padding: 15px; background-color: #f9f9f9; border-radius: 5px; }
                        .question-text { color: #2c3e50; margin-top: 0; }
                        .option-list { list-style-type: none; padding-left: 0; }
                        .option-list li { margin: 8px 0; padding: 5px; background-color: #fff; border-left: 3px solid #3498db; }
                        .text-answers { padding: 10px; background-color: #fff; border-radius: 4px; }
                        .stats { color: #666; margin: 5px 0; }
                    </style>
                </head>
                <body>
                    <div class="header">
                        <h1>问卷报告 - %s</h1>
                        <p class="stats">创建时间: %s</p>
                        <p class="stats">总回复数: %d</p>
                        <p class="stats">报告生成时间: %s</p>
                    </div>
                    <div class="content">%s</div>
                </body>
            </html>
        """,
                escapeHtml(questionnaire.getTitle()),
                escapeHtml(questionnaire.getTitle()),
                questionnaire.getCreateTime() != null ? questionnaire.getCreateTime().toString() : "未知",
                analysisService.getResponseCount(questionnaireId),
                LocalDateTime.now().format(DATE_FORMATTER),
                questionsHtml.toString()
        );
    }

    /**
     * 导出统计图表（使用JFreeChart示例）
     * 需要添加JFreeChart依赖：org.jfree:jfreechart:1.5.3
     */
    public boolean exportCharts(Long questionnaireId, File targetDir) {
        if (questionnaireId == null || targetDir == null) {
            System.err.println("Invalid parameters for exportCharts");
            return false;
        }

        try {
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                System.err.println("Failed to create chart directory: " + targetDir.getAbsolutePath());
                return false;
            }

            List<Question> questions = questionDao.getQuestionsByQuestionnaireId(questionnaireId).stream()
                    .filter(q -> "SINGLE_CHOICE".equals(q.getType()) || "MULTIPLE_CHOICE".equals(q.getType()))
                    .collect(Collectors.toList());

            for (Question question : questions) {
                Map<String, Integer> distribution = analysisService.getAnswerDistribution(question.getid());
                if (distribution.isEmpty()) continue;

                DefaultPieDataset dataset = new DefaultPieDataset();
                distribution.forEach(dataset::setValue);

                JFreeChart chart = ChartFactory.createPieChart(
                    question.getQtext(),
                    dataset,
                    true,
                    true,
                    false
                );

                File chartFile = new File(targetDir, "question_" + question.getid() + ".png");
                ChartUtils.saveChartAsPNG(chartFile, chart, 600, 400);
            }

            return true;
        } catch (Exception e) {
            System.err.println("Error in exportCharts: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 生成综合报告（包含Excel、HTML和图表）
     */
    public boolean generateComprehensiveReport(Long questionnaireId, File targetDir) {
        if (questionnaireId == null || targetDir == null) {
            return false;
        }

        try {
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                System.err.println("Failed to create target directory: " + targetDir.getAbsolutePath());
                return false;
            }

            File excelFile = new File(targetDir, "问卷统计数据.xlsx");
            boolean excelSuccess = exportToExcel(questionnaireId, excelFile);

            File chartDir = new File(targetDir, "图表");
            boolean chartsSuccess = exportCharts(questionnaireId, chartDir);

            String htmlContent = generateHtmlReport(questionnaireId);
            File htmlFile = new File(targetDir, "问卷报告.html");
            Files.write(Paths.get(htmlFile.getAbsolutePath()), htmlContent.getBytes(StandardCharsets.UTF_8));

            return excelSuccess && chartsSuccess;
        } catch (IOException e) {
            System.err.println("Error in generateComprehensiveReport: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 填充问卷信息表
    private void fillInfoSheet(XSSFSheet sheet, Questionnaire questionnaire) {
        XSSFRow row = sheet.createRow(0);
        row.createCell(0).setCellValue("问卷标题");
        row.createCell(1).setCellValue(questionnaire.getTitle());

        row = sheet.createRow(1);
        row.createCell(0).setCellValue("描述");
        row.createCell(1).setCellValue(questionnaire.getDescription() != null ? questionnaire.getDescription() : "");

        row = sheet.createRow(3);
        row.createCell(0).setCellValue("总回复数");
        row.createCell(1).setCellValue(analysisService.getResponseCount(questionnaire.getId()));

        row = sheet.createRow(4);
        row.createCell(0).setCellValue("完成率");
        row.createCell(1).setCellValue(calculateCompletionRate(questionnaire.getId()));
    }

    // 填充问题统计表
    private void fillDataSheet(XSSFSheet sheet, List<Question> questions, Long questionnaireId) {
        XSSFRow header = sheet.createRow(0);
        header.createCell(0).setCellValue("问题ID");
        header.createCell(1).setCellValue("问题内容");
        header.createCell(2).setCellValue("问题类型");
        header.createCell(3).setCellValue("统计结果");
        header.createCell(4).setCellValue("有效率");

        int rowIdx = 1;
        for (Question question : questions) {
            XSSFRow row = sheet.createRow(rowIdx++);
            row.createCell(0).setCellValue(question.getid());
            row.createCell(1).setCellValue(question.getQtext());
            row.createCell(2).setCellValue(question.getType());

            String stats;
            double validityRate = 0;
            if ("SINGLE_CHOICE".equals(question.getType()) || "MULTIPLE_CHOICE".equals(question.getType())) {
                Map<String, Integer> distribution = analysisService.getAnswerDistribution(question.getid());
                stats = distribution.entrySet().stream()
                        .map(e -> e.getKey() + ": " + e.getValue() + " 次")
                        .collect(Collectors.joining("; "));
                validityRate = calculateValidityRate(distribution.values().stream().mapToInt(Integer::intValue).sum(),
                        analysisService.getResponseCount(questionnaireId));
            } else {
                int textCount = analysisService.getTextAnswers(question.getid()).size();
                stats = "文本回复: " + textCount + " 条";
                validityRate = calculateValidityRate(textCount, analysisService.getResponseCount(questionnaireId));
            }
            row.createCell(3).setCellValue(stats);
            row.createCell(4).setCellValue(validityRate + "%");
        }
    }

    // 辅助方法：自动调整列宽
    private void adjustSheetColumnWidth(XSSFSheet sheet) {
        for (int col = 0; col < 5; col++) {
            sheet.autoSizeColumn(col);
            if (sheet.getColumnWidth(col) > 8000) {
                sheet.setColumnWidth(col, 8000);
            }
        }
    }

    // 辅助方法：计算百分比
    private String calculatePercentage(int count, int total) {
        if (total == 0) return "0%";
        return String.format("%.1f%%", (count * 100.0) / total);
    }

    // 辅助方法：计算完成率
    private String calculateCompletionRate(Long questionnaireId) {
        int totalResponses = responseDao.countByQuestionnaireId(questionnaireId);
        int completedCount = analysisService.getResponseCount(questionnaireId);
        if (totalResponses == 0) return "0%";
        return String.format("%.1f%%", (completedCount * 100.0) / totalResponses);
    }

    // 辅助方法：计算有效率（有效回答数/总回复数）
    private double calculateValidityRate(int validCount, int totalCount) {
        if (totalCount == 0) return 0;
        return Math.round((validCount * 100.0) / totalCount * 10) / 10.0;
    }

    // 辅助方法：HTML转义（防止XSS和格式错乱）
    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
