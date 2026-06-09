package Util;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.prefs.Preferences;

public class DatabaseConnectionTester extends Application {

    private TextField hostField;
    private TextField portField;
    private TextField databaseField;
    private TextField usernameField;
    private PasswordField passwordField;
    private TextArea outputArea;
    private Label statusLabel;

    private Preferences prefs;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        prefs = Preferences.userNodeForPackage(DatabaseConnectionTester.class);

        primaryStage.setTitle("OpenGauss 数据库连接测试工具");

        // 创建输入表单
        GridPane form = createForm();

        // 创建按钮区域
        HBox buttonBox = createButtonBox(primaryStage);

        // 创建输出区域
        outputArea = new TextArea();
        outputArea.setEditable(false);
        outputArea.setPrefHeight(200);

        // 状态标签
        statusLabel = new Label("就绪");
        statusLabel.setTextFill(Color.BLACK);

        // 主布局
        VBox mainLayout = new VBox(10);
        mainLayout.setPadding(new Insets(15));
        mainLayout.getChildren().addAll(form, buttonBox, outputArea, statusLabel);

        // 加载保存的配置
        loadSavedConfig();

        Scene scene = new Scene(mainLayout, 600, 500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private GridPane createForm() {
        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(10);
        form.setPadding(new Insets(10));

        // 表单字段
        hostField = new TextField();
        portField = new TextField("26000");
        databaseField = new TextField();
        usernameField = new TextField();
        passwordField = new PasswordField();

        // 添加标签和字段到表单
        form.add(new Label("主机:"), 0, 0);
        form.add(hostField, 1, 0);
        form.add(new Label("端口:"), 0, 1);
        form.add(portField, 1, 1);
        form.add(new Label("数据库:"), 0, 2);
        form.add(databaseField, 1, 2);
        form.add(new Label("用户名:"), 0, 3);
        form.add(usernameField, 1, 3);
        form.add(new Label("密码:"), 0, 4);
        form.add(passwordField, 1, 4);

        // 设置字段宽度
        hostField.setPrefWidth(250);
        portField.setPrefWidth(100);
        databaseField.setPrefWidth(250);
        usernameField.setPrefWidth(250);
        passwordField.setPrefWidth(250);

        return form;
    }

    private HBox createButtonBox(Stage stage) {
        Button testBtn = new Button("测试连接");
        testBtn.setOnAction(e -> testConnection());

        Button saveBtn = new Button("保存配置");
        saveBtn.setOnAction(e -> saveConfig());

        Button loadBtn = new Button("加载配置");
        loadBtn.setOnAction(e -> loadSavedConfig());

        Button clearBtn = new Button("清除输出");
        clearBtn.setOnAction(e -> outputArea.clear());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(testBtn, saveBtn, loadBtn, clearBtn);

        return buttonBox;
    }

    private void testConnection() {
        String host = hostField.getText();
        String port = portField.getText();
        String database = databaseField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (host.isEmpty() || username.isEmpty()) {
            showError("主机和用户名不能为空");
            return;
        }

        String url = String.format("jdbc:opengauss://%s:%s/%s", host, port, database);

        outputArea.appendText("尝试连接到: " + url + "\n");
        outputArea.appendText("用户名: " + username + "\n");
        statusLabel.setText("正在连接...");
        statusLabel.setTextFill(Color.BLUE);

        try {
            // 加载驱动
            Class.forName("org.opengauss.Driver");
            outputArea.appendText("OpenGauss JDBC驱动加载成功\n");

            // 尝试连接
            Connection conn = DriverManager.getConnection(url, username, password);
            outputArea.appendText("连接成功！\n");

            // 获取数据库信息
            outputArea.appendText("数据库产品: " + conn.getMetaData().getDatabaseProductName() + "\n");
            outputArea.appendText("数据库版本: " + conn.getMetaData().getDatabaseProductVersion() + "\n");
            outputArea.appendText("驱动版本: " + conn.getMetaData().getDriverVersion() + "\n");

            conn.close();

            statusLabel.setText("连接成功");
            statusLabel.setTextFill(Color.GREEN);

        } catch (ClassNotFoundException e) {
            showError("找不到OpenGauss JDBC驱动: " + e.getMessage());
            outputArea.appendText("请确保opengauss-jdbc驱动已添加到类路径中\n");
        } catch (SQLException e) {
            showError("数据库连接错误: " + e.getMessage());
            outputArea.appendText("错误代码: " + e.getErrorCode() + "\n");
            outputArea.appendText("SQL状态: " + e.getSQLState() + "\n");

            // 提供可能的解决方案
            provideSolutions(e);
        }
    }

    private void provideSolutions(SQLException e) {
        outputArea.appendText("\n可能的解决方案:\n");

        if (e.getMessage().contains("Invalid username/password")) {
            outputArea.appendText("1. 检查用户名和密码是否正确\n");
            outputArea.appendText("2. 确保用户存在于数据库中\n");
            outputArea.appendText("3. 检查密码是否包含特殊字符，可能需要转义\n");
        } else if (e.getMessage().contains("Connection refused")) {
            outputArea.appendText("1. 检查数据库主机和端口是否正确\n");
            outputArea.appendText("2. 确保数据库服务正在运行\n");
            outputArea.appendText("3. 检查防火墙设置\n");
        } else if (e.getMessage().contains("database") && e.getMessage().contains("does not exist")) {
            outputArea.appendText("1. 检查数据库名称是否正确\n");
            outputArea.appendText("2. 确保数据库已创建\n");
        }

        outputArea.appendText("4. 检查pg_hba.conf配置，确保允许从当前主机连接\n");
        outputArea.appendText("5. 确认数据库监听配置（postgresql.conf）\n");
    }

    private void showError(String message) {
        statusLabel.setText("错误: " + message);
        statusLabel.setTextFill(Color.RED);
        outputArea.appendText("错误: " + message + "\n");
    }

    private void saveConfig() {
        prefs.put("host", hostField.getText());
        prefs.put("port", portField.getText());
        prefs.put("database", databaseField.getText());
        prefs.put("username", usernameField.getText());

        // 注意：密码保存不安全，仅用于演示
        prefs.put("password", passwordField.getText());

        outputArea.appendText("配置已保存\n");
    }

    private void loadSavedConfig() {
        hostField.setText(prefs.get("host", ""));
        portField.setText(prefs.get("port", "26000"));
        databaseField.setText(prefs.get("database", ""));
        usernameField.setText(prefs.get("username", ""));
        passwordField.setText(prefs.get("password", ""));

        outputArea.appendText("配置已加载\n");
    }
}