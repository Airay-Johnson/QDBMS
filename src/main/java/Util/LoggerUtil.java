package Util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LoggerUtil {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void log(String operation, String details) {
        String timestamp = LocalDateTime.now().format(formatter);
        String logMessage = String.format("[%s] %s: %s", timestamp, operation, details);
        System.out.println(logMessage);

        // 在实际应用中，您可能还想将日志写入文件或数据库
        // writeToFile(logMessage);
    }

    public static void info(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[INFO][" + timestamp + "] " + message);
    }

    public static void error(String message, Exception e) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.err.println("[ERROR][" + timestamp + "] " + message);
        if (e != null) {
            e.printStackTrace();
        }
    }

    public static void debug(String message) {
        String timestamp = LocalDateTime.now().format(formatter);
        System.out.println("[DEBUG][" + timestamp + "] " + message);
    }
}