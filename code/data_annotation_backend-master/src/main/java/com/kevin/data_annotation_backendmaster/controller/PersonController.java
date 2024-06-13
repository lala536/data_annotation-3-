package com.kevin.data_annotation_backendmaster.controller;

import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.service.IPersonService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/person")
public class PersonController {
    @Resource
    private IPersonService personService;

    // 查询Person
    @GetMapping("/getPerson")
    public Result getPerson(@RequestParam(defaultValue = "") Integer id) {
        return Result.success(personService.getPerson(id));
    }
}
