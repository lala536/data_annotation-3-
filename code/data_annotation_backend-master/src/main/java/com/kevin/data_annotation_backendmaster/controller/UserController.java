package com.kevin.data_annotation_backendmaster.controller;

import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.service.IUserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private IUserService userService;

    // 获取所有username
    @GetMapping("/getUsernames")
    public Result getUsernames() {
        return Result.success(userService.getUsernames());
    }

    // 获取对应username的id作为uid
    @GetMapping("/getIdByUsername")
    public Result getIdByUsername(@RequestParam(defaultValue = "") String username) {
        return Result.success(userService.getIdByUsername(username));
    }
}
