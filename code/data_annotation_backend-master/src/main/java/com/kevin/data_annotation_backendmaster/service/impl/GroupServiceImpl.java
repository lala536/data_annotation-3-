package com.kevin.data_annotation_backendmaster.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Group;
import com.kevin.data_annotation_backendmaster.mapper.GroupMapper;
import com.kevin.data_annotation_backendmaster.service.IGroupService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, Group> implements IGroupService {
    @Resource
    private GroupMapper groupMapper;

    @Override
    public List<Group> getAllGroup() {
        return groupMapper.getAllGroup();
    }

    @Override
    public String getPersonIds(String groupId) {
        return groupMapper.getPersonIds(groupId);
    }
}
