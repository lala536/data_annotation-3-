package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.Record;


import java.util.List;

public interface IRecordService extends IService<Record> {
    List<Record> getRecord(Integer classId, Integer groupId, Integer personId, Integer uid, String labelType);

    void deleteRecord(Integer id);

    void createRecord(Record record);

    void updateRecord(Record record);
}
