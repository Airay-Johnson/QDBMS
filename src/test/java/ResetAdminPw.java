import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;
public class ResetAdminPw {
    public static void main(String[] args) throws Exception {
        Class.forName("org.opengauss.Driver");
        Connection conn = DriverManager.getConnection("jdbc:opengauss://192.168.56.101:26000/survey_db","xiaohao","Xh.202366");
        String hash = BCrypt.hashpw("123456", BCrypt.gensalt());
        System.out.println("New bcrypt hash for 123456: " + hash);
        PreparedStatement stmt = conn.prepareStatement("UPDATE \"User\" SET password = ? WHERE username = ?");
        stmt.setString(1, hash);
        stmt.setString(2, "admin");
        int rows = stmt.executeUpdate();
        System.out.println("Updated admin password: " + rows + " rows affected");
        stmt.close();
        conn.close();
    }
}
