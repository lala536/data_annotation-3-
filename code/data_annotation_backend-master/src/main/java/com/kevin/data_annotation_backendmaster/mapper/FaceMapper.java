package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Face;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FaceMapper extends BaseMapper<Face> {
    List<Face> getFace(String videoName, Integer uid);

    List<Face> getFaceByPersonId(String videoName, Integer uid, Integer personId);

    void deleteFace(String videoName, Integer personId, Integer uid, String time);
}

