package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Class;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ClassMapper extends BaseMapper<Class> {
    List<Class> getAllClass();
}
