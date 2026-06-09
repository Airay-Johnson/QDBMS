package SurveySystem.service;

import SurveySystem.dao.AnswerDao;
import SurveySystem.dao.QuestionDao;
import SurveySystem.dao.QuestionnaireDao;
import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import SurveySystem.model.User;
import Util.DBUtil;
import Util.SessionManager;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据分析服务
 */
public class AnalysisService {

    private static final AnalysisService instance = new AnalysisService();

    public static AnalysisService getInstance() { return instance; }

    /** 获取当前用户可分析的问卷列表 */
    public static List<Questionnaire> getAvailableQuestionnaires() {
        User currentUser = SessionManager.getCurrentUser();
        if (currentUser == null) return Collections.emptyList();
        return QuestionnaireDao.getQuestionnairesForAnalysis(
                Math.toIntExact(currentUser.getUid()));
    }

    /** 综合分析入口 */
    public static Map<String, Object> analyzeQuestionnaire(int questionnaireId) {
        Map<String, Object> results = new HashMap<>();
        Questionnaire questionnaire = QuestionnaireDao.getQuestionnaireById(questionnaireId);
        results.put("questionnaire_info", questionnaire);
        int totalResponses = instance.getResponseCount((long) questionnaireId);
        results.put("total_responses", totalResponses);
        results.put("single_choice", analyzeSingleChoiceQuestions(questionnaireId));
        results.put("multiple_choice", analyzeMultipleChoiceQuestions(questionnaireId));
        results.put("text_answers", analyzeTextQuestions(questionnaireId));
        results.put("cross_analysis", analyzeCrossTabulation(questionnaireId));
        results.put("demographic_analysis", analyzeDemographics(questionnaireId));
        return results;
    }

