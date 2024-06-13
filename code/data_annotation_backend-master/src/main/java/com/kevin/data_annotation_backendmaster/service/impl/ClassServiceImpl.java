package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Class;
import com.kevin.data_annotation_backendmaster.mapper.ClassMapper;
import com.kevin.data_annotation_backendmaster.service.IClassService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements IClassService {
    @Resource
    private ClassMapper classMapper;

    @Override
    public List<Class> getAllClass() {
        return classMapper.getAllClass();
    }
}
