package com.qdbms.controller;

import com.qdbms.common.Result;
import com.qdbms.entity.Response;
import com.qdbms.service.ResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/responses")
@RequiredArgsConstructor
public class ResponseController {

    private final ResponseService responseService;

    @PostMapping
    public Result<Response> submit(@RequestParam Long questionnaireId,
                                    @RequestBody List<Map<String, Object>> answers,
                                    HttpServletRequest request) {
        return Result.ok(responseService.submit(questionnaireId, answers, request.getRemoteAddr()));
    }

    @GetMapping
    public Result<List<Response>> listByQuestionnaire(@RequestParam Long questionnaireId) {
        return Result.ok(responseService.listByQuestionnaire(questionnaireId));
    }
}
