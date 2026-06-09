package SurveySystem.service;

import SurveySystem.model.Response;
import Util.DBUtil;
import Util.LoggerUtil;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResponseService {

    /** 创建问卷响应记录（返回新响应ID） */
    public Long createResponse(Long questionnaireId, Long respondentId) {
        String sql = "INSERT INTO Response (questionnaire_id, respondent_id, start_time, is_completed) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, questionnaireId);
            stmt.setLong(2, respondentId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(4, false);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            LoggerUtil.log("DATABASE_ERROR", "创建响应失败: " + e.getMessage());
        }
        return null;
    }

    /** 保存问题答案 */
    public boolean saveAnswer(Long responseId, Long questionId, String answerText) {
        String sql = "INSERT INTO Answer (response_id, question_id, answer_text) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, responseId);
            stmt.setLong(2, questionId);
            stmt.setString(3, answerText);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.log("DATABASE_ERROR", "保存答案失败: " + e.getMessage());
            return false;
        }
    }

    /** 按问卷ID获取响应列表 */
    public List<Response> getResponsesByQuestionnaire(Long questionnaireId) {
        List<Response> responses = new ArrayList<>();
        String sql = "SELECT * FROM Response WHERE questionnaire_id = ? ORDER BY start_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Response r = new Response();
                r.setReid(rs.getLong("reid"));
                r.setRid(rs.getLong("respondent_id"));
                r.setQuestionnaireId(rs.getLong("questionnaire_id"));
                Timestamp st = rs.getTimestamp("start_time");
                if (st != null) r.setStartTime(st.toLocalDateTime());
                Timestamp et = rs.getTimestamp("end_time");
                if (et != null) r.setEndTime(et.toLocalDateTime());
                r.setCompleted(rs.getBoolean("is_completed"));
                responses.add(r);
            }
        } catch (SQLException e) {
            LoggerUtil.log("DATABASE_ERROR", "获取响应列表失败: " + e.getMessage());
        }
        return responses;
    }

    /** 标记响应为已完成 */
    public boolean completeResponse(Long responseId) {
        String sql = "UPDATE Response SET end_time = ?, is_completed = ? WHERE reid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(2, true);
            stmt.setLong(3, responseId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LoggerUtil.log("DATABASE_ERROR", "完成响应失败: " + e.getMessage());
            return false;
        }
    }

    /** 获取某响应的所有答案 */
    public Map<Long, String> getAnswersByResponse(Long responseId) {
        Map<Long, String> answers = new HashMap<>();
        String sql = "SELECT question_id, answer_text FROM Answer WHERE response_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, responseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                answers.put(rs.getLong("question_id"), rs.getString("answer_text"));
            }
        } catch (SQLException e) {
            LoggerUtil.log("DATABASE_ERROR", "获取答案失败: " + e.getMessage());
        }
        return answers;
    }

    /** 获取某问题的所有答案 */
    public List<String> getAnswersByQuestion(Long questionId) {
        List<String> answers = new ArrayList<>();
        String sql = "SELECT answer_text FROM Answer WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String text = rs.getString("answer_text");
                if (text != null) answers.add(text);
            }
        } catch (SQLException e) {
            LoggerUtil.log("DATABASE_ERROR", "获取问题答案失败: " + e.getMessage());
        }
        return answers;
    }

    /** 删除响应（事务：先删答案，再删响应） */
    public boolean deleteResponse(Long responseId) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            try (PreparedStatement s = conn.prepareStatement(
                    "DELETE FROM Answer WHERE response_id = ?")) {
                s.setLong(1, responseId); s.executeUpdate();
            }
            try (PreparedStatement s = conn.prepareStatement(
                    "DELETE FROM Response WHERE reid = ?")) {
                s.setLong(1, responseId);
                boolean ok = s.executeUpdate() > 0;
                conn.commit();
                return ok;
            }
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (Exception ex) {}
            LoggerUtil.log("DATABASE_ERROR", "删除响应失败: " + e.getMessage());
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (Exception e) {}
            }
        }
        return false;
    }
}
