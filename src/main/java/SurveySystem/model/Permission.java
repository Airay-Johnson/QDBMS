package SurveySystem.model;

/**
 * 权限实体类，定义系统支持的具体权限（如"创建问卷"、"导出报告"）
 */
public class Permission {
    private Long pid;           // 权限ID（主键）
    private String name;        // 权限名称（如"SURVEY_CREATE"、"DATA_EXPORT"）
    private String description; // 权限描述（如"允许创建新问卷"）
    private Integer pLevel;     // 权限级别（可选，用于权限优先级控制）

    // 空构造函数
    public Permission() {}

    // 带参构造函数
    public Permission(Long pid, String name, String description, Integer pLevel) {
        this.pid = pid;
        this.name = name;
        this.description = description;
        this.pLevel = pLevel;
    }

    // Getter和Setter
    public Long getPid() {
        return pid;
    }

    public void setPid(Long pid) {
        this.pid = pid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPLevel() {
        return pLevel;
    }

    public void setPLevel(Integer pLevel) {
        this.pLevel = pLevel;
    }
}