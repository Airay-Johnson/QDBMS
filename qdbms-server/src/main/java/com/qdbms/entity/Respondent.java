package com.qdbms.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

@Data
@TableName("respondent")
public class Respondent {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long questionnaireId;
    private String name;
    private String email;
    private String phone;
    private String groupName;
}
