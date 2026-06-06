package SurveySystem.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class SurveyResponse {
    private Long id;                  // 提交记录ID
    private Long QUESTIONNAIRE_ID;     // 关联问卷ID
    private Long Uid;              // 提交用户ID（可为null表示匿名提交）
    private Timestamp submitTime;     // 提交时间
    private Map<Long, Object> answers; // 答案集合（问题ID -> 答案）
    private String userIp;            // 提交者IP地址
    private String userAgent;         // 提交者浏览器信息

    public SurveyResponse() {
        this.answers = new HashMap<>();
        this.submitTime = new Timestamp(System.currentTimeMillis());
    }

    public SurveyResponse(Long questionnaireId, Long userId) {
        this();
        this.QUESTIONNAIRE_ID = questionnaireId;
        this.Uid = userId;
    }

    // 添加答案
    public void addAnswer(Long questionId, Object answer) {
        answers.put(questionId, answer);
    }

    // 获取单个问题答案
    public Object getAnswer(Long questionId) {
        return answers.get(questionId);
    }

    // getter和setter
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getQuestionnaireId() { return QUESTIONNAIRE_ID; }
    public void setQuestionnaireId(Long questionnaireId) { this.QUESTIONNAIRE_ID = questionnaireId; }

    public Long getUserId() { return Uid; }
    public void setUserId(Long userId) { this.Uid = userId; }

    public Timestamp getSubmitTime() { return submitTime; }
    public void setSubmitTime(Timestamp submitTime) { this.submitTime = submitTime; }

    public Map<Long, Object> getAnswers() { return answers; }
    public void setAnswers(Map<Long, Object> answers) { this.answers = answers; }

    public String getUserIp() { return userIp; }
    public void setUserIp(String userIp) { this.userIp = userIp; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
