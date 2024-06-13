package com.kevin.data_annotation_backendmaster.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user_role")
public class UserRole {
    private static final long serialVersionUID = 1L;

    @TableField("uid")
    private Integer uid;

    @TableField("rid")
    private Integer rid;
}
