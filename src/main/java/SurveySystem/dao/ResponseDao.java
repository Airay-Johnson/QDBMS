package SurveySystem.dao;

import SurveySystem.model.Response;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 响应数据访问层
 * 表：Response（reid, questionnaire_id, respondent_id, start_time, end_time, is_completed）
 */
public class ResponseDao {

    /** 新增响应记录（返回自增reid） */
    public Long addResponse(Long questionnaireId, Long respondentId, Timestamp startTime, Boolean completed) {
        String sql = "INSERT INTO Response (questionnaire_id, respondent_id, start_time, is_completed) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, questionnaireId);
            stmt.setLong(2, respondentId);
            stmt.setTimestamp(3, startTime);
            stmt.setBoolean(4, completed);
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 根据reid查询 */
    public Response findById(Long reid) {
        String sql = "SELECT * FROM Response WHERE reid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, reid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToResponse(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 按问卷ID查询所有响应 */
    public List<Response> findByQuestionnaireId(Long questionnaireId) {
        List<Response> list = new ArrayList<>();
        String sql = "SELECT * FROM Response WHERE questionnaire_id = ? ORDER BY start_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, questionnaireId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(mapToResponse(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 更新完成状态 */
    public boolean updateCompleted(Long reid, Timestamp endTime, Boolean completed) {
        String sql = "UPDATE Response SET end_time = ?, is_completed = ? WHERE reid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, endTime);
            stmt.setBoolean(2, completed);
            stmt.setLong(3, reid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 删除响应 */
    public boolean deleteById(Long reid) {
        String sql = "DELETE FROM Response WHERE reid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, reid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 统计某问卷响应数 */
    public int countByQuestionnaireId(Long questionnaireId) {
        String sql = "SELECT COUNT(*) FROM Response WHERE questionnaire_id = ?";
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

    private Response mapToResponse(ResultSet rs) throws SQLException {
        Response r = new Response();
        r.setReid(rs.getLong("reid"));
        r.setRid(rs.getLong("respondent_id"));
        r.setQuestionnaireId(rs.getLong("questionnaire_id"));
        Timestamp st = rs.getTimestamp("start_time");
        if (st != null) r.setStartTime(st.toLocalDateTime());
        Timestamp et = rs.getTimestamp("end_time");
        if (et != null) r.setEndTime(et.toLocalDateTime());
        r.setCompleted(rs.getBoolean("is_completed"));
        return r;
    }
}
