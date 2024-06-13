package com.kevin.data_annotation_backendmaster.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.entity.Record;
import com.kevin.data_annotation_backendmaster.entity.Text;
import com.kevin.data_annotation_backendmaster.entity.dto.ExportRecord;
import com.kevin.data_annotation_backendmaster.entity.dto.ExportText1;
import com.kevin.data_annotation_backendmaster.entity.dto.ExportText2;
import com.kevin.data_annotation_backendmaster.service.ITextService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/text")
public class TextController {
    @Resource
    private ITextService textService;

    // 根据person_id查询Text
    @GetMapping("/getText")
    public Result getText(@RequestParam Integer classId,
                            @RequestParam Integer groupId,
                            @RequestParam Integer personId,
                            @RequestParam Integer uid) {
        return Result.success(textService.getText(classId, groupId, personId, uid));
    }

    // 查询所有Text
    @GetMapping("/getAllText")
    public Result getAllText(@RequestParam Integer classId,
                          @RequestParam Integer groupId,
                          @RequestParam Integer uid) {
        return Result.success(textService.getAllText(classId, groupId, uid));
    }

    // 根据id删除Text
    @DeleteMapping("/deleteText/{id}")
    public void deleteText(@PathVariable Integer id) {
        textService.deleteText(id);
    }

    // 新增Text
    @PostMapping("/createText")
    public void createText(@RequestBody Text text) {
        textService.createText(text);
    }

    // 更新Text
    @PostMapping("/updateText")
    public void updateText(@RequestBody Text text) {
        textService.updateText(text);
    }

    // 导出全部Text
    @GetMapping("/exportText1")
    public void exportText1(@RequestParam Integer classId,
                            @RequestParam Integer groupId,
                            @RequestParam Integer uid,
                            @RequestParam(defaultValue = "") String className,
                            @RequestParam(defaultValue = "") String groupName,
                            HttpServletResponse response) throws IOException {
        // 组装文件名
        String fileName = className + "_" + groupName + "_" + uid;

        // 根据筛选条件构建查询条件
        QueryWrapper<Text> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(classId != null, "class_id", classId);
        queryWrapper.eq(groupId != null, "group_id", groupId);
        queryWrapper.eq(uid != null, "uid", uid);

        // 筛选要导出的属性
        List<Text> texts = textService.list(queryWrapper);
        List<ExportText1> exportText1s = new ArrayList<>();

        for (Text text : texts) {
            ExportText1 exportText1 = new ExportText1();
            exportText1.setStart(text.getStart());
            exportText1.setEnd(text.getEnd());
            exportText1.setPersonId(text.getPersonId());
            exportText1.setText(text.getText());
            exportText1s.add(exportText1);
        }

        // 生成Excel文件
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(exportText1s, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".csv");

        ServletOutputStream outputStream = response.getOutputStream();
        writer.flush(outputStream, true);
        writer.close();
        outputStream.flush();
        outputStream.close();
    }

    // 导出个人Text
    @GetMapping("/exportText2")
    public void exportText2(@RequestParam Integer classId,
                            @RequestParam Integer groupId,
                            @RequestParam Integer personId,
                            @RequestParam Integer uid,
                            @RequestParam(defaultValue = "") String className,
                            @RequestParam(defaultValue = "") String groupName,
                            @RequestParam(defaultValue = "") String personName,
                            HttpServletResponse response) throws IOException {
        // 组装文件名
        String fileName = className + "_" + groupName + "_" + personName + "_" + uid;

        // 根据筛选条件构建查询条件
        QueryWrapper<Text> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(classId != null, "class_id", classId);
        queryWrapper.eq(groupId != null, "group_id", groupId);
        queryWrapper.eq(personId != null, "person_id", personId);
        queryWrapper.eq(uid != null, "uid", uid);

        // 筛选要导出的属性
        List<Text> texts = textService.list(queryWrapper);
        List<ExportText2> exportText2s = new ArrayList<>();

        for (Text text : texts) {
            ExportText2 exportText2 = new ExportText2();
            exportText2.setStart(text.getStart());
            exportText2.setEnd(text.getEnd());
            exportText2.setText(text.getText());
            exportText2s.add(exportText2);
        }

        // 生成Excel文件
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(exportText2s, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".csv");

        ServletOutputStream outputStream = response.getOutputStream();
        writer.flush(outputStream, true);
        writer.close();
        outputStream.flush();
        outputStream.close();
    }
}
