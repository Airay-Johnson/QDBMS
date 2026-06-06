package SurveySystem.model;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ResponseData {
    private final SimpleIntegerProperty responseId;
    private final SimpleStringProperty respondentName;
    private final SimpleStringProperty surveyName;
    private final SimpleStringProperty submitTime;

    // 日期时间格式化器
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ResponseData(int responseId, String respondentName, String surveyName, LocalDateTime submitTime) {
        this.responseId = new SimpleIntegerProperty(responseId);
        this.respondentName = new SimpleStringProperty(respondentName);
        this.surveyName = new SimpleStringProperty(surveyName);
        this.submitTime = new SimpleStringProperty(submitTime.format(formatter));
    }

    // responseId的getter和setter
    public int getResponseId() {
        return responseId.get();
    }

    public void setResponseId(int responseId) {
        this.responseId.set(responseId);
    }

    public SimpleIntegerProperty responseIdProperty() {
        return responseId;
    }

    // respondentName的getter和setter
    public String getRespondentName() {
        return respondentName.get();
    }

    public void setRespondentName(String respondentName) {
        this.respondentName.set(respondentName);
    }

    public SimpleStringProperty respondentNameProperty() {
        return respondentName;
    }

    // surveyName的getter和setter
    public String getSurveyName() {
        return surveyName.get();
    }

    public void setSurveyName(String surveyName) {
        this.surveyName.set(surveyName);
    }

    public SimpleStringProperty surveyNameProperty() {
        return surveyName;
    }

    // submitTime的getter和setter
    public String getSubmitTime() {
        return submitTime.get();
    }

    public void setSubmitTime(LocalDateTime submitTime) {
        this.submitTime.set(submitTime.format(formatter));
    }

    public SimpleStringProperty submitTimeProperty() {
        return submitTime;
    }
}
