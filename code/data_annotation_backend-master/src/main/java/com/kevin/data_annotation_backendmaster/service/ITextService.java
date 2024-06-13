package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.Text;

import java.util.List;

public interface ITextService extends IService<Text> {
    List<Text> getText(Integer classId, Integer groupId, Integer personId, Integer uid);

    void createText(Text text);

    void updateText(Text text);

    void deleteText(Integer id);

    List<Text> getAllText(Integer classId, Integer groupId, Integer uid);
}
