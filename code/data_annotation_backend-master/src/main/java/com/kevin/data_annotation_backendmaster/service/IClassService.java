package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.Class;

import java.util.List;

public interface IClassService extends IService<Class> {
    List<Class> getAllClass();
}
