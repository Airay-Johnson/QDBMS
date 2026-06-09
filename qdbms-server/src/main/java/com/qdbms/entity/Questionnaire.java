package com.qdbms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("questionnaire")
public class Questionnaire {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long creatorId;
    private String title;
    private String description;
    private Integer status; // 0=草稿 1=已发布 2=已关闭
    @TableField(fill = FieldFill.INSERT)
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime publishedAt;
    private java.time.LocalDateTime closedAt;
}
