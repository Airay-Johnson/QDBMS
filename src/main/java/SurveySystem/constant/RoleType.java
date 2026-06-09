// RoleType.java
package SurveySystem.constant;

public enum RoleType {
    ADMIN("管理员", "系统管理员，拥有所有权限"),
    RESEARCHER("研究员", "可以进行数据分析和问卷管理"),
    SURVEYOR("调查员", "可以创建问卷和查看结果"),
    RESPONDENT("受访者", "只能填写问卷");

    private final String name;
    private final String description;

    RoleType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}