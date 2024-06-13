package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.Group;

import java.util.List;

public interface IGroupService extends IService<Group> {
    List<Group> getAllGroup();

    String getPersonIds(String groupId);
}

