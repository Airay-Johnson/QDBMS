package SurveySystem.dao;

import SurveySystem.model.Log;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志数据访问层（LogDao）
 * 对应数据库表：log（LID, USER_ID, OPERATION, TARGET, DETAILS, OPERATION_TIME, IP_ADDRESS）
 */
public class LogDao {

    /**
     * 新增操作日志
     */
    public void addLog(Log log) {
        String sql = "INSERT INTO log(uid, operation, target, op_time) VALUES(?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, log.getUid());
            stmt.setString(2, log.getOperation());
            stmt.setString(3, log.getTarget());
            stmt.setTimestamp(4, log.getOpTime());
            stmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询所有日志（按时间倒序，最多500条）
     */
    public List<Log> findAll() {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                "LEFT JOIN \"User\" u ON l.uid = u.uid " +
                "ORDER BY l.op_time DESC LIMIT 500";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                logs.add(mapToLog(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * 查询指定时间范围内的日志
     */
    public List<Log> findByTimeRange(Timestamp start, Timestamp end) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                "LEFT JOIN \"User\" u ON l.uid = u.uid " +
                "WHERE l.op_time BETWEEN ? AND ? " +
                "ORDER BY l.op_time DESC LIMIT 500";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setTimestamp(1, start);
            stmt.setTimestamp(2, end);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapToLog(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * 根据操作类型过滤日志
     */
    public List<Log> findByOperation(String operation) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                "LEFT JOIN \"User\" u ON l.uid = u.uid " +
                "WHERE l.operation = ? " +
                "ORDER BY l.op_time DESC LIMIT 500";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, operation);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(mapToLog(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    /**
     * 根据用户ID查询操作记录
     */
    public List<Log> findByUserId(Long uid) {
        List<Log> logs = new ArrayList<>();
        String sql = "SELECT l.*, u.username FROM log l " +
                "LEFT JOIN \"User\" u ON l.uid = u.uid " +
                "WHERE l.uid = ? ORDER BY l.op_time DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, uid);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                logs.add(mapToLog(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return logs;
    }

    private Log mapToLog(ResultSet rs) throws SQLException {
        Log log = new Log();
        log.setLogId(rs.getLong("log_id"));
        log.setUid(rs.getLong("uid"));
        log.setOperation(rs.getString("operation"));
        log.setTarget(rs.getString("target"));
        log.setOpTime(rs.getTimestamp("op_time"));
        String username = rs.getString("username");
        log.setUserName(username);
        log.setDetails((username != null ? username : "未知用户") + " 执行了 " + rs.getString("operation"));
        return log;
    }
}
