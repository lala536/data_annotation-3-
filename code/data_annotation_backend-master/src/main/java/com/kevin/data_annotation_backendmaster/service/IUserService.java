package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.User;

import java.util.List;

public interface IUserService extends IService<User> {
    User login(User user);
    User register(User user);
    List<String> getUsernames();
    Integer getIdByUsername(String username);
}

