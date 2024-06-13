package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Text;
import com.kevin.data_annotation_backendmaster.mapper.TextMapper;
import com.kevin.data_annotation_backendmaster.service.ITextService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class TextServiceImpl extends ServiceImpl<TextMapper, Text> implements ITextService {
    @Resource
    private TextMapper textMapper;

    @Override
    public List<Text> getText(Integer classId, Integer groupId, Integer personId, Integer uid) {
        return textMapper.getText(classId, groupId, personId, uid);
    }

    @Override
    public void createText(Text text) {
        textMapper.createText(text);
    }

    @Override
    public void updateText(Text text) {
        textMapper.updateText(text);
    }

    @Override
    public void deleteText(Integer id) {
        textMapper.deleteText(id);
    }

    @Override
    public List<Text> getAllText(Integer classId, Integer groupId, Integer uid) {
        return textMapper.getAllText(classId, groupId, uid);
    }
}