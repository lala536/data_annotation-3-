package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Face;
import com.kevin.data_annotation_backendmaster.mapper.FaceMapper;
import com.kevin.data_annotation_backendmaster.service.IFaceService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class FaceServiceImpl extends ServiceImpl<FaceMapper, Face> implements IFaceService {
    @Resource
    private FaceMapper faceMapper;

    @Override
    public List<Face> getFace(String videoName, Integer uid) {
        return faceMapper.getFace(videoName, uid);
    }

    @Override
    public List<Face> getFaceByPersonId(String videoName, Integer uid, Integer personId) {
        return faceMapper.getFaceByPersonId(videoName, uid, personId);
    }

    @Override
    public void deleteFace(String videoName, Integer personId, Integer uid, String time) {
        faceMapper.deleteFace(videoName, personId, uid, time);
    }
}
