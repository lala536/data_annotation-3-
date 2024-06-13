package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Group;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupMapper extends BaseMapper<Group> {
    List<Group> getAllGroup();

    String getPersonIds(String groupId);
}
