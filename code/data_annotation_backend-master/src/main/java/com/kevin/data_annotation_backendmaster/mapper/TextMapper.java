package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Text;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface TextMapper extends BaseMapper<Text> {
    List<Text> getText(Integer classId, Integer groupId, Integer personId, Integer uid);

    void createText(Text text);

    void updateText(Text text);

    void deleteText(Integer id);

    List<Text> getAllText(Integer classId, Integer groupId, Integer uid);
}