package SurveySystem.model;

import java.time.LocalDateTime;

public class Response {
    private Long reid;
    private Long rid;           // 对应数据库 RESPONDENT_ID（受访者ID）
    private Long questionnaireId; // 对应数据库 QUESTIONNAIRE_ID（问卷ID）
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean completed;

    // 构造函数
    public Response() {}

    public Response(Long reid, Long rid, LocalDateTime startTime, LocalDateTime endTime, Boolean completed) {
        this.reid = reid;
        this.rid = rid;
        this.startTime = startTime;
        this.endTime = endTime;
        this.completed = completed;
    }

    // Getter 和 Setter 方法
    public Long getReid() {
        return reid;
    }

    public void setReid(Long reid) {
        this.reid = reid;
    }

    public Long getRid() {
        return rid;
    }

    public void setRid(Long rid) {
        this.rid = rid;
    }

    public Long getQuestionnaireId() {
        return questionnaireId;
    }

    public void setQuestionnaireId(Long questionnaireId) {
        this.questionnaireId = questionnaireId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public void setCompleted(Boolean completed) {
        this.completed = completed;
    }
}