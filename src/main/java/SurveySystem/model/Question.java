package SurveySystem.model;

import SurveySystem.constant.OptionDataCleaner;
import Util.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Question {
    private Long id;
    private Long QUESTIONNAIRE_ID; // 与数据库列名匹配
    private String qtext;
    private String type;
    private String options; // 存储JSON格式的选项字符串
    private Integer sequence_Number;
    private Boolean is_required;
    private static final Logger logger = LoggerFactory.getLogger(Question.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public Question() {}
    private LongProperty qid = new SimpleLongProperty();
    public List<String> getOptionsList() {
        if (options == null || options.trim().isEmpty()) {
            return Arrays.asList();
        }
        return Arrays.asList(options.split(","));
    }


    // 将List<Map>转换为JSON字符串设置选项
    public void setOptionsFromList(List<Map<String, String>> optionsList) {
        this.options = JsonUtil.toJson(optionsList);
    }

    public Question(Long id, Long questionnaireId, String qtext, String type, String options) {
        this.id = id;
        this.QUESTIONNAIRE_ID = questionnaireId;
        this.qtext = qtext;
        this.type = type;
        this.options = options;
    }

    // Getters and Setters
    public Long getid() {
        return id;
    }

    public void setid(Long qid) {
        this.id = qid;
    }

    public Long getQuestionnaireId() {
        return QUESTIONNAIRE_ID;
    }

    public void setQuestionnaireId(Long questionnaireId) {
        this.QUESTIONNAIRE_ID = questionnaireId;
    }

    public String getQtext() {
        return qtext;
    }

    public void setQtext(String qtext) {
        this.qtext = qtext;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public Integer getSequence_Number() {
        return sequence_Number;
    }

    public void setSequence_Number(Integer sequence_Number) {
        this.sequence_Number = sequence_Number;
    }

    public Boolean getIs_Required() {
        return is_required;
    }

    public void setIs_Required(Boolean is_required) {
        this.is_required = is_required;
    }

    // 在 Question 类中添加这些方法
    public Long getQid() {
        return getid();
    }

    public void setQid(Long qid) {
        setid(qid);
    }

    public LongProperty qidProperty() {
        // 确保 id 和 qid 保持同步
        if (qid == null) {
            qid = new SimpleLongProperty(getid());
            // 添加监听器以保持同步
            qid.addListener((obs, oldVal, newVal) -> setid(newVal.longValue()));
        }
        return qid;
    }

    /**
     * 获取解析后的选项列表
     */
    public List<Option> getParsedOptions() {
        List<Option> options = new ArrayList<>();

        if (this.options == null || this.options.trim().isEmpty()) {
            return options;
        }

        // 清理选项数据
        String cleanedOptions = OptionDataCleaner.cleanOptionData(this.options);

        try {
            // 尝试解析为 JSON 数组
            if (OptionDataCleaner.isValidJson(cleanedOptions)) {
                Option[] optionArray = objectMapper.readValue(cleanedOptions, Option[].class);
                options = Arrays.asList(optionArray);
                logger.info("成功解析JSON选项: {}");
            } else {
                throw new Exception("不是有效的JSON格式");
            }
        } catch (Exception e) {
            logger.warn("无法解析选项为 JSON，尝试使用竖线分割: {}");

            // 如果 JSON 解析失败，回退到竖线分割
            String[] optionTexts = cleanedOptions.split("\\|");
            for (int i = 0; i < optionTexts.length; i++) {
                String text = optionTexts[i].trim();
                if (!text.isEmpty()) {
                    // 生成选项ID，如A, B, C...
                    String id = String.valueOf((char) ('A' + i));
                    options.add(new Option(id, text));
                }
            }
        }

        return options;
    }


    @Override
    public String toString() {
        return "Question{" +
                "qid=" + id +
                ", QUESTIONNAIRE_ID=" + QUESTIONNAIRE_ID +
                ", qtext='" + qtext + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}