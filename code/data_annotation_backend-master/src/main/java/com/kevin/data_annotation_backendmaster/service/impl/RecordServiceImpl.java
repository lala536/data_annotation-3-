package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Record;
import com.kevin.data_annotation_backendmaster.mapper.RecordMapper;
import com.kevin.data_annotation_backendmaster.service.IRecordService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements IRecordService {
    @Resource
    private RecordMapper recordMapper;

    @Override
    public List<Record> getRecord(Integer classId, Integer groupId, Integer personId, Integer uid, String labelType) {
        return recordMapper.getRecord(classId, groupId, personId, uid, labelType);
    }

    @Override
    public void deleteRecord(Integer id) {
        recordMapper.deleteRecord(id);
    }

    @Override
    public void createRecord(Record record) {
        recordMapper.createRecord(record);
    }

    @Override
    public void updateRecord(Record record) {
        recordMapper.updateRecord(record);
    }
}
