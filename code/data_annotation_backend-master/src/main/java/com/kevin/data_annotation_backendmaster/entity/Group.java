package com.kevin.data_annotation_backendmaster.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("group")
public class Group {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("group_name")
    private String groupName;

    @TableField("class_id")
    private Integer classId;

    @TableField("group_size")
    private Integer groupSize;

    @TableField("group_member")
    private String groupMember;
}
