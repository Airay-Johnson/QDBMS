package SurveySystem.service;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.common.Term;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class TextAnalysisService {
    public static List<String> extractKeywords(String text, int count) {
        return HanLP.extractKeyword(text, count);
    }

    public static Map<String, Integer> wordFrequency(String text) {
        List<Term> termList = HanLP.segment(text);
        Map<String, Integer> frequencyMap = new HashMap<>();

        for (Term term : termList) {
            if (term.word.length() > 1) { // 过滤单字
                frequencyMap.put(term.word, frequencyMap.getOrDefault(term.word, 0) + 1);
            }
        }

        return frequencyMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }
}