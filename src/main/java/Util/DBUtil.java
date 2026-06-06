package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // 请根据您的实际数据库配置修改这些值
    private static final String URL = "jdbc:opengauss://192.168.56.101:26000/survey_db";
    private static final String USER = "xiaohao";
    private static final String PASSWORD = "Xh.202366";

    static {
        try {
            // 加载数据库驱动
            Class.forName("org.opengauss.Driver");
            System.out.println("OpenGauss JDBC驱动加载成功");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load OpenGauss JDBC driver", e);
        }
    }

    /**
     * 获取数据库连接
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("数据库连接成功建立");
        return conn;
    }

    /**
     * 测试数据库连接
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 关闭数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                System.out.println("数据库连接已关闭");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}