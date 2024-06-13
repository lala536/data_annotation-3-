package com.kevin.data_annotation_backendmaster.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.entity.Record;
import com.kevin.data_annotation_backendmaster.entity.dto.ExportRecord;
import com.kevin.data_annotation_backendmaster.service.IRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/record")
public class RecordController {
    @Resource
    private IRecordService recordService;

    // 查询Record
    @GetMapping("/getRecord")
    public Result getRecord(@RequestParam Integer classId,
                            @RequestParam Integer groupId,
                            @RequestParam Integer personId,
                            @RequestParam Integer uid,
                            @RequestParam(defaultValue = "") String labelType) {
        return Result.success(recordService.getRecord(classId, groupId, personId, uid, labelType));
    }

    // 根据id删除一条record
    @DeleteMapping("/deleteRecord/{id}")
    public void deleteRecord(@PathVariable Integer id) {
        recordService.deleteRecord(id);
    }

    // 新增一条record
    @PostMapping("/createRecord")
    public void createRecord(@RequestBody Record record) {
        recordService.createRecord(record);
    }

    // 更新一条record
    @PostMapping("/updateRecord")
    public void updateRecord(@RequestBody Record record) {
        recordService.updateRecord(record);
    }

    // 根据条件导出record表
    @GetMapping("/exportRecord")
    public void exportRecord(@RequestParam Integer classId,
                             @RequestParam Integer groupId,
                             @RequestParam Integer personId,
                             @RequestParam Integer uid,
                             @RequestParam(defaultValue = "") String labelType,
                             @RequestParam(defaultValue = "") String className,
                             @RequestParam(defaultValue = "") String groupName,
                             @RequestParam(defaultValue = "") String personName,
                             HttpServletResponse response) throws IOException {
        // 组装文件名
        String fileName = className + "_" + groupName + "_" + personName + "_" + uid + "_" + labelType;

        // 根据筛选条件构建查询条件
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(classId != null, "class_id", classId);
        queryWrapper.eq(groupId != null, "group_id", groupId);
        queryWrapper.eq(personId != null, "person_id", personId);
        queryWrapper.eq(uid != null, "uid", uid);
        queryWrapper.like(StrUtil.isNotBlank(labelType), "label_type", labelType);

        // 筛选要导出的属性
        List<Record> records = recordService.list(queryWrapper);
        List<ExportRecord> exportRecords = new ArrayList<>();

        for (Record record : records) {
            ExportRecord exportRecord = new ExportRecord();
            exportRecord.setStart(record.getStart());
            exportRecord.setEnd(record.getEnd());
            exportRecord.setLabel(record.getLabel());
            exportRecords.add(exportRecord);
        }

        // 生成Excel文件
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(exportRecords, true);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".xlsx");

        ServletOutputStream outputStream = response.getOutputStream();
        writer.flush(outputStream, true);
        writer.close();
        outputStream.flush();
        outputStream.close();
    }
}
