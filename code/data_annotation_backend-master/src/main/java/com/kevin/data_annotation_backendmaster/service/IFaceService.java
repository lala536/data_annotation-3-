package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.Face;

import java.util.List;

public interface IFaceService extends IService<Face> {
    List<Face> getFace(String videoName, Integer uid);

    List<Face> getFaceByPersonId(String videoName, Integer uid, Integer personId);

    void deleteFace(String videoName, Integer personId, Integer uid, String time);
}
