package SurveySystem.service;

import SurveySystem.dao.QuestionDao;
import SurveySystem.model.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class QuestionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 获取问卷下所有问题
     */
    public List<Question> getQuestionsByQuestionnaireId(Long questionnaireId) {
        return QuestionDao.getQuestionsByQuestionnaireId(questionnaireId);
    }

    /**
     * 按类型获取问题
     */
    public List<Question> getQuestionsByQuestionnaireIdAndType(Long questionnaireId, String type) {
        return QuestionDao.getQuestionsByQuestionnaireIdAndType(questionnaireId, type);
    }

    /**
     * 添加问题（自动计算序号）
     */
    public boolean addQuestion(Question question) {
        if (question.getSequence_Number() <= 0) {
            List<Question> existing = getQuestionsByQuestionnaireId(question.getQuestionnaireId());
            int maxSeq = existing.stream()
                    .mapToInt(q -> q.getSequence_Number() > 0 ? q.getSequence_Number() : 0)
                    .max().orElse(0);
            question.setSequence_Number(maxSeq + 1);
        }
        return QuestionDao.addQuestion(question);
    }

    /**
     * 更新问题
     */
    public boolean updateQuestion(Question question) {
        return QuestionDao.updateQuestion(question);
    }

    /**
     * 删除问题
     */
    public boolean deleteQuestion(Long qid) {
        return QuestionDao.deleteQuestion(qid);
    }

    /**
     * 解析选项 JSON 为列表
     */
    public List<OptionEntry> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.trim().isEmpty()) return List.of();
        try {
            return objectMapper.readValue(optionsJson, new TypeReference<List<OptionEntry>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }

    public static class OptionEntry {
        private String id;
        private String text;

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getText() { return text; }
        public void setText(String text) { this.text = text; }
    }
}
