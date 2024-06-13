package com.kevin.data_annotation_backendmaster.controller;

import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.service.IClassService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/class")
public class ClassController {
    @Resource
    private IClassService classService;

    // 获取class表所有字段
    @GetMapping("/getAllClass")
    public Result getAllClass() {
        return Result.success(classService.getAllClass());
    }
}
