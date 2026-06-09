import java.sql.*;
public class TestMore {
    public static void main(String[] args) throws Exception {
        Class.forName("org.opengauss.Driver");
        Connection conn = DriverManager.getConnection("jdbc:opengauss://192.168.56.101:26000/survey_db","xiaohao","Xh.202366");
        Statement stmt = conn.createStatement();
        System.out.println("=== role columns ===");
        ResultSet rs = conn.getMetaData().getColumns(null, null, "role", null);
        while (rs.next()) System.out.println("  " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
        System.out.println("=== user_role columns ===");
        rs = conn.getMetaData().getColumns(null, null, "user_role", null);
        while (rs.next()) System.out.println("  " + rs.getString("COLUMN_NAME") + " (" + rs.getString("TYPE_NAME") + ")");
        System.out.println("=== questionnaire data ===");
        rs = stmt.executeQuery("SELECT id, title, status, station, create_time FROM questionnaire ORDER BY id");
        while (rs.next()) System.out.println("  ID=" + rs.getLong("id") + " title=" + rs.getString("title") + " status=" + rs.getString("status") + " station=" + rs.getBoolean("station") + " created=" + rs.getTimestamp("create_time"));
        System.out.println("=== questions for questionnaire 1 ===");
        rs = stmt.executeQuery("SELECT qid, qtext, type, options, is_required FROM question WHERE questionnaire_id=1");
        while (rs.next()) System.out.println("  QID=" + rs.getLong("qid") + " text=" + rs.getString("qtext") + " type=" + rs.getString("type") + " is_req=" + rs.getBoolean("is_required") + " options=" + rs.getString("options"));
        conn.close();
    }
}
