package com.kevin.data_annotation_backendmaster.controller;

import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.service.IGroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/group")
public class GroupController {
    @Resource
    private IGroupService groupService;

    // 获取group表所有字段
    @GetMapping("/getAllGroup")
    public Result getAllGroup() {
        return Result.success(groupService.getAllGroup());
    }

    // 获取某个小组的所有person_id
    @GetMapping("/getPersonIds")
    public Result getPersonIds(@RequestParam(defaultValue = "") String groupId) {
        return Result.success(groupService.getPersonIds(groupId));
    }
}
