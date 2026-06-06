// PermissionType.java
package SurveySystem.constant;

public enum PermissionType {
    SURVEY_CREATE("创建问卷"),
    SURVEY_EDIT("编辑问卷"),
    SURVEY_PUBLISH("发布问卷"),
    QUESTION_MANAGE("管理问题"),
    RESPONDENT_MANAGE("管理受访者"),
    RESPONSE_VIEW("查看回答"),
    DATA_ANALYZE("数据分析"),
    USER_MANAGE("用户管理"),
    PERMISSION_GRANT("权限授予"),
    LOG_VIEW("查看日志"),
    DATA_EXPORT("导出数据");

    private final String description;

    PermissionType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

