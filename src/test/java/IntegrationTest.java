import SurveySystem.model.*;
import SurveySystem.service.*;
import SurveySystem.dao.*;
import Util.*;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

/**
 * Integration test for core services
 */
public class IntegrationTest {
    static int passed = 0, failed = 0;

    static void assertEquals(Object expected, Object actual, String msg) {
        if (expected == null ? actual == null : expected.equals(actual)) {
            System.out.println("  PASS: " + msg);
            passed++;
        } else {
            System.out.println("  FAIL: " + msg + " (expected=" + expected + ", actual=" + actual + ")");
            failed++;
        }
    }

    static void assertNotNull(Object obj, String msg) {
        if (obj != null) { System.out.println("  PASS: " + msg); passed++; }
        else { System.out.println("  FAIL: " + msg + " (was null)"); failed++; }
    }

    static void assertTrue(boolean cond, String msg) {
        if (cond) { System.out.println("  PASS: " + msg); passed++; }
        else { System.out.println("  FAIL: " + msg); failed++; }
    }

    static void assertFalse(boolean cond, String msg) {
        if (!cond) { System.out.println("  PASS: " + msg); passed++; }
        else { System.out.println("  FAIL: " + msg); failed++; }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== Testing Database Connection ===");
        assertTrue(DBUtil.testConnection(), "DB connection");

        System.out.println("\n=== Testing PasswordUtil ===");
        String hash = PasswordUtil.encryptPassword("test123");
        assertNotNull(hash, "Password hash not null");
        assertTrue(hash.startsWith("$2a$"), "Hash is bcrypt format");
        assertTrue(PasswordUtil.verifyPassword("test123", hash), "Password verify correct");
        assertFalse(PasswordUtil.verifyPassword("wrong", hash), "Password verify wrong");

        // Verify admin password with bcrypt
        assertTrue(PasswordUtil.verifyPassword("123456", "$2a$10$Jptm77A/0lOobOjQWKbTVOSFU2mwj11WbcGL1XwPpKAaeWLkkIZOy"), "Admin password 123456 with bcrypt");

        // Test SHA-256 backward compatibility with known hash computed inline
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        String knownSha256Hex = bytesToHex(md.digest("testpass".getBytes(StandardCharsets.UTF_8)));
        assertTrue(PasswordUtil.verifyPassword("testpass", knownSha256Hex), "SHA-256 hex backward compat");
        String knownSha256Base64 = Base64.getEncoder().encodeToString(md.digest("testpass".getBytes(StandardCharsets.UTF_8)));
        assertTrue(PasswordUtil.verifyPassword("testpass", knownSha256Base64), "SHA-256 Base64 backward compat");

        System.out.println("\n=== Testing AuthService Login ===");
        AuthService auth = AuthService.getInstance();
        User user = auth.authenticate("admin", "123456");
        assertNotNull(user, "Admin login with correct password");
        assertEquals("admin", user != null ? user.getUsername() : null, "Username is admin");

        // Set user01 password to known SHA-256 hash, then test login + bcrypt upgrade
        String testPw = "migrateme";
        String sha256Hash = bytesToHex(MessageDigest.getInstance("SHA-256").digest(testPw.getBytes(StandardCharsets.UTF_8)));
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement("UPDATE \"User\" SET password = ? WHERE username = ?")) {
            stmt.setString(1, sha256Hash);
            stmt.setString(2, "user01");
            stmt.executeUpdate();
            System.out.println("  Set user01 password to known SHA-256 hash");
        }

        assertTrue(auth.login("user01", testPw, false, false), "user01 login with old SHA-256");

        // Now verify user01 password was upgraded to bcrypt
        UserDao userDao = new UserDao();
        User user01 = userDao.findByUsername("user01");
        assertNotNull(user01, "user01 found");
        if (user01 != null) {
            assertTrue(user01.getPassword().startsWith("$2a$"), "user01 password upgraded to bcrypt");
            System.out.println("  user01 pw now starts with: " + user01.getPassword().substring(0, 15) + "...");
        }

        assertFalse(auth.login("admin", "wrong", false, false), "Login wrong password");
        assertFalse(auth.login("nonexistent", "123456", false, false), "Login nonexistent user");

        // Full login flow with login() which sets currentUser
        assertTrue(auth.login("admin", "123456", false, false), "Admin login with correct password");
        assertTrue(auth.isAdmin(), "Current user is admin");
        assertTrue(auth.hasPermission(auth.getCurrentUser().getUid(), "SURVEY_CREATE"), "Admin has SURVEY_CREATE permission");

        System.out.println("\n=== Testing QuestionnaireService ===");
        QuestionnaireService qs = new QuestionnaireService();
        List<Questionnaire> allQ = qs.getAllQuestionnaires();
        assertTrue(allQ.size() > 0, "Has questionnaires (count=" + allQ.size() + ")");

