package SurveySystem;

import Util.DBUtil;
import java.sql.*;

public class DBTest {
    public static void main(String[] args) throws Exception {
        Connection conn = DBUtil.getConnection();
        System.out.println("Connected successfully!");
        
        // List tables
        DatabaseMetaData meta = conn.getMetaData();
        ResultSet rs = meta.getTables(null, null, "%", new String[]{"TABLE"});
        System.out.println("Tables:");
        while (rs.next()) {
            System.out.println("  - " + rs.getString("TABLE_NAME"));
        }
        rs.close();
        
        // Check Questionnaire table
        Statement stmt = conn.createStatement();
        rs = stmt.executeQuery("SELECT COUNT(*) FROM Questionnaire");
        if (rs.next()) System.out.println("Questionnaire count: " + rs.getInt(1));
        rs.close();
        
        // Check User table
        rs = stmt.executeQuery("SELECT COUNT(*) FROM \"User\"");
        if (rs.next()) System.out.println("User count: " + rs.getInt(1));
        rs.close();
        
        stmt.close();
        conn.close();
    }
}
