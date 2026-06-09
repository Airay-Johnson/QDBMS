package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.model.*;
import SurveySystem.service.*;
import Util.SessionManager;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 问卷填写界面控制器
 * 支持：问卷展示、受访者注册、答案收集、提交存储
 */
public class SurveyResponseController {

    private static final Logger logger = LoggerFactory.getLogger(SurveyResponseController.class);

    // ===== FXML 控件 =====
    @FXML
    private Label questionnaireTitle;

    @FXML
    private Label questionnaireDescription;

    @FXML
    private VBox questionsContainer;

    @FXML
    private Button submitBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private ProgressBar progressBar;

    @FXML
    private Label progressLabel;

    @FXML
    private VBox respondentPanel;   // 受访者信息面板（初始显示）

    @FXML
    private TextField emailField;

    @FXML
    private TextField ageField;

    @FXML
    private ComboBox<String> sexCombo;

    @FXML
    private TextField addressField;

    @FXML
    private Button startSurveyBtn;

    // ===== 内部数据 =====
    private Questionnaire currentQuestionnaire;
    private Respondent currentRespondent;
    private List<Question> questions;
    private Map<Long, Object> answers = new HashMap<>();   // 问题ID -> 答案
    private int totalQuestions = 0;
    private int answeredQuestions = 0;

    // ===== 服务层 =====
    private QuestionnaireService questionnaireService = new QuestionnaireService();
    private RespondentService respondentService = new RespondentService();
    private ResponseService responseService = new ResponseService();
    private SurveyResponseService surveyResponseService = new SurveyResponseService();

    // 当前登录用户（用于记录提交者信息，可选）
    private User currentUser;

    /**
     * 设置当前登录用户（由DashboardController调用）
     */
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    private final ObjectMapper objectMapper = new ObjectMapper();

    @FXML
    public void initialize() {
        questions = new ArrayList<>();
        sexCombo.getItems().addAll("MALE", "FEMALE", "OTHER");
        sexCombo.setValue("MALE");

        submitBtn.setDisable(true);
        submitBtn.setOnAction(this::handleSubmit);
        cancelBtn.setOnAction(this::handleCancel);
        startSurveyBtn.setOnAction(this::handleStartSurvey);

        updateProgress();
    }

    /**
     * 接收从选择界面传来的问卷对象
     */
    public void setQuestionnaire(Questionnaire questionnaire) {
        this.currentQuestionnaire = questionnaire;

        if (questionnaire == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "问卷数据为空");
            return;
        }

        questionnaireTitle.setText(questionnaire.getTitle());
        questionnaireDescription.setText(
                questionnaire.getDescription() != null && !questionnaire.getDescription().isEmpty()
                        ? questionnaire.getDescription()
                        : "暂无描述"
        );

        // 如果问卷未发布，提示并退出
        if (!questionnaire.isPublished()) {
            showAlert(Alert.AlertType.WARNING, "提示", "该问卷尚未发布，暂时无法填写");
            return;
        }

