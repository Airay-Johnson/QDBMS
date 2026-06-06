package com.qdbms.service.impl;

import com.qdbms.dto.QuestionRequest;
import com.qdbms.entity.Question;
import com.qdbms.mapper.QuestionMapper;
import com.qdbms.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    @Override
    @Transactional
    public Question create(QuestionRequest request, Long questionnaireId) {
        Question q = new Question();
        q.setQuestionnaireId(questionnaireId);
        q.setQuestionText(request.getQuestionText());
        q.setType(request.getType());
        q.setOptions(request.getOptions());
        q.setIsRequired(request.getIsRequired());
        int maxSeq = questionMapper.maxSequenceNumber(questionnaireId);
        q.setSequenceNumber(request.getSequenceNumber() != null
                ? request.getSequenceNumber() : maxSeq + 1);
        questionMapper.insert(q);
        return q;
    }

    @Override
    public List<Question> listByQuestionnaire(Long questionnaireId) {
        return questionMapper.findByQuestionnaireId(questionnaireId);
    }

    @Override
    @Transactional
    public Question update(Long id, QuestionRequest request) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        q.setQuestionText(request.getQuestionText());
        q.setType(request.getType());
        q.setOptions(request.getOptions());
        q.setIsRequired(request.getIsRequired());
        if (request.getSequenceNumber() != null) {
            q.setSequenceNumber(request.getSequenceNumber());
        }
        questionMapper.updateById(q);
        return q;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) {
            throw new IllegalArgumentException("问题不存在");
        }
        questionMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void sort(List<Long> questionIds) {
        for (int i = 0; i < questionIds.size(); i++) {
            Question q = questionMapper.selectById(questionIds.get(i));
            if (q != null) {
                q.setSequenceNumber(i + 1);
                questionMapper.updateById(q);
            }
        }
    }
}
