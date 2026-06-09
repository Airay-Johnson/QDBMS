package UI;


import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class DashboardController {

    @FXML private Label usernameLabel;
    @FXML private Button logoutButton;

    // 设置用户名（从登录界面传递过来）
    public void setUsername(String username) {
        usernameLabel.setText("欢迎, " + username + "!");
    }

    @FXML
    public void initialize() {
        // 设置退出登录按钮事件
        logoutButton.setOnAction(event -> {
            try {
                // 关闭当前窗口
                Stage stage = (Stage) logoutButton.getScene().getWindow();
                stage.close();

                // 重新打开登录窗口
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/login.fxml"));
                Parent root = loader.load();

                Scene scene = new Scene(root, 600, 400);
                scene.getStylesheets().add(getClass().getResource("/style/application.css").toExternalForm());

                Stage loginStage = new Stage();
                loginStage.setTitle("用户登录系统");
                loginStage.setScene(scene);
                loginStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
