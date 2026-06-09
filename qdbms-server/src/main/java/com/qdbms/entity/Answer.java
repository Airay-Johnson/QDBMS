package com.qdbms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("answer")
public class Answer {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long responseId;
    private Long questionId;
    private String answerText;
    private String answerOptions; // JSON for multi-select
}
