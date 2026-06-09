package com.qdbms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class QuestionRequest {
    @NotBlank(message = "题目内容不能为空")
    private String questionText;

    @NotBlank(message = "题目类型不能为空")
    private String type; // single, multiple, text

    private String options; // JSON

    private Integer sequenceNumber;

    private Boolean isRequired = true;
}
