package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Person;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PersonMapper extends BaseMapper<Person> {
    List<Person> getPerson(Integer id);
}
