package com.qdbms.service;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface ExportService {
    void exportToExcel(Long questionnaireId, HttpServletResponse response) throws IOException;
}
