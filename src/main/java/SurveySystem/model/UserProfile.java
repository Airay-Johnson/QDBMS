package SurveySystem.model;

// UserProfile.java
import java.time.LocalDateTime;

public class UserProfile {
    private String username;
    private String encryptedPassword;
    private LocalDateTime saveTime;
    private boolean autoLoginEnabled;

    public UserProfile() {}

    public UserProfile(String username, String encryptedPassword, boolean autoLoginEnabled) {
        this.username = username;
        this.encryptedPassword = encryptedPassword;
        this.autoLoginEnabled = autoLoginEnabled;
        this.saveTime = LocalDateTime.now();
    }

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEncryptedPassword() { return encryptedPassword; }
    public void setEncryptedPassword(String encryptedPassword) { this.encryptedPassword = encryptedPassword; }

    public LocalDateTime getSaveTime() { return saveTime; }
    public void setSaveTime(LocalDateTime saveTime) { this.saveTime = saveTime; }

    public boolean isAutoLoginEnabled() { return autoLoginEnabled; }
    public void setAutoLoginEnabled(boolean autoLoginEnabled) { this.autoLoginEnabled = autoLoginEnabled; }

    @Override
    public String toString() {
        return username + (autoLoginEnabled ? " (自动登录)" : "");
    }
}
