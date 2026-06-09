package SurveySystem.constant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.itextpdf.text.log.Logger;
import com.itextpdf.text.log.LoggerFactory;

public class OptionDataCleaner {
    public static final Logger logger = LoggerFactory.getLogger(OptionDataCleaner.class);

    /**
     * 清理和规范化选项数据
     */
    public static String cleanOptionData(String rawData) {
        if (rawData == null || rawData.trim().isEmpty()) {
            return rawData;
        }

        String cleaned = rawData.trim();

        // 尝试修复常见的格式问题
        cleaned = cleaned
                .replace("'", "\"")  // 将单引号替换为双引号
                .replace("\\\"", "\"")  // 移除不必要的转义
                .replace("}, {", "},{")  // 移除多余的空格
                .replace("} , {", "},{") // 移除多余的空格
                .replace("\" \"", "\",\"") // 修复缺少逗号的问题
                .replace("id:", "\"id\":") // 修复缺少引号的键
                .replace("text:", "\"text\":"); // 修复缺少引号的键

        // 确保是有效的JSON数组格式
        if (!cleaned.startsWith("[") && cleaned.contains("{")) {
            cleaned = "[" + cleaned + "]";
        }

        logger.info("清理选项数据: {} -> {}");
        return cleaned;
    }

    /**
     * 验证选项数据是否是有效的JSON格式
     */
    public static boolean isValidJson(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }

        String trimmed = data.trim();
        try {
            if (trimmed.startsWith("[")) {
                new ObjectMapper().readTree(trimmed);
                return true;
            } else if (trimmed.startsWith("{")) {
                // 尝试将其包装为数组
                new ObjectMapper().readTree("[" + trimmed + "]");
                return true;
            }
        } catch (Exception e) {
            // 不是有效的JSON
        }

        return false;
    }
}
