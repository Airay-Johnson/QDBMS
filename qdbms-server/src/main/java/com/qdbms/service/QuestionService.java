package com.qdbms.service;

import com.qdbms.dto.QuestionRequest;
import com.qdbms.entity.Question;

import java.util.List;

public interface QuestionService {
    Question create(QuestionRequest request, Long questionnaireId);
    List<Question> listByQuestionnaire(Long questionnaireId);
    Question update(Long id, QuestionRequest request);
    void delete(Long id);
    void sort(List<Long> questionIds);
}
