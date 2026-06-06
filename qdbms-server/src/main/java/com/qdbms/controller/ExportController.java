package com.qdbms.controller;

import com.qdbms.service.ExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final ExportService exportService;

    @GetMapping("/questionnaire/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','USER')")
    public void exportExcel(@PathVariable Long id,
                            HttpServletResponse response) throws IOException {
        exportService.exportToExcel(id, response);
    }
}
