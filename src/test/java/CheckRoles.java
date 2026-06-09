import java.sql.*;
public class CheckRoles {
    public static void main(String[] args) throws Exception {
        Class.forName("org.opengauss.Driver");
        Connection conn = DriverManager.getConnection("jdbc:opengauss://192.168.56.101:26000/survey_db","xiaohao","Xh.202366");
        Statement stmt = conn.createStatement();
        // Role data
        ResultSet rs = stmt.executeQuery("SELECT * FROM role");
        System.out.println("=== Roles ===");
        while (rs.next()) System.out.println("  roid=" + rs.getLong("roid") + " name=" + rs.getString("name") + " desc=" + rs.getString("description"));
        // User_Role
        rs = stmt.executeQuery("SELECT * FROM user_role");
        System.out.println("=== User_Role ===");
        while (rs.next()) System.out.println("  user_id=" + rs.getLong("user_id") + " role_id=" + rs.getLong("role_id"));
        // Test bcrypt verify
        System.out.println("=== Testing password ===");
        rs = stmt.executeQuery("SELECT password FROM \"User\" WHERE username='admin'");
        rs.next();
        String hash = rs.getString("password");
        System.out.println("Admin hash starts with: " + hash.substring(0, 10) + "...");
        System.out.println("Is bcrypt: " + hash.startsWith("$2a$"));
        // Test questions
        rs = stmt.executeQuery("SELECT qid, type FROM question WHERE questionnaire_id=1");
        while (rs.next()) System.out.println("  QID=" + rs.getLong("qid") + " type=" + rs.getString("type"));
        // Test response/answer counts
        rs = stmt.executeQuery("SELECT COUNT(*) FROM response");
        rs.next();
        System.out.println("Total responses: " + rs.getInt(1));
        rs = stmt.executeQuery("SELECT COUNT(*) FROM answer");
        rs.next();
        System.out.println("Total answers: " + rs.getInt(1));
        conn.close();
    }
}