        if (!allQ.isEmpty()) {
            Questionnaire q = allQ.get(0);
            assertNotNull(q.getId(), "Questionnaire has ID");
            assertNotNull(q.getTitle(), "Questionnaire has title");
            assertNotNull(q.getCreateTime(), "Questionnaire has createTime");
            System.out.println("  Q ID=" + q.getId() + " title=" + q.getTitle() + " created=" + q.getCreateTime());
        }

        System.out.println("\n=== Testing Questionnaire With Questions ===");
        if (!allQ.isEmpty()) {
            Questionnaire withQ = qs.getQuestionnaireWithQuestions(allQ.get(0).getId());
            assertNotNull(withQ, "Got questionnaire with questions");
            if (withQ != null) {
                List<Question> questions = withQ.getQuestions();
                assertNotNull(questions, "Questions list not null");
                if (questions != null && !questions.isEmpty()) {
                    System.out.println("  Questions count: " + questions.size());
                    for (Question q : questions) {
                        assertNotNull(q.getQtext(), "Question text not null");
                        assertNotNull(q.getType(), "Question type not null");
                        System.out.println("  QID=" + q.getid() + " type=" + q.getType() + " text=" + q.getQtext());
                    }
                    // Test findQuestionById (fixed bug)
                    Question found = withQ.findQuestionById(questions.get(0).getid());
                    assertNotNull(found, "findQuestionById found question");
                }
            }
        }

        System.out.println("\n=== Testing AnalysisService ===");
        if (!allQ.isEmpty()) {
            Map<String, Object> results = AnalysisService.analyzeQuestionnaire(allQ.get(0).getId().intValue());
            assertNotNull(results, "Analysis results not null");
            Questionnaire resultQ = (Questionnaire) results.get("questionnaire_info");
            assertNotNull(resultQ, "Analysis result has questionnaire info");
            if (resultQ != null) {
                assertEquals(allQ.get(0).getId(), resultQ.getId(), "Analysis questionnaire ID matches");
            }
            System.out.println("  Total responses: " + results.get("total_responses"));
        }

        System.out.println("\n=== Testing ReportExportService ===");
        ReportExportService reportService = new ReportExportService();
        if (!allQ.isEmpty()) {
            String html = reportService.generateHtmlReport(allQ.get(0).getId());
            assertNotNull(html, "HTML report generated");
            assertTrue(html.contains("<!DOCTYPE html>"), "HTML has DOCTYPE");
            System.out.println("  HTML report length: " + html.length() + " chars");
        }
        // Invalid ID
        String errorHtml = reportService.generateHtmlReport(-1L);
        assertTrue(errorHtml.contains("无效的问卷ID") || errorHtml.contains("错误"), "HTML report for invalid ID shows error");

        System.out.println("\n=== Testing PermissionService ===");
        PermissionService ps = new PermissionService();
        List<Role> roles = ps.getUserRoles(user.getUid());
        assertTrue(roles.size() > 0, "Admin has roles");
        if (!roles.isEmpty()) {
            System.out.println("  First role: " + roles.get(0).getRoleName());
        }

        System.out.println("\n=== Testing RespondentService ===");
        RespondentService respondentService = new RespondentService();
        Respondent r = respondentService.register("test@example.com", 30, "MALE", "TestCity");
        assertNotNull(r, "Respondent registration");
        if (r != null) {
            assertNotNull(r.getRid(), "Respondent has ID");
            System.out.println("  Registered respondent ID=" + r.getRid() + " email=" + r.getEmail());
        }

        System.out.println("\n=== Testing ResponseService ===");
        ResponseService responseService = new ResponseService();
        if (!allQ.isEmpty() && r != null) {
            Long responseId = responseService.createResponse(allQ.get(0).getId(), r.getRid());
            assertNotNull(responseId, "Response created");
            if (responseId != null) {
                System.out.println("  Created response ID=" + responseId);
                List<Question> questions = qs.getQuestionnaireWithQuestions(allQ.get(0).getId()).getQuestions();
                if (questions != null && !questions.isEmpty()) {
                    Question firstQ = questions.get(0);
                    boolean saved = responseService.saveAnswer(responseId, firstQ.getid(), "A");
                    assertTrue(saved, "Answer saved for QID=" + firstQ.getid());
                }
                boolean completed = responseService.completeResponse(responseId);
                assertTrue(completed, "Response completed");
            }
        }

        System.out.println("\n=== TEST SUMMARY ===");
        System.out.println("  Passed: " + passed);
        System.out.println("  Failed: " + failed);
        if (failed > 0) System.exit(1);
        else System.out.println("  ALL TESTS PASSED!");
    }

    static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) hexString.append(String.format("%02x", b));
        return hexString.toString();
    }
}
