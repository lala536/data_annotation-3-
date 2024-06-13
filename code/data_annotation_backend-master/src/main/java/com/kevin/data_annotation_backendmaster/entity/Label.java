package com.kevin.data_annotation_backendmaster.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("label")
public class Label {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("label")
    private String label;

    @TableField("code")
    private String code;

    @TableField("color")
    private String color;

    @TableField("shortcuts")
    private String shortcuts;

    @TableField("label_type")
    private String labelType;

    @TableField("is_group")
    private String isGroup;

    @TableField("enable")
    private String enable;

    @TableField("description")
    private String description;
}
