import java.sql.*;
public class CheckPw {
    public static void main(String[] args) throws Exception {
        Class.forName("org.opengauss.Driver");
        Connection conn = DriverManager.getConnection("jdbc:opengauss://192.168.56.101:26000/survey_db","xiaohao","Xh.202366");
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT uid, username, password, length(password) as pw_len FROM \"User\"");
        while (rs.next()) {
            String pw = rs.getString("password");
            int len = rs.getInt("pw_len");
            System.out.println("UID=" + rs.getLong("uid") + " user=" + rs.getString("username") + " len=" + len);
            System.out.println("  hash=[" + pw + "]");
            System.out.println("  isBcrypt=" + (pw != null && pw.startsWith("$2a$")));
            if (pw != null && pw.startsWith("$2a$")) System.out.println("  validLength=" + (pw.length() == 60));
        }
        conn.close();
    }
}
