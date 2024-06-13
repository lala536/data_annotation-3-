package com.kevin.data_annotation_backendmaster.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.entity.Face;
import com.kevin.data_annotation_backendmaster.service.IFaceService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/face")
public class FaceController {
    @Resource
    private IFaceService faceService;

    // 创建一个新的face数据，这里直接调用mybatis-plus的方法saveOrUpdate
    @PostMapping("/saveOrUpdateFace")
    public void saveOrUpdateFace(@RequestBody Face face) {
        QueryWrapper<Face> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("video_name", face.getVideoName())  // 匹配 video_name 字段
                .eq("person_id", face.getPersonId())    // 匹配 person_id 字段
                .eq("uid", face.getUid())              // 匹配 uid 字段
                .eq("time", face.getTime());           // 排除当前记录的 time 值
        Face existingFace = faceService.getOne(queryWrapper);
        if (existingFace != null) {  // 查到了,更新
            existingFace.setX0(face.getX0());
            existingFace.setY0(face.getY0());
            existingFace.setX1(face.getX1());
            existingFace.setY1(face.getY1());
            faceService.updateById(existingFace);
        } else {  // 查不到,新增
            faceService.save(face);
        }
    }

    // 根据videoName和uid获取face
    @GetMapping("/getFace")
    public Result getFace(@RequestParam(defaultValue = "") String videoName, @RequestParam(defaultValue = "") Integer uid) {
        return Result.success(faceService.getFace(videoName, uid));
    }

    // 根据videoName和uid和personId获取face
    @GetMapping("/getFaceByPersonId")
    public Result getFaceByPersonId(@RequestParam(defaultValue = "") String videoName,
                                    @RequestParam(defaultValue = "") Integer uid,
                                    @RequestParam(defaultValue = "") Integer personId) {
        return Result.success(faceService.getFaceByPersonId(videoName, uid, personId));
    }

    // 删除face
    @DeleteMapping("/deleteFace")
    public void deleteFace(@RequestParam(defaultValue = "") String videoName,
                           @RequestParam(defaultValue = "") Integer personId,
                           @RequestParam(defaultValue = "") Integer uid,
                           @RequestParam(defaultValue = "") String time) {
        faceService.deleteFace(videoName, personId, uid, time);
    }
}
