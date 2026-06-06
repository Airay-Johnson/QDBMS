package com.qdbms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class QuestionnaireRequest {
    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题不超过200字")
    private String title;

    private String description;
}
