package com.kevin.data_annotation_backendmaster.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kevin.data_annotation_backendmaster.entity.Person;
import com.kevin.data_annotation_backendmaster.mapper.PersonMapper;
import com.kevin.data_annotation_backendmaster.service.IPersonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements IPersonService {
    @Resource
    private PersonMapper personMapper;

    @Override
    public List<Person> getPerson(Integer id) {
        return personMapper.getPerson(id);
    }
}
