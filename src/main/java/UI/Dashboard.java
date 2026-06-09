package UI;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class Dashboard {
    @FXML private Label welcomeLabel;

    // 接收登录用户名
    public void setUsername(String username) {
        welcomeLabel.setText("欢迎, " + username + "!");
    }
}
