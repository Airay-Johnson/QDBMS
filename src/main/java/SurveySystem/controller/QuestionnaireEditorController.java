package SurveySystem.controller;

import SurveySystem.model.Question;
import SurveySystem.model.Questionnaire;
import SurveySystem.service.QuestionnaireService;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class QuestionnaireEditorController implements Initializable {

    @FXML
    private TextField titleField;  // 问卷标题输入框
    @FXML
    private TextArea descriptionArea;  // 问卷描述输入框
    @FXML
    private VBox questionsContainer;  // 问题容器
    @FXML
    private Button addQuestionBtn;  // 添加问题按钮
    @FXML
    private Button saveBtn;  // 保存按钮
    @FXML
    private Button cancelBtn;  // 取消按钮
    @FXML
    private CheckBox isPublishedCheckbox;  // 是否发布复选框

    private Questionnaire currentQuestionnaire;  // 当前编辑的问卷
    private ObservableList<Question> questions = FXCollections.observableArrayList();  // 问题列表
    private QuestionnaireService questionnaireService = new QuestionnaireService();  // 问卷服务
    private OnSaveListener saveListener;  // 保存回调接口

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化UI组件
        initUI();
        // 绑定事件
        bindEvents();
    }

    // 初始化UI
    private void initUI() {
        // 禁用保存按钮直到标题不为空
        saveBtn.disableProperty().bind(Bindings.isEmpty(titleField.textProperty()));

        // 初始化问题列表显示
        refreshQuestionsList();
    }

    // 绑定事件处理
    private void bindEvents() {
        // 添加问题按钮事件
        addQuestionBtn.setOnAction(e -> addNewQuestion());

        // 保存按钮事件
        saveBtn.setOnAction(e -> saveQuestionnaire());

        // 取消按钮事件
        cancelBtn.setOnAction(e -> closeWindow());
    }

    // 接收并初始化问卷数据
    public void initData(Questionnaire questionnaire) {
        this.currentQuestionnaire = questionnaire;

        if (questionnaire != null) {
            // 填充表单数据
            titleField.setText(questionnaire.getTitle());
            descriptionArea.setText(questionnaire.getDescription());
            isPublishedCheckbox.setSelected(questionnaire.isPublished());

            // 加载问题列表
            if (questionnaire.getQuestions() != null) {
                questions.addAll(questionnaire.getQuestions());
            }
        } else {
            // 如果是新问卷，初始化默认值
            this.currentQuestionnaire = Questionnaire.builder()
                    .station(false)
                    .build();
        }

        refreshQuestionsList();
    }

    // 添加新问题
    private void addNewQuestion() {
        Question newQuestion = new Question();
        newQuestion.setQuestionnaireId(currentQuestionnaire.getId());
        newQuestion.setType("singleChoice");  // 与Questionnaire类中使用的类型保持一致
        newQuestion.setQtext("新问题");
        questions.add(newQuestion);
        currentQuestionnaire.addQuestion(newQuestion);  // 使用Questionnaire类的方法
        refreshQuestionsList();
    }

    // 刷新问题列表显示
    private void refreshQuestionsList() {
        questionsContainer.getChildren().clear();

        for (int i = 0; i < questions.size(); i++) {
            Question question = questions.get(i);
            VBox questionBox = createQuestionBox(question, i);
            questionsContainer.getChildren().add(questionBox);
        }
    }

    // 创建问题编辑框
    private VBox createQuestionBox(Question question, int index) {
        VBox box = new VBox(5);
        box.getStyleClass().add("question-item");

        // 问题内容输入
        TextField questionField = new TextField(question.getQtext());
        questionField.setPromptText("请输入问题内容");
        questionField.textProperty().addListener((obs, oldVal, newVal) ->
                question.setQtext(newVal));

        // 问题类型选择 - 与Questionnaire类中定义的类型保持一致
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("singleChoice", "multiple_choices", "text");
        typeCombo.setValue(question.getType());
        typeCombo.valueProperty().addListener((obs, oldVal, newVal) ->
                question.setType(newVal));

        // 删除按钮
        Button deleteBtn = new Button("删除");
        deleteBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        deleteBtn.setOnAction(e -> {
            Question removed = questions.remove(index);
            currentQuestionnaire.removeQuestion(removed);  // 使用Questionnaire类的方法
            refreshQuestionsList();
        });

        HBox questionTools = new HBox(10);
        questionTools.getChildren().addAll(typeCombo, deleteBtn);

        box.getChildren().addAll(questionField, questionTools);
        return box;
    }

    // 保存问卷
    private void saveQuestionnaire() {
        if (currentQuestionnaire == null) {
            showAlert("错误", "问卷对象为空", Alert.AlertType.ERROR);
            return;
        }

        // 更新问卷基本信息
        currentQuestionnaire.setTitle(titleField.getText());
        currentQuestionnaire.setDescription(descriptionArea.getText());

        // 根据复选框状态更新发布状态
        if (isPublishedCheckbox.isSelected()) {
            currentQuestionnaire.publish();  // 使用Questionnaire类的方法
        } else {
            currentQuestionnaire.unpublish();  // 使用Questionnaire类的方法
        }

        // 更新问题列表
        currentQuestionnaire.setQuestions(questions);

        try {
            // 保存到数据库
            if (currentQuestionnaire.getId() == null) {
                // 新增问卷 - 设置创建时间
                currentQuestionnaire = Questionnaire.builder()
                        .uid(currentQuestionnaire.getUid())
                        .title(currentQuestionnaire.getTitle())
                        .description(currentQuestionnaire.getDescription())
                        .station(currentQuestionnaire.getStation())
                        .questions(currentQuestionnaire.getQuestions())
                        .build();
                questionnaireService.createQuestionnaire(currentQuestionnaire);
            } else {
                // 更新现有问卷
                questionnaireService.updateQuestionnaire(currentQuestionnaire);
            }

            showAlert("成功", "问卷保存成功，状态：" + currentQuestionnaire.getStatusDisplay(),
                    Alert.AlertType.INFORMATION);

            // 触发保存回调
            if (saveListener != null) {
                saveListener.onSaveSuccess(currentQuestionnaire);
            }

            closeWindow();
        } catch (Exception e) {
            showAlert("错误", "保存失败: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    // 显示提示框
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // 关闭窗口
    @FXML
    private void closeWindow() {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    // 设置保存回调
    public void setOnSaveListener(OnSaveListener listener) {
        this.saveListener = listener;
    }

    // 保存回调接口
    public interface OnSaveListener {
        void onSaveSuccess(Questionnaire questionnaire);
    }
}
