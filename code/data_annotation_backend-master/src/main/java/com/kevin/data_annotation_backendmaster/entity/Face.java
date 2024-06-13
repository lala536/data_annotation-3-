package com.kevin.data_annotation_backendmaster.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("face")
public class Face {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("video_name")
    private String videoName;

    @TableField("person_id")
    private Integer personId;

    @TableField("uid")
    private Integer uid;

    @TableField("time")
    private String time;

    @TableField("x0")
    private float x0;

    @TableField("y0")
    private float y0;

    @TableField("x1")
    private float x1;

    @TableField("y1")
    private float y1;
}
