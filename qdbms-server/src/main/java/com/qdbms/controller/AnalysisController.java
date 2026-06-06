package com.qdbms.controller;

import com.qdbms.common.Result;
import com.qdbms.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    @GetMapping("/questionnaire/{id}")
    public Result<Map<String, Object>> getStats(@PathVariable Long id) {
        return Result.ok(analysisService.getQuestionnaireStats(id));
    }

    @GetMapping("/questions/{id}/single")
    public Result<List<Map<String, Object>>> singleChoiceStats(@PathVariable Long id) {
        return Result.ok(analysisService.getSingleChoiceStats(id));
    }

    @GetMapping("/questions/{id}/multiple")
    public Result<List<Map<String, Object>>> multipleChoiceStats(@PathVariable Long id) {
        return Result.ok(analysisService.getMultipleChoiceStats(id));
    }

    @GetMapping("/questions/{id}/text")
    public Result<List<String>> textAnswers(@PathVariable Long id) {
        return Result.ok(analysisService.getTextAnswers(id));
    }

    @GetMapping("/questionnaire/{id}/answers")
    public Result<List<Map<String, Object>>> allAnswers(@PathVariable Long id) {
        return Result.ok(analysisService.getAllAnswers(id));
    }
}
