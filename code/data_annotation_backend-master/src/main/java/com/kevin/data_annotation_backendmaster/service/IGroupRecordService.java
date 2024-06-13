package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.GroupRecord;
import com.kevin.data_annotation_backendmaster.entity.Record;

import java.util.List;

public interface IGroupRecordService  extends IService<GroupRecord> {
    List<GroupRecord> getGroupRecord(Integer classId, Integer groupId, Integer uid , String labelType);

    void createGroupRecord(GroupRecord groupRecord);

    void updateGroupRecord(GroupRecord groupRecord);

    void deleteGroupRecord(Integer id);
}
