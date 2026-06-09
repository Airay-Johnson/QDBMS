package com.qdbms.service;

import java.util.List;
import java.util.Map;

public interface AnalysisService {

    /**
     * 获取问卷基础统计（总答卷数、各题统计）
     */
    Map<String, Object> getQuestionnaireStats(Long questionnaireId);

    /**
     * 获取单选题选项分布
     */
    List<Map<String, Object>> getSingleChoiceStats(Long questionId);

    /**
     * 获取多选题选项分布
     */
    List<Map<String, Object>> getMultipleChoiceStats(Long questionId);

    /**
     * 获取文本题答案列表
     */
    List<String> getTextAnswers(Long questionId);

    /**
     * 获取问卷所有答案明细
     */
    List<Map<String, Object>> getAllAnswers(Long questionnaireId);
}
