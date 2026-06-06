package SurveySystem.dao;

import SurveySystem.model.Answer;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 答案数据访问层
 * 表：Answer（answer_id, response_id, question_id, answer_text）
 */
public class AnswerDao {

    /** 新增答案 */
    public boolean addAnswer(Answer answer) {
        String sql = "INSERT INTO Answer (response_id, question_id, answer_text) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, answer.getResponseId());
            stmt.setLong(2, answer.getQuestionId());
            stmt.setString(3, answer.getAnswerText());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) answer.setId(keys.getLong(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 批量保存答案 */
    public int batchAddAnswers(Long responseId, java.util.Map<Long, String> answers) {
        String sql = "INSERT INTO Answer (response_id, question_id, answer_text) VALUES (?, ?, ?)";
        int saved = 0;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (java.util.Map.Entry<Long, String> entry : answers.entrySet()) {
                if (entry.getValue() == null || entry.getValue().trim().isEmpty()) continue;
                stmt.setLong(1, responseId);
                stmt.setLong(2, entry.getKey());
                stmt.setString(3, entry.getValue());
                stmt.addBatch();
            }
            int[] results = stmt.executeBatch();
            conn.commit();
            for (int r : results) {
                if (r >= 0 || r == Statement.SUCCESS_NO_INFO) saved++;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return saved;
    }

    /** 按问卷ID查询所有答案 */
    public static List<Answer> getByQuestionnaireId(Long questionnaireId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT a.* FROM Answer a " +
                "INNER JOIN Response resp ON a.response_id = resp.reid " +
                "WHERE resp.questionnaire_id = ? ORDER BY resp.reid, a.question_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) answers.add(mapToAnswer(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    /** 按问题ID查询 */
    public List<Answer> findByQuestionId(Long questionId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM Answer WHERE question_id = ? ORDER BY response_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) answers.add(mapToAnswer(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    /** 按响应ID查询 */
    public List<Answer> findByResponseId(Long responseId) {
        List<Answer> answers = new ArrayList<>();
        String sql = "SELECT * FROM Answer WHERE response_id = ? ORDER BY question_id";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, responseId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) answers.add(mapToAnswer(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return answers;
    }

    /** 统计问卷回答数 */
    public int countByQuestionnaireId(Long questionnaireId) {
        String sql = "SELECT COUNT(*) FROM Answer a " +
                "INNER JOIN Response resp ON a.response_id = resp.reid " +
                "WHERE resp.questionnaire_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** 删除响应下的所有答案 */
    public boolean deleteByResponseId(Long responseId) {
        String sql = "DELETE FROM Answer WHERE response_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, responseId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 删除问题下的所有答案 */
    public boolean deleteByQuestionId(Long questionId) {
        String sql = "DELETE FROM Answer WHERE question_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionId);
            return stmt.executeUpdate() >= 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Answer mapToAnswer(ResultSet rs) throws SQLException {
        Answer a = new Answer();
        a.setId(rs.getLong("answer_id"));
        a.setResponseId(rs.getLong("response_id"));
        a.setQuestionId(rs.getLong("question_id"));
        a.setAnswerText(rs.getString("answer_text"));
        return a;
    }
}
