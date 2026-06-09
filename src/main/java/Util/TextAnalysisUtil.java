package Util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文本分析工具类（用于问卷文本答案的分词和词频统计）
 */
public class TextAnalysisUtil {
    // 中文停用词（基础版）
    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
            "的", "是", "在", "了", "和", "就", "都", "一个", "也", "有", "到", "更"
    ));
    // 中文分词正则（匹配汉字）
    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\u4e00-\u9fa5]");

    /**
     * 分析文本并返回词频统计（按频率降序）
     */
    public static Map<String, Integer> analyze(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyMap();
        }

        // 1. 清洗文本（保留中文、字母、数字）
        String cleanedText = text.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", " ");

        // 2. 简单分词（按空格拆分）
        String[] words = cleanedText.split("\\s+");

        // 3. 过滤无效词和停用词
        Map<String, Integer> freqMap = new HashMap<>();
        for (String word : words) {
            word = word.trim();
            if (isValidWord(word)) {
                freqMap.put(word, freqMap.getOrDefault(word, 0) + 1);
            }
        }

        // 4. 按词频降序排序
        return freqMap.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    /**
     * 提取_topN_个关键词
     */
    public static List<String> extractKeywords(String text, int topN) {
        Map<String, Integer> freqMap = analyze(text);
        return freqMap.keySet().stream()
                .limit(topN)
                .collect(Collectors.toList());
    }

    /**
     * 验证词有效性（长度>1且非停用词）
     */
    private static boolean isValidWord(String word) {
        return word.length() > 1 && !STOP_WORDS.contains(word);
    }
}