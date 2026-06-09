package SurveySystem.dao;

import SurveySystem.model.Question;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionDao {

    public static List<Question> getQuestionsByQuestionnaireId(Long questionnaireId) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Question WHERE questionnaire_id = ? ORDER BY sequence_number, qid";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) questions.add(extract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public static List<Question> getQuestionsByQuestionnaireIdAndType(Long questionnaireId, String type) {
        List<Question> questions = new ArrayList<>();
        String sql = "SELECT * FROM Question WHERE questionnaire_id = ? AND type = ? ORDER BY sequence_number";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            stmt.setString(2, type);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) questions.add(extract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return questions;
    }

    public static boolean addQuestion(Question question) {
        if (question == null || question.getQuestionnaireId() == null
                || question.getQtext() == null || question.getQtext().trim().isEmpty()) {
            System.err.println("Invalid question data");
            return false;
        }
        String sql = "INSERT INTO Question (questionnaire_id, qtext, type, options, is_required, sequence_number) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, question.getQuestionnaireId());
            stmt.setString(2, question.getQtext());
            stmt.setString(3, question.getType() != null ? question.getType() : "TEXT");
            stmt.setString(4, question.getOptions() != null ? question.getOptions() : null);
            stmt.setBoolean(5, Boolean.TRUE.equals(question.getIs_Required()));
            stmt.setInt(6, question.getSequence_Number());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) question.setid(keys.getLong(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean updateQuestion(Question question) {
        if (question == null || question.getid() == null) return false;
        String sql = "UPDATE Question SET qtext = ?, type = ?, options = ?, is_required = ?, sequence_number = ? WHERE qid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, question.getQtext());
            stmt.setString(2, question.getType() != null ? question.getType() : "TEXT");
            stmt.setString(3, question.getOptions() != null ? question.getOptions() : null);
            stmt.setBoolean(4, Boolean.TRUE.equals(question.getIs_Required()));
            stmt.setInt(5, question.getSequence_Number());
            stmt.setLong(6, question.getid());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean deleteQuestion(Long qid) {
        String sql = "DELETE FROM Question WHERE qid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, qid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Question extract(ResultSet rs) throws SQLException {
        Question q = new Question();
        q.setid(rs.getLong("qid"));
        q.setQuestionnaireId(rs.getLong("questionnaire_id"));
        q.setQtext(rs.getString("qtext"));
        q.setType(rs.getString("type"));
        q.setOptions(rs.getString("options"));
        q.setIs_Required(rs.getBoolean("is_required"));
        q.setSequence_Number(rs.getInt("sequence_number"));
        return q;
    }
}
