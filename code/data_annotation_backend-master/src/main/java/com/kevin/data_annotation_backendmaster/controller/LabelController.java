package com.kevin.data_annotation_backendmaster.controller;

import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.entity.Label;
import com.kevin.data_annotation_backendmaster.service.ILabelService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/label")
public class LabelController {
    @Resource
    private ILabelService labelService;

    // 获取label表里所有数据
    @GetMapping("/getAllLabel")
    public Result getAllLabel() {
        return Result.success(labelService.getAllLabel());
    }

    // 获取label表里所有数据
    @PostMapping("/postAllLabel")
    public Result postAllLabel() {
        return Result.success(labelService.getAllLabel());
    }

    // 获取label表里的所有label_type
    @GetMapping("/getLabelType")
    public Result getLabelType() {
        return Result.success(labelService.getLabelType());
    }

    // 获取label表里对应label_type的所有label
    @GetMapping("/getLabel")
    public Result getLabel(@RequestParam(defaultValue = "") String labelType) {
        return Result.success(labelService.getLabel(labelType));
    }

    // 获取label表里对应label的color
    @GetMapping("/getColor")
    public Result getColor(@RequestParam(defaultValue = "") String label) {
        return Result.success(labelService.getColor(label));
    }
    //删除某个label标签
    @PostMapping("/deleteLabel")
    public Result deleteLabel(@RequestBody Integer id){
        labelService.deleteLabel(id);
        return Result.success(0);
    }

    @PostMapping("/addLabel")
    public void addLabel(@RequestBody Label label){
         labelService.addLabel(label);
         Result.success("ok");
    }

    @PostMapping("/updateAllLabels")
    public Result updateAllLabels(@RequestBody List<Label> finalEmotions) {
        finalEmotions.forEach(label -> labelService.updateLabel(label));
        return Result.success("ok");
    }
    // 新增一个标签
    @PostMapping("/updateLabel")
    public  Result updateLabel(@RequestBody Label label) {
        labelService.updateLabel(label);
       return Result.success("ok");
    }
}
