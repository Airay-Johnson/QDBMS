package SurveySystem.service;

import SurveySystem.dao.QuestionDao;
import SurveySystem.dao.QuestionnaireDao;
import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionnaireService {

    /**
     * 获取当前用户创建的问卷列表
     */
    public List<Questionnaire> getAllQuestionnaires() {
        List<Questionnaire> list = new ArrayList<>();
        String sql = "SELECT * FROM Questionnaire ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Questionnaire getQuestionnaireById(Long id) {
        String sql = "SELECT * FROM Questionnaire WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return extract(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean createQuestionnaire(Questionnaire questionnaire) {
        String sql = "INSERT INTO Questionnaire (uid, title, description, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, questionnaire.getUid());
            stmt.setString(2, questionnaire.getTitle());
            stmt.setString(3, questionnaire.getDescription());
            stmt.setString(4, questionnaire.getStation() != null && questionnaire.getStation() ? "PUBLISHED" : "DRAFT");
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) questionnaire.setId(keys.getLong(1));
            }
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateQuestionnaire(Questionnaire questionnaire) {
        String sql = "UPDATE Questionnaire SET title = ?, description = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, questionnaire.getTitle());
            stmt.setString(2, questionnaire.getDescription());
            stmt.setString(3, questionnaire.getStation() != null && questionnaire.getStation() ? "PUBLISHED" : "DRAFT");
            stmt.setLong(4, questionnaire.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateQuestionnaireStatus(Long id, Boolean status) {
        String sql = "UPDATE Questionnaire SET status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status != null && status ? "PUBLISHED" : "DRAFT");
            stmt.setLong(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteQuestionnaire(Long id) {
        String sql = "DELETE FROM Questionnaire WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Questionnaire> getQuestionnairesByUser(Long uid) {
        List<Questionnaire> list = new ArrayList<>();
        String sql = "SELECT * FROM Questionnaire WHERE uid = ? ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, uid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(extract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Questionnaire getQuestionnaireWithQuestions(Long questionnaireId) {
        Questionnaire q = getQuestionnaireById(questionnaireId);
        if (q != null) {
            List<Question> questions = QuestionDao.getQuestionsByQuestionnaireId(questionnaireId);
            q.setQuestions(questions);
        }
        return q;
    }

    public List<Questionnaire> getPublishedQuestionnaires() {
        List<Questionnaire> list = new ArrayList<>();
        String sql = "SELECT * FROM Questionnaire WHERE status = 'PUBLISHED' ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(extract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private Long generateNewQuestionnaireId() {
        String sql = "SELECT MAX(id) FROM Questionnaire";
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next() && rs.getObject(1) != null) {
                return rs.getLong(1) + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 1L;
    }

    private Questionnaire extract(ResultSet rs) throws SQLException {
        Questionnaire q = new Questionnaire();
        q.setId(rs.getLong("id"));
        q.setUid(rs.getLong("uid"));
        q.setTitle(rs.getString("title"));
        q.setDescription(rs.getString("description"));
        String status = rs.getString("status");
        q.setStation("PUBLISHED".equalsIgnoreCase(status));
        Timestamp createTime = rs.getTimestamp("create_time");
        if (createTime != null) q.setCreateTime(createTime);
        return q;
    }
}
