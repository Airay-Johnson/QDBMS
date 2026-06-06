package SurveySystem.model;

import java.sql.Timestamp;

/**
 * 答案实体（Answer）
 * 对应数据库 Answer 表
 * 表结构：ANSWER_ID(PK), RESPONSE_ID(FK), QUESTION_ID(FK), ANSWER_TEXT, CREATE_TIME
 */
public class Answer {

    private Long id;                 // ANSWER_ID
    private Long responseId;         // RESPONSE_ID（关联响应表）
    private Long questionId;          // QUESTION_ID（关联问题表）
    private String answerText;       // ANSWER_TEXT（答案内容）

    // 兼容旧字段（尽量避免使用）
    private Long QUESTIONNAIRE_ID;
    private Long userId;
    private String answerContent;

    // 无参构造函数
    public Answer() {}

    // 带参构造函数
    public Answer(Long responseId, Long questionId, String answerText) {
        this.responseId = responseId;
        this.questionId = questionId;
        this.answerText = answerText;
    }

    // Getter 和 Setter
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getResponseId() {
        return responseId;
    }

    public void setResponseId(Long responseId) {
        this.responseId = responseId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    // 兼容旧代码的 getter（映射到 answerText）
    public String getAnswerContent() {
        return answerText != null ? answerText : answerContent;
    }

    // 兼容旧代码的 setter
    public void setAnswerContent(String answerContent) {
        this.answerContent = answerContent;
        if (this.answerText == null) {
            this.answerText = answerContent;
        }
    }

    // 兼容旧字段
    public Long getQuestionnaireId() {
        return QUESTIONNAIRE_ID;
    }

    public void setQuestionnaireId(Long questionnaireId) {
        this.QUESTIONNAIRE_ID = questionnaireId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "Answer{" +
                "id=" + id +
                ", responseId=" + responseId +
                ", questionId=" + questionId +
                ", answerText='" + answerText + '\'' +
                '}';
    }
}
