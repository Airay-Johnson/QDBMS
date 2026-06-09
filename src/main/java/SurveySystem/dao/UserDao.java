package SurveySystem.dao;

import SurveySystem.model.User;
import Util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDao {

    /** 根据用户名查找 */
    public User findByUsername(String username) {
        String sql = "SELECT * FROM \"User\" WHERE username = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToUser(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 根据ID查找 */
    public User findById(Long uid) {
        String sql = "SELECT * FROM \"User\" WHERE uid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, uid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return mapToUser(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /** 新增用户（返回自增UID） */
    public Long addUser(User user) {
        String sql = "INSERT INTO \"User\" (username, password, email) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, user.getEmail());
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

    /** 更新用户信息 */
    public boolean updateUser(User user) {
        String sql = "UPDATE \"User\" SET password = ?, email = ? WHERE uid = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getPassword());
            stmt.setString(2, user.getEmail());
            stmt.setLong(3, user.getUid());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** 查询所有用户 */
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM \"User\" ORDER BY uid";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) users.add(mapToUser(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    /** 获取用户总数 */
    public int getTotalUserCount() {
        String sql = "SELECT COUNT(*) FROM \"User\"";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private User mapToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setUid(rs.getLong("uid"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        return user;
    }
}
