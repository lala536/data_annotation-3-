package com.kevin.data_annotation_backendmaster.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("static")
public class Video {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("video_name")
    private String videoName;

    @TableField("video_path")
    private String videoPath;

    @TableField("class_id")
    private Integer classId;

    @TableField("group_id")
    private Integer groupId;

    @TableField("member")
    private String member;
}
