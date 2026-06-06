package SurveySystem.dao;

import SurveySystem.model.Questionnaire;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class QuestionnaireDao {

    /**
     * 获取用户可分析的所有已发布问卷
     */
    public static List<Questionnaire> getQuestionnairesForAnalysis(int userId) {
        List<Questionnaire> list = new ArrayList<>();
        String sql = "SELECT DISTINCT q.* FROM Questionnaire q " +
                "WHERE q.status = 'PUBLISHED' " +
                "AND (q.uid = ? OR q.uid IN (SELECT uid FROM \"User\" WHERE uid = ?)) " +
                "ORDER BY q.create_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(extract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 根据ID查询
     */
    public static Questionnaire getQuestionnaireById(int id) {
        String sql = "SELECT * FROM Questionnaire WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return extract(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 按创建者查询
     */
    public static List<Questionnaire> getQuestionnairesByCreator(int userId) {
        List<Questionnaire> list = new ArrayList<>();
        String sql = "SELECT * FROM Questionnaire WHERE uid = ? ORDER BY create_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(extract(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 获取所有已发布问卷
     */
    public static List<Questionnaire> getAllPublishedQuestionnaires() {
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

    /**
     * 创建问卷（自动返回自增ID）
     */
    public static boolean createQuestionnaire(Questionnaire q) {
        String sql = "INSERT INTO Questionnaire (uid, title, description, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, q.getUid());
            stmt.setString(2, q.getTitle());
            stmt.setString(3, q.getDescription());
            // 默认状态：草稿
            stmt.setString(4, q.getStation() != null && q.getStation() ? "PUBLISHED" : "DRAFT");
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) q.setId(keys.getLong(1));
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新问卷
     */
    public static boolean updateQuestionnaire(Questionnaire q) {
        String sql = "UPDATE Questionnaire SET title = ?, description = ?, status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, q.getTitle());
            stmt.setString(2, q.getDescription());
            stmt.setString(3, q.getStation() != null && q.getStation() ? "PUBLISHED" : "DRAFT");
            stmt.setLong(4, q.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 删除问卷
     */
    public static boolean deleteQuestionnaire(int id) {
        String sql = "DELETE FROM Questionnaire WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 更新问卷发布状态
     */
    public static boolean updateQuestionnaireStatus(int id, boolean published) {
        String sql = "UPDATE Questionnaire SET status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, published ? "PUBLISHED" : "DRAFT");
            stmt.setInt(2, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 权限检查（创建者或被分享者）
     */
    public static boolean hasAccessPermission(int questionnaireId, int userId) {
        String sql = "SELECT COUNT(*) FROM Questionnaire WHERE id = ? AND uid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, questionnaireId);
            stmt.setInt(2, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Questionnaire extract(ResultSet rs) throws SQLException {
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
