package com.kevin.data_annotation_backendmaster.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
public class LabelDto {

    private Integer id;
    private String label;
    private String labelType;
    private Boolean enable;
    private String color;
    private String code; // 假设这是一个字符串，尽管你给出的例子中是null
    private String shortcuts;
}
