package SurveySystem.controller;

import SurveySystem.MainAPP;
import SurveySystem.dao.LogDao;
import SurveySystem.model.Log;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ViewLogsController {

    @FXML
    private TableView<Log> logTable;

    @FXML
    private ComboBox<String> operationFilter;

    @FXML
    private DatePicker startDate;

    @FXML
    private DatePicker endDate;

    private LogDao logDao = new LogDao();
    private ObservableList<Log> logList;

    @FXML
    public void initialize() {
        // 设置列
        TableColumn<Log, String> timeCol = new TableColumn<>("时间");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("operationTime"));

        TableColumn<Log, String> userCol = new TableColumn<>("用户");
        userCol.setCellValueFactory(new PropertyValueFactory<>("userName"));

        TableColumn<Log, String> opCol = new TableColumn<>("操作");
        opCol.setCellValueFactory(new PropertyValueFactory<>("operation"));

        TableColumn<Log, String> targetCol = new TableColumn<>("目标");
        targetCol.setCellValueFactory(new PropertyValueFactory<>("target"));

        TableColumn<Log, String> detailCol = new TableColumn<>("详情");
        detailCol.setCellValueFactory(new PropertyValueFactory<>("details"));

        logTable.getColumns().addAll(timeCol, userCol, opCol, targetCol, detailCol);

        // 填充操作类型过滤下拉框
        operationFilter.setItems(FXCollections.observableArrayList(
                "全部", "CREATE_QUESTIONNAIRE", "UPDATE_QUESTIONNAIRE", "DELETE_QUESTIONNAIRE",
                "ADD_QUESTION", "UPDATE_QUESTION", "DELETE_QUESTION",
                "PUBLISH_SURVEY", "SUBMIT_RESPONSE", "LOGIN", "REGISTER"
        ));
        operationFilter.getSelectionModel().selectFirst();

        loadLogs();
    }

    private void loadLogs() {
        List<Log> logs = logDao.findAll();
        logList = FXCollections.observableArrayList(logs);
        logTable.setItems(logList);
    }

    @FXML
    private void handleSearch() {
        String op = operationFilter.getSelectionModel().getSelectedItem();
        LocalDate start = startDate.getValue();
        LocalDate end = endDate.getValue();

        if (start != null && end != null) {
            Timestamp startTs = Timestamp.valueOf(start.atStartOfDay());
            Timestamp endTs = Timestamp.valueOf(end.atTime(LocalTime.MAX));
            List<Log> logs = logDao.findByTimeRange(startTs, endTs);
            logList = FXCollections.observableArrayList(logs);
            logTable.setItems(logList);
            MainAPP.showAlert(Alert.AlertType.INFORMATION, "查询完成", "共找到 " + logs.size() + " 条记录");
        } else if (op != null && !"全部".equals(op)) {
            List<Log> logs = logDao.findByOperation(op);
            logList = FXCollections.observableArrayList(logs);
            logTable.setItems(logList);
        } else {
            loadLogs();
        }
    }

    @FXML
    private void handleRefresh() {
        loadLogs();
        MainAPP.showAlert(Alert.AlertType.INFORMATION, "成功", "日志已刷新");
    }

    @FXML
    private void handleBack() {
        Stage stage = (Stage) logTable.getScene().getWindow();
        stage.close();
    }
}
