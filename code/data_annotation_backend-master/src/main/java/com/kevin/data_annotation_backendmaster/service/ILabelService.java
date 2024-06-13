package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.Label;

import java.util.List;

public interface ILabelService extends IService<Label> {
    List<Label> getAllLabel();

    List<String> getLabelType();

    List<String> getLabel(String labelType);

    String getColor(String label);

    void updateLabel(Label label);

    String deleteLabel(int id);

    String addLabel(Label label);
}
