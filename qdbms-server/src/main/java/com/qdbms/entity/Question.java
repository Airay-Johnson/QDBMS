package com.qdbms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("question")
public class Question {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionnaireId;
    private String questionText;
    private String type; // single, multiple, text
    private String options; // JSON string
    private Integer sequenceNumber;
    private Boolean isRequired;
}
