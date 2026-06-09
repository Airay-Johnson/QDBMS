package com.qdbms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qdbms.dto.QuestionnaireRequest;
import com.qdbms.entity.Questionnaire;
import com.qdbms.mapper.QuestionnaireMapper;
import com.qdbms.service.QuestionnaireService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class QuestionnaireServiceImpl implements QuestionnaireService {

    private final QuestionnaireMapper questionnaireMapper;

    @Override
    @Transactional
    public Questionnaire create(QuestionnaireRequest request, Long creatorId) {
        Questionnaire q = new Questionnaire();
        q.setTitle(request.getTitle());
        q.setDescription(request.getDescription());
        q.setStatus(0);
        q.setCreatorId(creatorId);
        questionnaireMapper.insert(q);
        return q;
    }

    @Override
    public IPage<Questionnaire> listByCreator(Long creatorId, int page, int size) {
        LambdaQueryWrapper<Questionnaire> wrapper = new LambdaQueryWrapper<Questionnaire>()
                .eq(Questionnaire::getCreatorId, creatorId)
                .orderByDesc(Questionnaire::getCreatedAt);
        return questionnaireMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public List<Questionnaire> listAllByCreator(Long creatorId) {
        return questionnaireMapper.findByCreatorId(creatorId);
    }

    @Override
    public Questionnaire getById(Long id) {
        Questionnaire q = questionnaireMapper.selectById(id);
        if (q == null) {
            throw new IllegalArgumentException("问卷不存在");
        }
        return q;
    }

    @Override
    @Transactional
    public Questionnaire update(Long id, QuestionnaireRequest request) {
        Questionnaire q = getById(id);
        q.setTitle(request.getTitle());
        q.setDescription(request.getDescription());
        questionnaireMapper.updateById(q);
        return q;
    }

    @Override
    @Transactional
    public void delete(Long id, Long currentUserId) {
        Questionnaire q = getById(id);
        if (!q.getCreatorId().equals(currentUserId)) {
            throw new IllegalArgumentException("无权删除此问卷");
        }
        questionnaireMapper.deleteById(id);
    }

    @Override
    @Transactional
    public void publish(Long id) {
        Questionnaire q = getById(id);
        q.setStatus(1);
        q.setPublishedAt(LocalDateTime.now());
        questionnaireMapper.updateById(q);
    }

    @Override
    @Transactional
    public void close(Long id) {
        Questionnaire q = getById(id);
        q.setStatus(2);
        q.setClosedAt(LocalDateTime.now());
        questionnaireMapper.updateById(q);
    }
}
