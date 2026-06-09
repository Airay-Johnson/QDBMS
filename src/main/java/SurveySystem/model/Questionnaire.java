package SurveySystem.model;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Questionnaire {
    private Long id;
    private Long uid;
    private String title;
    private String description;
    private Boolean station;
    private Timestamp createTime;
    private List<Question> questions;

    public Questionnaire() {
        this.station = false;
        this.questions = new ArrayList<>();
    }

    // 获取状态的显示文本
    public String getStatusDisplay() {
        return station ? "已发布" : "未发布";
    }

    public Questionnaire(Long id, Long uid, String title, String description,
                         Boolean station) {
        this.id = id;
        this.uid = uid;
        this.title = title;
        this.description = description;
        this.station = station;
        this.questions = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUid() {
        return uid;
    }

    public void setUid(Long uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getStation() {
        return station;
    }

    public void setStation(Boolean station) {
        this.station = station;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public void setQuestions(List<Question> questions) {
        this.questions = questions;
    }

    public void addQuestion(Question question) {
        if (questions == null) {
            questions = new ArrayList<>();
        }
        questions.add(question);
    }

    public boolean removeQuestion(Question question) {
        return questions != null && questions.remove(question);
    }

    public Question findQuestionById(Long qid) {
        if (questions == null) {
            return null;
        }

        for (Question question : questions) {
            if (question.getid().equals(qid)) {
                return question;
            }
        }
        return null;
    }

    public List<Question> getSingleChoiceQuestions() {
        List<Question> singleChoiceQuestions = new ArrayList<>();
        if (questions != null) {
            for (Question question : questions) {
                if ("singleChoice".equals(question.getType())) {
                    singleChoiceQuestions.add(question);
                }
            }
        }
        return singleChoiceQuestions;
    }

    public List<Question> getMultipleChoiceQuestions() {
        List<Question> multipleChoiceQuestions = new ArrayList<>();
        if (questions != null) {
            for (Question question : questions) {
                if ("multiple_choices".equals(question.getType())) {
                    multipleChoiceQuestions.add(question);
                }
            }
        }
        return multipleChoiceQuestions;
    }

    public List<Question> getTextQuestions() {
        List<Question> textQuestions = new ArrayList<>();
        if (questions != null) {
            for (Question question : questions) {
                if ("text".equals(question.getType())) {
                    textQuestions.add(question);
                }
            }
        }
        return textQuestions;
    }

    public int getQuestionCount() {
        return questions != null ? questions.size() : 0;
    }

    public boolean isPublished() {
        return Boolean.TRUE.equals(station);
    }

    public void publish() {
        this.station = true;
    }

    public void unpublish() {
        this.station = false;
    }

    @Override
    public String toString() {
        return "Questionnaire{" +
                "id=" + id +
                ", uid=" + uid +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", station=" + station +
                ", createTime=" + createTime +
                ", questionCount=" + getQuestionCount() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Questionnaire that = (Questionnaire) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long id;
        private Long uid;
        private String title;
        private String description;
        private Boolean station;
        private Timestamp createTime;
        private List<Question> questions;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder uid(Long uid) {
            this.uid = uid;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }


        public Builder station(Boolean station) {
            this.station = station;
            return this;
        }

        public Builder createTime(Timestamp createTime) {
            this.createTime = createTime;
            return this;
        }

        public Builder questions(List<Question> questions) {
            this.questions = questions;
            return this;
        }

        public Questionnaire build() {
            Questionnaire questionnaire = new Questionnaire();
            questionnaire.setId(id);
            questionnaire.setUid(uid);
            questionnaire.setTitle(title);
            questionnaire.setDescription(description);
            questionnaire.setStation(station);
            questionnaire.setCreateTime(createTime);
            questionnaire.setQuestions(questions);
            return questionnaire;
        }
    }
}