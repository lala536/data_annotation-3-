package com.kevin.data_annotation_backendmaster.controller;

import cn.hutool.core.util.StrUtil;
import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.entity.User;
import com.kevin.data_annotation_backendmaster.service.IUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/web")
public class WebController {
    @Resource
    IUserService userService;

    // 登录
    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("数据输入不合法");
        }
        user = userService.login(user);
        return Result.success(user);
    }

    // 注册
    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        if (StrUtil.isBlank(user.getUsername()) || StrUtil.isBlank(user.getPassword())) {
            return Result.error("数据输入不合法");
        }
        if (user.getUsername().length() > 10 || user.getPassword().length() > 20) {
            return Result.error("数据输入不合法");
        }
        user = userService.register(user);
        return Result.success(user);
    }
}
