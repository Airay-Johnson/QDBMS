package com.qdbms.controller;

import com.qdbms.common.Result;
import com.qdbms.dto.QuestionnaireRequest;
import com.qdbms.entity.Questionnaire;
import com.qdbms.entity.User;
import com.qdbms.mapper.UserMapper;
import com.qdbms.service.QuestionnaireService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questionnaires")
@RequiredArgsConstructor
public class QuestionnaireController {

    private final QuestionnaireService questionnaireService;
    private final UserMapper userMapper;

    @PostMapping
    public Result<Questionnaire> create(@Valid @RequestBody QuestionnaireRequest request,
                                         Authentication auth) {
        return Result.ok(questionnaireService.create(request, getUserId(auth)));
    }

    @GetMapping
    public Result<List<Questionnaire>> list(Authentication auth) {
        return Result.ok(questionnaireService.listAllByCreator(getUserId(auth)));
    }

    @GetMapping("/{id}")
    public Result<Questionnaire> getById(@PathVariable Long id) {
        return Result.ok(questionnaireService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Questionnaire> update(@PathVariable Long id,
                                         @Valid @RequestBody QuestionnaireRequest request) {
        return Result.ok(questionnaireService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @questionnaireMapper.selectById(#id).creatorId == authentication.principal.id")
    public Result<Void> delete(@PathVariable Long id, Authentication auth) {
        questionnaireService.delete(id, getUserId(auth));
        return Result.ok("删除成功", null);
    }

    @PostMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        questionnaireService.publish(id);
        return Result.ok("发布成功", null);
    }

    @PostMapping("/{id}/close")
    public Result<Void> close(@PathVariable Long id) {
        questionnaireService.close(id);
        return Result.ok("已关闭", null);
    }

    private Long getUserId(Authentication auth) {
        User user = userMapper.selectByUsername(auth.getName());
        return user != null ? user.getId() : null;
    }
}
