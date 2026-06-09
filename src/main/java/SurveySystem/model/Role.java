package SurveySystem.model;

/**
 * 角色实体类，用于权限管理（如"管理员"、"普通用户"、"分析师"）
 */
public class Role {
    private Long roleId;        // 角色ID（主键）
    private String roleName;    // 角色名称（如"ADMIN"、"USER"、"ANALYST"）
    private String description; // 角色描述（如"系统管理员，拥有所有权限"）

    // 空构造函数
    public Role() {}

    // 带参构造函数
    public Role(Long roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    // Getter和Setter
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}