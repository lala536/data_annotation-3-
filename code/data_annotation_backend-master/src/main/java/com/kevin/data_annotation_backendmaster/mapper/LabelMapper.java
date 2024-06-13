package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Label;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LabelMapper extends BaseMapper<Label> {
    List<Label> getAllLabel();

    List<String> getLabelType();

    List<String> getLabel(String labelType);

    String getColor(String label);

    void updateLabel(Label label);

   String deleteLabel(int id);

   void addLabel(Label label);
}
