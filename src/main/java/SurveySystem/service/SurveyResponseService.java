package SurveySystem.service;

import SurveySystem.model.SurveyResponse;
import Util.DBUtil;
import Util.LoggerUtil;
import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static SurveySystem.service.AuthService.logger;
import static Util.JsonUtil.objectMapper;

public class SurveyResponseService {
    private static Gson gson = new Gson(); // 用于将答案Map转换为JSON字符串存储

    /**
     * 保存问卷提交记录
     *
     * @return
     */
    public boolean saveSurveyResponse(SurveyResponse response) {
        String sql = "INSERT INTO survey_responses (questionnaire_id, user_id, answers, submit_time) VALUES (?, ?, ?::json, NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, response.getQuestionnaireId());
            pstmt.setLong(2, response.getUserId());

            // 将答案Map转换为JSON字符串
            String answersJson = objectMapper.writeValueAsString(response.getAnswers());
            pstmt.setString(3, answersJson);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (Exception e) {
            LoggerUtil.error("保存问卷回答失败", e);
            return false;
        }
    }

    /**
     * 获取某个问卷的所有提交记录
     */
    @SuppressWarnings("unchecked")
    public List<SurveyResponse> getResponsesByQuestionnaireId(Long questionnaireId) throws SQLException {
        List<SurveyResponse> responses = new ArrayList<>();
        String sql = "SELECT * FROM survey_responses WHERE questionnaire_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, questionnaireId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SurveyResponse response = new SurveyResponse();
                    response.setId(rs.getLong("id"));
                    response.setQuestionnaireId(rs.getLong("questionnaire_id"));
                    response.setUserId(rs.getLong("user_id"));
                    response.setSubmitTime(rs.getTimestamp("submit_time"));
                    response.setUserIp(rs.getString("user_ip"));
                    response.setUserAgent(rs.getString("user_agent"));

                    // 将JSON字符串转换为Map
                    String answersJson = rs.getString("answers");
                    response.setAnswers(gson.fromJson(answersJson, Map.class));

                    responses.add(response);
                }
            }
        }

        return responses;
    }

    /**
     * 获取某个用户提交的问卷记录
     */
    public List<SurveyResponse> getResponsesByUserId(Long userId) throws SQLException {
        List<SurveyResponse> responses = new ArrayList<>();
        String sql = "SELECT * FROM survey_responses WHERE user_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    SurveyResponse response = new SurveyResponse();
                    response.setId(rs.getLong("id"));
                    response.setQuestionnaireId(rs.getLong("questionnaire_id"));
                    response.setUserId(rs.getLong("user_id"));
                    response.setSubmitTime(rs.getTimestamp("submit_time"));

                    responses.add(response);
                }
            }
        }

        return responses;
    }

    /**
     * 获取问卷的提交数量
     */
    public int getResponseCount(Long questionnaireId) throws SQLException {
        String sql = "SELECT COUNT(*) as count FROM survey_responses WHERE questionnaire_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, questionnaireId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        }

        return 0;
    }
}
