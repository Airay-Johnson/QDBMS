package com.qdbms.service;

import com.qdbms.entity.Response;

import java.util.List;
import java.util.Map;

public interface ResponseService {
    Response submit(Long questionnaireId, List<Map<String, Object>> answers, String ipAddress);
    List<Response> listByQuestionnaire(Long questionnaireId);
}