        // 显示受访者信息填写面板
        respondentPanel.setVisible(true);
        respondentPanel.setManaged(true);
        questionsContainer.getChildren().clear();
    }

    /**
     * 点击"开始填写"按钮：注册受访者，加载问卷问题
     */
    @FXML
    private void handleStartSurvey(ActionEvent event) {
        // 1. 收集受访者信息
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "警告", "邮箱不能为空");
            return;
        }

        Integer age = null;
        if (!ageField.getText().trim().isEmpty()) {
            try {
                age = Integer.parseInt(ageField.getText().trim());
            } catch (NumberFormatException e) {
                showAlert(Alert.AlertType.WARNING, "警告", "年龄格式错误");
                return;
            }
        }

        String sex = sexCombo.getValue();
        String address = addressField.getText().trim();

        // 2. 注册受访者
        currentRespondent = respondentService.register(email, age, sex, address);
        if (currentRespondent == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "受访者注册失败，请重试");
            return;
        }

        // 3. 隐藏受访者面板，加载问卷问题
        respondentPanel.setVisible(false);
        respondentPanel.setManaged(false);

        // 4. 加载问卷问题
        loadQuestions();
    }

    /**
     * 加载问卷问题列表
     */
    private void loadQuestions() {
        if (currentQuestionnaire == null) return;

        // 从数据库加载问题
        questions = questionnaireService.getQuestionnaireWithQuestions(currentQuestionnaire.getId()).getQuestions();
        if (questions == null) questions = new ArrayList<>();

        totalQuestions = questions.size();
        answeredQuestions = 0;
        updateProgress();

        questionsContainer.getChildren().clear();

        for (int i = 0; i < questions.size(); i++) {
            Question q = questions.get(i);
            VBox box = createQuestionUI(q, i + 1);
            questionsContainer.getChildren().add(box);
        }

        if (questions.isEmpty()) {
            Label empty = new Label("该问卷暂没有问题");
            empty.setStyle("-fx-text-fill: #999; -fx-font-size: 14px; -fx-padding: 20;");
            questionsContainer.getChildren().add(empty);
        }
    }

    /**
     * 根据问题类型创建UI组件
     */
    private VBox createQuestionUI(Question question, int index) {
        VBox container = new VBox(10);
        container.setStyle(
                "-fx-background-color: white; -fx-padding: 15; " +
                "-fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 5, 0, 0, 2);"
        );
        container.setUserData(question.getid());

        // 问题标题
        Label qLabel = new Label(index + ". " + question.getQtext());
        qLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        qLabel.setWrapText(true);
        container.getChildren().add(qLabel);

        String type = (question.getType() != null ? question.getType().toUpperCase() : "");

        switch (type) {
            case "SINGLE_CHOICE":
                createSingleChoiceUI(question, container);
                break;
            case "MULTIPLE_CHOICE":
                createMultipleChoiceUI(question, container);
                break;
            case "TEXT":
                createTextInputUI(question, container);
                break;
            default:
                Label unknown = new Label("【暂不支持的问题类型: " + type + "】");
                unknown.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 13px;");
                container.getChildren().add(unknown);
        }

        return container;
    }

    private void createSingleChoiceUI(Question question, VBox container) {
        ToggleGroup group = new ToggleGroup();
        VBox opts = new VBox(8);
        opts.setStyle("-fx-padding: 5 0 0 20;");

        List<Option> options = parseOptions(question.getOptions());
        if (options.isEmpty()) {
            opts.getChildren().add(new Label("(无选项)"));
            container.getChildren().add(opts);
            return;
        }

        for (Option opt : options) {
            RadioButton rb = new RadioButton(opt.getText());
            rb.setToggleGroup(group);
            rb.setUserData(opt.getId());
            rb.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            rb.setWrapText(true);

            rb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    answers.put(question.getid(), rb.getUserData());
                    refreshAnsweredCount();
                    updateProgress();
                }
            });
            opts.getChildren().add(rb);
        }
        container.getChildren().add(opts);
    }

    private void createMultipleChoiceUI(Question question, VBox container) {
        VBox opts = new VBox(8);
        opts.setStyle("-fx-padding: 5 0 0 20;");

        List<Option> options = parseOptions(question.getOptions());
        if (options.isEmpty()) {
            opts.getChildren().add(new Label("(无选项)"));
            container.getChildren().add(opts);
            return;
        }

        for (Option opt : options) {
            CheckBox cb = new CheckBox(opt.getText());
            cb.setUserData(opt.getId());
            cb.setStyle("-fx-font-size: 14px; -fx-text-fill: #333;");
            cb.setWrapText(true);

            cb.selectedProperty().addListener((obs, oldVal, newVal) -> {
                @SuppressWarnings("unchecked")
                List<String> selected = (List<String>) answers.computeIfAbsent(
                        question.getid(), k -> new ArrayList<>());
                String id = (String) cb.getUserData();
                if (newVal && !selected.contains(id)) {
                    selected.add(id);
                } else if (!newVal) {
                    selected.remove(id);
                }
                refreshAnsweredCount();
                updateProgress();
            });
            opts.getChildren().add(cb);
        }
        container.getChildren().add(opts);
    }

    private void createTextInputUI(Question question, VBox container) {
        TextArea ta = new TextArea();
        ta.setPromptText("请输入您的回答...");
        ta.setPrefHeight(80);
        ta.setStyle("-fx-font-size: 14px;");
        ta.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.trim().isEmpty()) {
                answers.put(question.getid(), newVal.trim());
            } else {
                answers.remove(question.getid());
            }
            refreshAnsweredCount();
            updateProgress();
        });
        container.getChildren().add(ta);
    }

    /**
     * 解析问题选项（支持JSON格式和逗号分隔格式）
     */
    private List<Option> parseOptions(String optionsJson) {
        List<Option> result = new ArrayList<>();
        if (optionsJson == null || optionsJson.trim().isEmpty()) return result;

        optionsJson = optionsJson.trim();

        // 尝试 JSON 格式
        if (optionsJson.startsWith("[")) {
            try {
                List<Map<String, String>> parsed = objectMapper.readValue(
                        optionsJson, new TypeReference<List<Map<String, String>>>() {});
                for (Map<String, String> item : parsed) {
                    Option opt = new Option();
                    opt.setId(item.getOrDefault("id", ""));
                    opt.setText(item.getOrDefault("text", ""));
                    result.add(opt);
                }
                return result;
            } catch (Exception e) {
                // JSON解析失败，尝试其他格式
            }
        }

        // 逗号分隔格式: A.选项1,B.选项2 或 直接"选项1,选项2"
        String[] parts = optionsJson.split(",");
        char label = 'A';
        for (String part : parts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            Option opt = new Option();
            // 去掉开头的 "A." "B." 等前缀
            if (part.length() > 2 && part.charAt(1) == '.') {
                opt.setId(String.valueOf(part.charAt(0)));
                opt.setText(part.substring(2).trim());
            } else {
                opt.setId(String.valueOf(label));
                opt.setText(part);
            }
            result.add(opt);
            label++;
        }
        return result;
    }

    /**
     * 刷新已答问题计数
     */
    private void refreshAnsweredCount() {
        answeredQuestions = 0;
        for (Question q : questions) {
            Object ans = answers.get(q.getid());
            if (ans != null) {
                if (ans instanceof List) {
                    if (!((List<?>) ans).isEmpty()) answeredQuestions++;
                } else if (!ans.toString().trim().isEmpty()) {
                    answeredQuestions++;
                }
            }
        }
    }

    private void updateProgress() {
        double progress = totalQuestions > 0 ? (double) answeredQuestions / totalQuestions : 0;
        progressBar.setProgress(progress);
        progressLabel.setText(String.format("已完成 %d/%d 题", answeredQuestions, totalQuestions));
        submitBtn.setDisable(answeredQuestions < totalQuestions);
    }

    /**
     * 提交问卷
     */
    @FXML
    private void handleSubmit(ActionEvent event) {
        if (currentRespondent == null) {
            showAlert(Alert.AlertType.ERROR, "错误", "受访者信息无效");
            return;
        }

        if (!validateRequiredQuestions()) {
            showAlert(Alert.AlertType.WARNING, "警告", "请完成所有必填问题");
            return;
        }

        try {
            // 1. 创建响应记录（关联当前受访者）
            Long responseId = responseService.createResponse(
                    currentQuestionnaire.getId(),
                    currentRespondent.getRid()
            );
            if (responseId == null) {
                showAlert(Alert.AlertType.ERROR, "错误", "创建响应记录失败");
                return;
            }

            // 2. 保存每道题的答案
            for (Question q : questions) {
                Object ans = answers.get(q.getid());
                if (ans == null) continue;

                String ansText;
                if (ans instanceof List) {
                    ansText = objectMapper.writeValueAsString(ans);
                } else {
                    ansText = ans.toString();
                }

                boolean saved = responseService.saveAnswer(responseId, q.getid(), ansText);
                if (!saved) {
                    logger.warn("保存答案失败: QID={}", q.getid());
                }
            }

            // 3. 标记响应为完成
            responseService.completeResponse(responseId);

            showAlert(Alert.AlertType.INFORMATION, "提交成功",
                    "感谢您的参与！问卷已成功提交。");
            logger.info("受访者 {} 提交问卷 {} 成功，响应ID={}",
                    currentRespondent.getEmail(), currentQuestionnaire.getId(), responseId);

            closeWindow();

        } catch (Exception e) {
            logger.error("提交问卷异常", e);
            showAlert(Alert.AlertType.ERROR, "系统错误",
                    "提交时发生异常: " + e.getMessage());
        }
    }

    /**
     * 简单验证：所有问题都已作答（更严格可按题目的isRequired判断）
     */
    private boolean validateRequiredQuestions() {
        refreshAnsweredCount();
        return answeredQuestions >= totalQuestions;
    }

    @FXML
    private void handleCancel(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认取消");
        confirm.setHeaderText("确定要取消填写吗？");
        confirm.setContentText("未提交的内容将丢失。");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            closeWindow();
        }
    }

    @FXML
    private void closeWindow() {
        Stage stage = (Stage) questionsContainer.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
