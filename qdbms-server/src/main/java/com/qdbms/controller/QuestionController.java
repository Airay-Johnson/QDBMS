package com.qdbms.controller;

import com.qdbms.common.Result;
import com.qdbms.dto.QuestionRequest;
import com.qdbms.entity.Question;
import com.qdbms.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public Result<Question> create(@Valid @RequestBody QuestionRequest request,
                                    @RequestParam Long questionnaireId) {
        return Result.ok(questionService.create(request, questionnaireId));
    }

    @GetMapping
    public Result<List<Question>> listByQuestionnaire(@RequestParam Long questionnaireId) {
        return Result.ok(questionService.listByQuestionnaire(questionnaireId));
    }

    @PutMapping("/{id}")
    public Result<Question> update(@PathVariable Long id,
                                    @Valid @RequestBody QuestionRequest request) {
        return Result.ok(questionService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        questionService.delete(id);
        return Result.ok("删除成功", null);
    }

    @PutMapping("/sort")
    public Result<Void> sort(@RequestBody List<Long> questionIds) {
        questionService.sort(questionIds);
        return Result.ok("排序成功", null);
    }
}
