package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.GroupRecord;
import com.kevin.data_annotation_backendmaster.mapper.GroupRecordMapper;
import com.kevin.data_annotation_backendmaster.service.IGroupRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
@Service
public class GroupRecordServiceImpl extends ServiceImpl<GroupRecordMapper, GroupRecord> implements IGroupRecordService {
    @Resource
    private GroupRecordMapper groupRecordMapper;

    @Override
    public List<GroupRecord> getGroupRecord(Integer classId, Integer groupId, Integer uid , String labelType) {
        return groupRecordMapper.getGroupRecord(classId, groupId, uid, labelType);
    }

    @Override
    public void createGroupRecord(GroupRecord groupRecord) {
        groupRecordMapper.createGroupRecord(groupRecord);
    }

    @Override
    public void updateGroupRecord(GroupRecord groupRecord) {
        groupRecordMapper.updateGroupRecord(groupRecord);
    }

    @Override
    public void deleteGroupRecord(Integer id) {
        groupRecordMapper.deleteGroupRecord(id);
    }
}
