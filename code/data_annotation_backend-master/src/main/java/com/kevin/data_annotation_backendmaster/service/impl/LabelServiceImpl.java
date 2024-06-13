package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Label;
import com.kevin.data_annotation_backendmaster.mapper.LabelMapper;
import com.kevin.data_annotation_backendmaster.service.ILabelService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class LabelServiceImpl extends ServiceImpl<LabelMapper, Label> implements ILabelService {
    @Resource
    private LabelMapper labelMapper;

    @Override
    public List<Label> getAllLabel() {
        return labelMapper.getAllLabel();
    }

    @Override
    public List<String> getLabelType() {
        return labelMapper.getLabelType();
    }

    @Override
    public List<String> getLabel(String labelType) {
        return labelMapper.getLabel(labelType);
    }

    @Override
    public String getColor(String label) {
        return labelMapper.getColor(label);
    }
    @Override
    public String deleteLabel(int id){
        return labelMapper.deleteLabel(id);
    }

    @Override
    public void updateLabel(Label label) {
        labelMapper.updateLabel(label);
    }
    @Override
    public String addLabel(Label label) {
         labelMapper.addLabel(label);
         return "null";
    }
}
