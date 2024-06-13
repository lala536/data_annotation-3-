package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Video;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface VideoMapper extends BaseMapper<Video> {
    List<Video> getAllVideo();

    List<Video> getMember(Integer classId, Integer groupId);
}

