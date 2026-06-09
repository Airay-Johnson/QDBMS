package com.qdbms.controller;

import com.qdbms.common.PageResult;
import com.qdbms.common.Result;
import com.qdbms.entity.Respondent;
import com.qdbms.service.RespondentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/respondents")
@RequiredArgsConstructor
public class RespondentController {

    private final RespondentService respondentService;

    @GetMapping
    public Result<PageResult<Respondent>> listByQuestionnaire(
            @RequestParam Long questionnaireId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        var ipage = respondentService.listByQuestionnaire(questionnaireId, page, size);
        return Result.ok(PageResult.of(ipage));
    }

    @GetMapping("/groups")
    public Result<List<Respondent>> listByGroup(
            @RequestParam Long questionnaireId,
            @RequestParam(required = false) String groupName) {
        return Result.ok(respondentService.listByGroup(questionnaireId, groupName));
    }

    @GetMapping("/{id}")
    public Result<Respondent> getById(@PathVariable Long id) {
        return Result.ok(respondentService.getById(id));
    }

    @PostMapping
    public Result<Respondent> create(@RequestParam Long questionnaireId,
                                      @RequestBody Respondent respondent) {
        respondent.setQuestionnaireId(questionnaireId);
        return Result.ok(respondentService.create(respondent));
    }

    @PutMapping("/{id}")
    public Result<Respondent> update(@PathVariable Long id,
                                      @RequestBody Respondent respondent) {
        return Result.ok(respondentService.update(id, respondent));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        respondentService.delete(id);
        return Result.ok("删除成功", null);
    }

    @PostMapping("/import")
    public Result<Map<String, Object>> importExcel(@RequestParam Long questionnaireId,
                                                    @RequestParam("file") MultipartFile file) throws IOException {
        int count = respondentService.importFromExcel(questionnaireId, file.getInputStream());
        return Result.ok(Map.of("imported", count));
    }
}
