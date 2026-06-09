package com.qdbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qdbms.entity.Answer;
import com.qdbms.entity.Response;
import com.qdbms.mapper.AnswerMapper;
import com.qdbms.mapper.ResponseMapper;
import com.qdbms.service.ResponseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ResponseServiceImpl implements ResponseService {

    private final ResponseMapper responseMapper;
    private final AnswerMapper answerMapper;

    @Override
    @Transactional
    public Response submit(Long questionnaireId, List<Map<String, Object>> answers, String ipAddress) {
        Response response = new Response();
        response.setQuestionnaireId(questionnaireId);
        response.setIpAddress(ipAddress);
        responseMapper.insert(response);

        for (Map<String, Object> ans : answers) {
            Answer answer = new Answer();
            answer.setResponseId(response.getId());
            answer.setQuestionId(Long.valueOf(ans.get("questionId").toString()));
            answer.setAnswerText((String) ans.getOrDefault("answerText", null));
            answer.setAnswerOptions((String) ans.getOrDefault("answerOptions", null));
            answerMapper.insert(answer);
        }

        return response;
    }

    @Override
    public List<Response> listByQuestionnaire(Long questionnaireId) {
        return responseMapper.selectList(
                new LambdaQueryWrapper<Response>()
                        .eq(Response::getQuestionnaireId, questionnaireId)
                        .orderByDesc(Response::getSubmittedAt));
    }
}
