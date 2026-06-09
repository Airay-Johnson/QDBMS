package SurveySystem.model;

public class User {
    private Long uid;
    private String username;
    private String password;
    private String email;
    private String salt;
    private Boolean isActive;
    private String role;

    // 无参构造函数
    public User() {
    }

    // 带参构造函数
    public User(Long uid, String username, String password, String email, String salt, Boolean isActive, String role) {
        this.uid = uid;
        this.username = username;
        this.password = password;
        this.email = email;
        this.salt = salt;
        this.isActive = isActive;
    }

    // Getter 和 Setter 方法
    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "User{" +
                "uid=" + uid +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", isActive=" + isActive +
                ", role='" + role + '\'' +
                '}';
    }
}