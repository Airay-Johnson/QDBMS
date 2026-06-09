package com.qdbms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("response")
public class Response {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionnaireId;
    private Long respondentId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime submittedAt;
    private String ipAddress;
}