    private static Map<String, Map<String, Integer>> analyzeSingleChoiceQuestions(int questionnaireId) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        List<Question> questions = QuestionDao.getQuestionsByQuestionnaireIdAndType((long) questionnaireId, "SINGLE_CHOICE");
        for (Question q : questions) {
            result.put("Q" + q.getid(), instance.getAnswerDistribution(q.getid()));
        }
        return result;
    }

    private static Map<String, Map<String, Integer>> analyzeMultipleChoiceQuestions(int questionnaireId) {
        Map<String, Map<String, Integer>> result = new HashMap<>();
        List<Question> questions = QuestionDao.getQuestionsByQuestionnaireIdAndType((long) questionnaireId, "MULTIPLE_CHOICE");
        for (Question q : questions) {
            result.put("Q" + q.getid(), instance.getMultipleChoiceDistribution(q.getid()));
        }
        return result;
    }

    private static Map<String, List<String>> analyzeTextQuestions(int questionnaireId) {
        Map<String, List<String>> result = new HashMap<>();
        List<Question> questions = QuestionDao.getQuestionsByQuestionnaireIdAndType((long) questionnaireId, "TEXT");
        for (Question q : questions) {
            result.put("Q" + q.getid(), instance.getTextAnswers(q.getid()));
        }
        return result;
    }

    /** 单选题选项分布 */
    public Map<String, Integer> getAnswerDistribution(Long questionId) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        String sql = "SELECT answer_text, COUNT(*) as cnt " +
                "FROM Answer WHERE question_id = ? GROUP BY answer_text ORDER BY cnt DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String text = rs.getString("answer_text");
                if (text != null && !text.trim().isEmpty())
                    distribution.put(text.trim(), rs.getInt("cnt"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return distribution;
    }

    /** 多选题分布 */
    public Map<String, Integer> getMultipleChoiceDistribution(Long questionId) {
        Map<String, Integer> distribution = new LinkedHashMap<>();
        String sql = "SELECT answer_text FROM Answer WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String ansText = rs.getString("answer_text");
                if (ansText == null) continue;
                if (ansText.startsWith("[")) {
                    try {
                        List<String> choices = new com.fasterxml.jackson.databind.ObjectMapper()
                                .readValue(ansText, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
                        for (String choice : choices) distribution.merge(choice.trim(), 1, Integer::sum);
                    } catch (Exception e) {
                        for (String part : ansText.split("[,\\[\\]\"]+")) {
                            part = part.trim();
                            if (!part.isEmpty()) distribution.merge(part, 1, Integer::sum);
                        }
                    }
                } else {
                    for (String part : ansText.split("[,\\[\\]\"]+")) {
                        part = part.trim();
                        if (!part.isEmpty()) distribution.merge(part, 1, Integer::sum);
                    }
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return distribution;
    }

    /** 文本题答案列表 */
    public List<String> getTextAnswers(Long questionId) {
        List<String> answers = new ArrayList<>();
        String sql = "SELECT answer_text FROM Answer WHERE question_id = ? AND answer_text IS NOT NULL";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String text = rs.getString("answer_text");
                if (text != null && !text.trim().isEmpty()) answers.add(text.trim());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return answers;
    }

    /** 交叉分析 */
    private static Map<String, Map<String, Map<String, Double>>> analyzeCrossTabulation(int questionnaireId) {
        Map<String, Map<String, Map<String, Double>>> result = new LinkedHashMap<>();
        List<Question> singleQuestions = QuestionDao.getQuestionsByQuestionnaireIdAndType((long) questionnaireId, "SINGLE_CHOICE");
        if (singleQuestions.size() < 2) return result;
        for (int i = 0; i < singleQuestions.size() - 1; i++) {
            Question q1 = singleQuestions.get(i);
            for (int j = i + 1; j < singleQuestions.size(); j++) {
                Question q2 = singleQuestions.get(j);
                result.put("Q" + q1.getid() + "_vs_Q" + q2.getid(),
                        instance.crossAnalyzeQuestions(q1.getid(), q2.getid()));
            }
        }
        return result;
    }

    private Map<String, Map<String, Double>> crossAnalyzeQuestions(Long qId1, Long qId2) {
        Map<String, Map<String, Double>> cross = new LinkedHashMap<>();
        Map<String, Integer> dist1 = getAnswerDistribution(qId1);
        Map<String, Integer> dist2 = getAnswerDistribution(qId2);
        for (String opt1 : dist1.keySet()) {
            cross.put(opt1, new LinkedHashMap<>());
            for (String opt2 : dist2.keySet()) cross.get(opt1).put(opt2, 0.0);
        }
        String sql = "SELECT a1.answer_text as ans1, a2.answer_text as ans2, COUNT(*) as cnt " +
                "FROM Answer a1 INNER JOIN Answer a2 ON a1.response_id = a2.response_id " +
                "WHERE a1.question_id = ? AND a2.question_id = ? " +
                "GROUP BY a1.answer_text, a2.answer_text ORDER BY a1.answer_text, a2.answer_text";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, qId1);
            stmt.setLong(2, qId2);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String ans1 = rs.getString("ans1");
                String ans2 = rs.getString("ans2");
                int count = rs.getInt("cnt");
                if (ans1 != null && ans2 != null && cross.containsKey(ans1.trim())
                        && cross.get(ans1.trim()).containsKey(ans2.trim())) {
                    int total = dist1.getOrDefault(ans1.trim(), 1);
                    cross.get(ans1.trim()).put(ans2.trim(),
                            Math.round((double) count / total * 1000.0) / 10.0);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return cross;
    }

    /** 按年龄/性别交叉分析 */
    public Map<String, Map<String, Integer>> getCrossAnalysis(Long questionId, String demographic) {
        Map<String, Map<String, Integer>> result = new LinkedHashMap<>();
        String sql;
        if ("age".equals(demographic)) {
            sql = "SELECT CASE WHEN r.age >= 18 AND r.age < 25 THEN '18-24岁' " +
                    "WHEN r.age >= 25 AND r.age < 35 THEN '25-34岁' " +
                    "WHEN r.age >= 35 AND r.age < 45 THEN '35-44岁' " +
                    "WHEN r.age >= 45 AND r.age < 55 THEN '45-54岁' " +
                    "WHEN r.age >= 55 AND r.age < 65 THEN '55-64岁' " +
                    "WHEN r.age >= 65 THEN '65岁以上' ELSE '未知' END as grp, " +
                    "a.answer_text, COUNT(*) as cnt " +
                    "FROM Answer a INNER JOIN Response resp ON a.response_id = resp.reid " +
                    "INNER JOIN Respondent r ON resp.respondent_id = r.rid " +
                    "WHERE a.question_id = ? GROUP BY grp, a.answer_text ORDER BY grp, cnt DESC";
        } else {
            sql = "SELECT r.sex as grp, a.answer_text, COUNT(*) as cnt " +
                    "FROM Answer a INNER JOIN Response resp ON a.response_id = resp.reid " +
                    "INNER JOIN Respondent r ON resp.respondent_id = r.rid " +
                    "WHERE a.question_id = ? AND r.sex IS NOT NULL " +
                    "GROUP BY r.sex, a.answer_text ORDER BY r.sex, cnt DESC";
        }
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String group = rs.getString("grp");
                String answer = rs.getString("answer_text");
                int count = rs.getInt("cnt");
                if (group != null && answer != null)
                    result.computeIfAbsent(group, k -> new LinkedHashMap<>()).put(answer.trim(), count);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    private static Map<String, Object> analyzeDemographics(int questionnaireId) {
        Map<String, Object> result = new HashMap<>();
        result.put("age_distribution", instance.getAgeGroupStats((long) questionnaireId));
        result.put("sex_distribution", instance.getSexStats((long) questionnaireId));
        return result;
    }

    private Map<String, Integer> getAgeGroupStats(Long questionnaireId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT CASE WHEN r.age >= 18 AND r.age < 25 THEN '18-24岁' " +
                "WHEN r.age >= 25 AND r.age < 35 THEN '25-34岁' " +
                "WHEN r.age >= 35 AND r.age < 45 THEN '35-44岁' " +
                "WHEN r.age >= 45 AND r.age < 55 THEN '45-54岁' " +
                "WHEN r.age >= 55 AND r.age < 65 THEN '55-64岁' " +
                "WHEN r.age >= 65 THEN '65岁以上' ELSE '未知' END as grp, " +
                "COUNT(DISTINCT resp.respondent_id) as cnt " +
                "FROM Response resp INNER JOIN Respondent r ON resp.respondent_id = r.rid " +
                "WHERE resp.questionnaire_id = ? AND r.age IS NOT NULL " +
                "GROUP BY grp ORDER BY MIN(r.age)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.put(rs.getString("grp"), rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    private Map<String, Integer> getSexStats(Long questionnaireId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        String sql = "SELECT r.sex, COUNT(DISTINCT resp.respondent_id) as cnt " +
                "FROM Response resp INNER JOIN Respondent r ON resp.respondent_id = r.rid " +
                "WHERE resp.questionnaire_id = ? AND r.sex IS NOT NULL GROUP BY r.sex ORDER BY cnt DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) result.put(rs.getString("sex"), rs.getInt("cnt"));
        } catch (SQLException e) { e.printStackTrace(); }
        return result;
    }

    public int getResponseCount(Long questionnaireId) {
        String sql = "SELECT COUNT(DISTINCT respondent_id) FROM Response WHERE questionnaire_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getQuestionResponseCount(Long questionId) {
        String sql = "SELECT COUNT(*) FROM Answer WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public int getTotalResponses(Long questionnaireId) { return getResponseCount(questionnaireId); }
}
