import java.sql.*;
public class CheckUsers {
    public static void main(String[] args) throws Exception {
        Class.forName("org.opengauss.Driver");
        Connection conn = DriverManager.getConnection("jdbc:opengauss://192.168.56.101:26000/survey_db","xiaohao","Xh.202366");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT uid, username, password, email FROM \"User\"");
        while (rs.next()) {
            String pw = rs.getString("password");
            System.out.println("UID=" + rs.getLong("uid") + " | " + rs.getString("username") + " | " + rs.getString("email") + " | pw_hash=" + (pw != null ? pw.substring(0, Math.min(pw.length(), 30)) : "NULL") + "...");
        }
        rs.close();
        rs = stmt.executeQuery("SELECT * FROM role");
        System.out.println("=== Roles ===");
        while (rs.next()) System.out.println("  " + rs.getLong("rid") + " | " + rs.getString("role_name"));
        rs.close();
        rs = stmt.executeQuery("SELECT * FROM user_role");
        System.out.println("=== User-Role ===");
        while (rs.next()) System.out.println("  " + rs.getLong("uid") + " -> role_id=" + rs.getLong("role_id"));
        rs.close();
        conn.close();
    }
}
