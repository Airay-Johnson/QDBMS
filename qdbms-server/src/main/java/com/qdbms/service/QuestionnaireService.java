package com.qdbms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdbms.dto.QuestionnaireRequest;
import com.qdbms.entity.Questionnaire;

import java.util.List;

public interface QuestionnaireService {
    Questionnaire create(QuestionnaireRequest request, Long creatorId);
    IPage<Questionnaire> listByCreator(Long creatorId, int page, int size);
    List<Questionnaire> listAllByCreator(Long creatorId);
    Questionnaire getById(Long id);
    Questionnaire update(Long id, QuestionnaireRequest request);
    void delete(Long id, Long currentUserId);
    void publish(Long id);
    void close(Long id);
}
