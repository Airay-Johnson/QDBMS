package com.qdbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdbms.entity.Answer;
import com.qdbms.entity.Question;
import com.qdbms.entity.Questionnaire;
import com.qdbms.entity.Response;
import com.qdbms.mapper.*;
import com.qdbms.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisServiceImpl implements AnalysisService {

    private final QuestionnaireMapper questionnaireMapper;
    private final QuestionMapper questionMapper;
    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;
    private final ObjectMapper objectMapper;

    @Override
    public Map<String, Object> getQuestionnaireStats(Long questionnaireId) {
        Questionnaire q = questionnaireMapper.selectById(questionnaireId);
        if (q == null) {
            throw new IllegalArgumentException("问卷不存在");
        }

        // 总答卷数
        long totalResponses = responseMapper.selectCount(
                new LambdaQueryWrapper<Response>()
                        .eq(Response::getQuestionnaireId, questionnaireId));

        // 题目列表及每题统计
        List<Question> questions = questionMapper.findByQuestionnaireId(questionnaireId);
        List<Map<String, Object>> questionStats = new ArrayList<>();

        for (Question question : questions) {
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("questionId", question.getId());
            stat.put("questionText", question.getQuestionText());
            stat.put("type", question.getType());

            if ("text".equals(question.getType())) {
                stat.put("answers", getTextAnswers(question.getId()));
            } else if ("single".equals(question.getType())) {
                stat.put("distribution", getSingleChoiceStats(question.getId()));
            } else if ("multiple".equals(question.getType())) {
                stat.put("distribution", getMultipleChoiceStats(question.getId()));
            }
            questionStats.add(stat);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("questionnaireId", questionnaireId);
        result.put("title", q.getTitle());
        result.put("totalResponses", totalResponses);
        result.put("totalQuestions", questions.size());
        result.put("questionStats", questionStats);
        return result;
    }

    @Override
    public List<Map<String, Object>> getSingleChoiceStats(Long questionId) {
        List<Answer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<Answer>().eq(Answer::getQuestionId, questionId));

        // 获取题目选项
        Question question = questionMapper.selectById(questionId);
        List<Map<String, String>> options = parseOptions(question.getOptions());

        // 统计每个选项的选择次数
        Map<String, Long> optionCount = new LinkedHashMap<>();
        for (Map<String, String> opt : options) {
            optionCount.put(opt.get("value"), 0L);
        }

        for (Answer answer : answers) {
            String value = answer.getAnswerText();
            if (value != null) {
                // 匹配选项label或value
                for (Map<String, String> opt : options) {
                    if (value.equals(opt.get("label")) || value.equals(opt.get("value"))) {
                        String key = opt.get("value");
                        optionCount.merge(key, 1L, Long::sum);
                        break;
                    }
                }
            }
        }

        long total = answers.size();
        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : optionCount.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("option", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percentage", total > 0 ? Math.round(entry.getValue() * 10000.0 / total) / 100.0 : 0);
            distribution.add(item);
        }
        return distribution;
    }

    @Override
    public List<Map<String, Object>> getMultipleChoiceStats(Long questionId) {
        List<Answer> answers = answerMapper.selectList(
                new LambdaQueryWrapper<Answer>().eq(Answer::getQuestionId, questionId));

        Question question = questionMapper.selectById(questionId);
        List<Map<String, String>> options = parseOptions(question.getOptions());

        // 统计每个选项的选择次数（多选可以选多个）
        Map<String, Long> optionCount = new LinkedHashMap<>();
        for (Map<String, String> opt : options) {
            optionCount.put(opt.get("value"), 0L);
        }

        long respondentCount = 0;
        for (Answer answer : answers) {
            String answerOptions = answer.getAnswerOptions();
            if (answerOptions != null) {
                respondentCount++;
                List<String> selected = parseSelectedOptions(answerOptions);
                for (String sel : selected) {
                    for (Map<String, String> opt : options) {
                        if (sel.equals(opt.get("label")) || sel.equals(opt.get("value"))) {
                            optionCount.merge(opt.get("value"), 1L, Long::sum);
                            break;
                        }
                    }
                }
            }
        }

        List<Map<String, Object>> distribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : optionCount.entrySet()) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("option", entry.getKey());
            item.put("count", entry.getValue());
            item.put("percentage", respondentCount > 0 ? Math.round(entry.getValue() * 10000.0 / respondentCount) / 100.0 : 0);
            distribution.add(item);
        }
        return distribution;
    }

    @Override
    public List<String> getTextAnswers(Long questionId) {
        return answerMapper.selectList(
                new LambdaQueryWrapper<Answer>()
                        .eq(Answer::getQuestionId, questionId)
                        .isNotNull(Answer::getAnswerText))
                .stream()
                .map(Answer::getAnswerText)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getAllAnswers(Long questionnaireId) {
        List<Response> responses = responseMapper.selectList(
                new LambdaQueryWrapper<Response>()
                        .eq(Response::getQuestionnaireId, questionnaireId)
                        .orderByDesc(Response::getSubmittedAt));

        List<Map<String, Object>> result = new ArrayList<>();
        for (Response resp : responses) {
            List<Answer> answers = answerMapper.selectList(
                    new LambdaQueryWrapper<Answer>().eq(Answer::getResponseId, resp.getId()));

            Map<String, Object> row = new LinkedHashMap<>();
            row.put("responseId", resp.getId());
            row.put("submittedAt", resp.getSubmittedAt());
            row.put("ipAddress", resp.getIpAddress());

            for (Answer ans : answers) {
                String key = "q_" + ans.getQuestionId();
                if (ans.getAnswerText() != null) {
                    row.put(key, ans.getAnswerText());
                } else if (ans.getAnswerOptions() != null) {
                    row.put(key, ans.getAnswerOptions());
                }
            }
            result.add(row);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<Map<String, String>>>() {});
        } catch (Exception e) {
            log.warn("解析选项JSON失败: {}", optionsJson, e);
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> parseSelectedOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isBlank()) return List.of();
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            // 可能是JSON对象数组格式
            try {
                List<Map<String, String>> list = objectMapper.readValue(optionsJson, new TypeReference<List<Map<String, String>>>() {});
                return list.stream().map(m -> m.getOrDefault("value", m.getOrDefault("label", ""))).collect(Collectors.toList());
            } catch (Exception e2) {
                log.warn("解析多选答案JSON失败: {}", optionsJson, e2);
                return List.of();
            }
        }
    }
}
