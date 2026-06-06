package com.qdbms.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdbms.entity.Answer;
import com.qdbms.entity.Question;
import com.qdbms.entity.Questionnaire;
import com.qdbms.entity.Response;
import com.qdbms.mapper.AnswerMapper;
import com.qdbms.mapper.QuestionMapper;
import com.qdbms.mapper.QuestionnaireMapper;
import com.qdbms.mapper.ResponseMapper;
import com.qdbms.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExportServiceImpl implements ExportService {

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void exportToExcel(Long questionnaireId, HttpServletResponse response) throws IOException {
        Questionnaire q = questionnaireMapper.selectById(questionnaireId);
        if (q == null) {
            throw new IllegalArgumentException("问卷不存在");
        }

        List<Question> questions = questionMapper.findByQuestionnaireId(questionnaireId);
        List<Response> responses = responseMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Response>()
                        .eq(Response::getQuestionnaireId, questionnaireId)
                        .orderByAsc(Response::getSubmittedAt));

        String filename = URLEncoder.encode(q.getTitle() + "_答卷数据.xlsx", StandardCharsets.UTF_8);
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("答卷数据");

            // 表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // 表头行
            Row headerRow = sheet.createRow(0);
            headerRow.createCell(0).setCellValue("序号");
            headerRow.getCell(0).setCellStyle(headerStyle);
            headerRow.createCell(1).setCellValue("提交时间");
            headerRow.getCell(1).setCellStyle(headerStyle);
            headerRow.createCell(2).setCellValue("IP地址");
            headerRow.getCell(2).setCellStyle(headerStyle);

            int col = 3;
            for (Question question : questions) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(question.getQuestionText());
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(col, 15 * 256);
                col++;
            }

            // 数据行
            int rowNum = 1;
            for (Response resp : responses) {
                Row row = sheet.createRow(rowNum);
                row.createCell(0).setCellValue(rowNum);
                row.createCell(1).setCellValue(resp.getSubmittedAt() != null ? resp.getSubmittedAt().toString() : "");
                row.createCell(2).setCellValue(resp.getIpAddress() != null ? resp.getIpAddress() : "");

                List<Answer> answers = answerMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Answer>()
                                .eq(Answer::getResponseId, resp.getId()));

                int ansCol = 3;
                for (Question question : questions) {
                    String value = "";
                    for (Answer ans : answers) {
                        if (ans.getQuestionId().equals(question.getId())) {
                            if (ans.getAnswerText() != null) {
                                value = ans.getAnswerText();
                            } else if (ans.getAnswerOptions() != null) {
                                value = formatOptions(ans.getAnswerOptions());
                            }
                            break;
                        }
                    }
                    row.createCell(ansCol).setCellValue(value);
                    ansCol++;
                }
                rowNum++;
            }

            workbook.write(response.getOutputStream());
            response.flushBuffer();
        }
    }

    private String formatOptions(String optionsJson) {
        try {
            List<Map<String, String>> list = objectMapper.readValue(optionsJson, new TypeReference<List<Map<String, String>>>() {});
            StringBuilder sb = new StringBuilder();
            for (Map<String, String> m : list) {
                if (!sb.isEmpty()) sb.append(", ");
                sb.append(m.getOrDefault("value", m.getOrDefault("label", "")));
            }
            return sb.toString();
        } catch (Exception e) {
            try {
                List<String> list = objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
                return String.join(", ", list);
            } catch (Exception e2) {
                return optionsJson;
            }
        }
    }
}
