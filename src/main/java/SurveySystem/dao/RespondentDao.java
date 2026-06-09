package SurveySystem.dao;

import SurveySystem.model.Respondent;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 受访者数据访问层
 * 表：Respondent（rid, age, sex, address, email）
 */
public class RespondentDao {

    /** 新增受访者（返回自增rid） */
    public Long addRespondent(Respondent r) {
        String sql = "INSERT INTO Respondent (age, sex, address, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (r.getAge() != null) stmt.setInt(1, r.getAge());
            else stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, r.getSex());
            stmt.setString(3, r.getAddress());
            stmt.setString(4, r.getEmail());
            int rows = stmt.executeUpdate();
            if (rows > 0) {
                ResultSet keys = stmt.getGeneratedKeys();
                if (keys.next()) return keys.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 根据rid查找 */
    public Respondent findById(Long rid) {
        String sql = "SELECT * FROM Respondent WHERE rid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, rid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToRespondent(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 根据邮箱查找 */
    public Respondent findByEmail(String email) {
        String sql = "SELECT * FROM Respondent WHERE email = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToRespondent(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 查询所有 */
    public List<Respondent> findAll() {
        List<Respondent> list = new ArrayList<>();
        String sql = "SELECT * FROM Respondent ORDER BY rid DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) list.add(mapToRespondent(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** 年龄段统计 */
    public List<Object[]> countByAgeGroup() {
        List<Object[]> results = new ArrayList<>();
        String sql = "SELECT " +
                "CASE WHEN age >= 18 AND age < 25 THEN '18-24岁' " +
                "     WHEN age >= 25 AND age < 35 THEN '25-34岁' " +
                "     WHEN age >= 35 AND age < 45 THEN '35-44岁' " +
                "     WHEN age >= 45 AND age < 55 THEN '45-54岁' " +
                "     WHEN age >= 55 AND age < 65 THEN '55-64岁' " +
                "     WHEN age >= 65 THEN '65岁以上' " +
                "     ELSE '未知' END AS age_group, COUNT(*) AS cnt " +
                "FROM Respondent WHERE age IS NOT NULL " +
                "GROUP BY age_group ORDER BY MIN(age)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) results.add(new Object[]{rs.getString("age_group"), rs.getInt("cnt")});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /** 性别统计 */
    public List<Object[]> countBySex() {
        List<Object[]> results = new ArrayList<>();
        String sql = "SELECT sex, COUNT(*) AS cnt FROM Respondent WHERE sex IS NOT NULL GROUP BY sex";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) results.add(new Object[]{rs.getString("sex"), rs.getInt("cnt")});
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }

    /** 更新受访者 */
    public boolean update(Respondent r) {
        String sql = "UPDATE Respondent SET age = ?, sex = ?, address = ?, email = ? WHERE rid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (r.getAge() != null) stmt.setInt(1, r.getAge());
            else stmt.setNull(1, Types.INTEGER);
            stmt.setString(2, r.getSex());
            stmt.setString(3, r.getAddress());
            stmt.setString(4, r.getEmail());
            stmt.setLong(5, r.getRid());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 删除受访者 */
    public boolean delete(Long rid) {
        String sql = "DELETE FROM Respondent WHERE rid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, rid);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 总数 */
    public int count() {
        String sql = "SELECT COUNT(*) FROM Respondent";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Respondent mapToRespondent(ResultSet rs) throws SQLException {
        Respondent r = new Respondent();
        r.setRid(rs.getLong("rid"));
        int age = rs.getInt("age");
        r.setAge(rs.wasNull() ? null : age);
        r.setSex(rs.getString("sex"));
        r.setAddress(rs.getString("address"));
        r.setEmail(rs.getString("email"));
        return r;
    }
}
