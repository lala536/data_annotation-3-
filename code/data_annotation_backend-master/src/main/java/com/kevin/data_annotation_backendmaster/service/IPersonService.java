package com.kevin.data_annotation_backendmaster.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kevin.data_annotation_backendmaster.entity.Person;

import java.util.List;

public interface IPersonService extends IService<Person> {
    List<Person> getPerson(Integer id);
}

