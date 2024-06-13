package com.kevin.data_annotation_backendmaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kevin.data_annotation_backendmaster.entity.Record;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RecordMapper extends BaseMapper<Record> {
    List<Record> getRecord(Integer classId, Integer groupId, Integer personId, Integer uid, String labelType);

    void deleteRecord(Integer id);

    void createRecord(Record record);

    void updateRecord(Record record);
}

