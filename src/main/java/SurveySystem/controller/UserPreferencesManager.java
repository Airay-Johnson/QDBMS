package SurveySystem.controller;

// UserPreferencesManager.java
import SurveySystem.model.UserProfile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class UserPreferencesManager {
    private static final String PREFS_FILE = "user_profiles.json";
    private List<UserProfile> userProfiles = new ArrayList<>();
    private Gson gson = new Gson();

    public UserPreferencesManager() {
        loadProfiles();
    }

    public void loadProfiles() {
        try {
            Path path = Paths.get(PREFS_FILE);
            if (Files.exists(path)) {
                String json = new String(Files.readAllBytes(path));
                userProfiles = gson.fromJson(json, new TypeToken<List<UserProfile>>(){}.getType());
                System.out.println("成功加载 " + userProfiles.size() + " 个用户配置");
            }
        } catch (IOException e) {
            System.err.println("加载用户配置失败: " + e.getMessage());
        }
    }

    public void saveProfiles() {
        try {
            String json = gson.toJson(userProfiles);
            Files.write(Paths.get(PREFS_FILE), json.getBytes());
            System.out.println("用户配置已保存");
        } catch (IOException e) {
            System.err.println("保存用户配置失败: " + e.getMessage());
        }
    }

    public void addOrUpdateProfile(UserProfile profile) {
        // 移除已存在的同名配置
        userProfiles.removeIf(p -> p.getUsername().equals(profile.getUsername()));
        userProfiles.add(profile);
        saveProfiles();
    }

    public void removeProfile(String username) {
        userProfiles.removeIf(p -> p.getUsername().equals(username));
        saveProfiles();
    }

    public List<UserProfile> getUserProfiles() {
        return new ArrayList<>(userProfiles);
    }

    public UserProfile getProfileByUsername(String username) {
        return userProfiles.stream()
                .filter(p -> p.getUsername().equals(username))
                .findFirst()
                .orElse(null);
    }
}
