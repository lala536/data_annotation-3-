package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.GroupRecord;
import com.kevin.data_annotation_backendmaster.entity.Record;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
@Mapper
public interface GroupRecordMapper extends BaseMapper<GroupRecord> {
    List<GroupRecord> getGroupRecord(Integer classId, Integer groupId, Integer uid , String labelType);

    void createGroupRecord(GroupRecord groupRecord);

    void updateGroupRecord(GroupRecord groupRecord);

    void deleteGroupRecord(Integer id);
}
