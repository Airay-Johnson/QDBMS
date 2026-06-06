package UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // 调试：检查FXML文件路径
        URL fxmlUrl = getClass().getResource("/view/login.fxml");
        if (fxmlUrl == null) {
            System.err.println("无法找到FXML文件: /view/login.fxml");
            System.err.println("尝试其他路径...");
            fxmlUrl = getClass().getResource("view/login.fxml");
            if (fxmlUrl == null) {
                System.err.println("也无法找到FXML文件: view/login.fxml");
                return;
            }
        }
        System.out.println("FXML文件位置: " + fxmlUrl);

        // 加载登录界面FXML文件
        Parent root = FXMLLoader.load(fxmlUrl);

        // 创建场景，设置宽高
        Scene scene = new Scene(root, 600, 400);

        // 应用CSS样式
        URL cssUrl = getClass().getResource("/style/application.css");
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("无法找到CSS文件: /style/application.css");
        }

        // 设置舞台属性
        primaryStage.setTitle("用户登录系统");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false); // 禁止调整窗口大小
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}