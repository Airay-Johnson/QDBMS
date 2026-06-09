import java.sql.*;

public class CheckDB {
    public static void main(String[] args) throws Exception {
        Class.forName("org.opengauss.Driver");
        Connection conn = DriverManager.getConnection("jdbc:opengauss://192.168.56.101:26000/survey_db", "xiaohao", "Xh.202366");
        String[][] tables = {{"questionnaire","questionnaire columns"},{"question","question columns"},{"answer","answer"},{"User","User"},{"response","response"},{"respondent","respondent"}};
        for (String[] t : tables) {
            System.out.println("=== " + t[1] + " ===");
            ResultSet rs = conn.createStatement().executeQuery("SELECT column_name, data_type FROM information_schema.columns WHERE table_name='" + t[0] + "' ORDER BY ordinal_position");
            while (rs.next()) System.out.println("  " + rs.getString(1) + " (" + rs.getString(2) + ")");
            rs.close();
        }
        // Check create_time column specifically
        System.out.println("\n=== Checking questionnaire data sample ===");
        ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM questionnaire LIMIT 3");
        ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) System.out.println("  col " + i + ": " + meta.getColumnName(i) + " (" + meta.getColumnTypeName(i) + ")");
        while (rs.next()) {
            for (int i = 1; i <= meta.getColumnCount(); i++) {
                Object val = rs.getObject(i);
                System.out.println("  " + meta.getColumnName(i) + " = " + (val != null ? val.toString().substring(0, Math.min(val.toString().length(), 50)) : "NULL"));
            }
        }
        rs.close();
        conn.close();
    }
}
