package com.kevin.data_annotation_backendmaster.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("text")
public class Text {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("class_id")
    private Integer classId;

    @TableField("group_id")
    private Integer groupId;

    @TableField("person_id")
    private Integer personId;

    @TableField("uid")
    private Integer uid;

    @TableField("start")
    private String start;

    @TableField("end")
    private String end;

    @TableField("text")
    private String text;
}
