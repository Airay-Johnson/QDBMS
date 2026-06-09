package SurveySystem.model;

import java.sql.Timestamp;

/**
 * 系统操作日志实体类，记录用户关键操作
 */
public class Log {
    private Long logId;         // 日志ID（主键）
    private Long uid;           // 操作用户ID（外键，关联User表）
    private String operation;   // 操作类型（如"ADD_QUESTION"、"PUBLISH_SURVEY"）
    private String target;      // 操作目标（如"问卷ID=123"、"问题ID=456"）
    private Timestamp opTime;   // 操作时间
    // FXML 展示用字段（非数据库字段）
    private String userName;   // 用户名（JOIN查询得到）
    private String details;    // 详情（展示用）
    private java.time.LocalDateTime operationTime; // 操作时间的LocalDateTime版本

    // 空构造函数
    public Log() {}

    // 带参构造函数（创建日志时使用）
    public Log(Long uid, String operation, String target, Timestamp opTime) {
        this.uid = uid;
        this.operation = operation;
        this.target = target;
        this.opTime = opTime;
    }

    // Getter和Setter
    public Long getLogId() {
        return logId;
    }

    public void setLogId(Long logId) {
        this.logId = logId;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Timestamp getOpTime() {
        return opTime;
    }

    public void setOpTime(Timestamp opTime) {
        this.opTime = opTime;
        if (opTime != null) {
            this.operationTime = opTime.toLocalDateTime();
        }
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public java.time.LocalDateTime getOperationTime() {
        return operationTime;
    }

    public void setOperationTime(java.time.LocalDateTime operationTime) {
        this.operationTime = operationTime;
    }
}