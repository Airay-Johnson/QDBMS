package com.qdbms.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.qdbms.entity.Respondent;

import java.io.InputStream;
import java.util.List;

public interface RespondentService {
    IPage<Respondent> listByQuestionnaire(Long questionnaireId, int page, int size);
    Respondent getById(Long id);
    Respondent create(Respondent respondent);
    Respondent update(Long id, Respondent respondent);
    void delete(Long id);
    int importFromExcel(Long questionnaireId, InputStream inputStream);
    List<Respondent> listByGroup(Long questionnaireId, String groupName);
}
