package com.kevin.data_annotation_backendmaster.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.entity.GroupRecord;
import com.kevin.data_annotation_backendmaster.entity.dto.ExportRecord;
import com.kevin.data_annotation_backendmaster.service.IGroupRecordService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@RestController
@RequestMapping("/groupRecord")
public class GroupRecordController {
    @Resource
    private IGroupRecordService groupRecordService;

    // 查询GroupRecord
    @GetMapping("/getGroupRecord")
    public Result getGroupRecord(Integer classId, Integer groupId, Integer uid, @RequestParam(defaultValue = "") String labelType) {
        return Result.success(groupRecordService.getGroupRecord(classId, groupId, uid, labelType));
    }

    // 插入GroupRecord
    @PostMapping("/createGroupRecord")
    public void createGroupRecord(@RequestBody GroupRecord groupRecord) {
        groupRecordService.createGroupRecord(groupRecord);
    }

    // 更新GroupRecord
    @PostMapping("/updateGroupRecord")
    public void updateGroupRecord(@RequestBody GroupRecord groupRecord) {
        groupRecordService.updateGroupRecord(groupRecord);
    }

    // 根据id删除GroupRecord
    @DeleteMapping("/deleteGroupRecord/{id}")
    public void deleteGroupRecord(@PathVariable Integer id) {
        groupRecordService.deleteGroupRecord(id);
    }

    // 导出GroupRecord
    @GetMapping("/exportGroupRecord")
    public void exportRecord(@RequestParam Integer classId,
                             @RequestParam Integer groupId,
                             @RequestParam Integer uid,
                             @RequestParam(defaultValue = "") String labelType,
                             @RequestParam(defaultValue = "") String className,
                             @RequestParam(defaultValue = "") String groupName,
                             HttpServletResponse response) throws IOException {
        // 组装文件名
        String fileName = className + "_" + groupName + "_" + uid + "_" + labelType;

        // 根据筛选条件构建查询条件
        QueryWrapper<GroupRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(classId != null, "class_id", classId);
        queryWrapper.eq(groupId != null, "group_id", groupId);
        queryWrapper.eq(uid != null, "uid", uid);
        queryWrapper.like(StrUtil.isNotBlank(labelType), "label_type", labelType);

        // 筛选要导出的属性
        List<GroupRecord> groupRecords = groupRecordService.list(queryWrapper);
        List<ExportRecord> exportRecords = new ArrayList<>();

        // 遍历赋值
        for (GroupRecord groupRecord : groupRecords) {
            ExportRecord exportRecord = new ExportRecord();
            exportRecord.setStart(groupRecord.getStart());
            exportRecord.setEnd(groupRecord.getEnd());
            exportRecord.setLabel(groupRecord.getLabel());
            exportRecords.add(exportRecord);
        }

        // 生成Excel文件
        ExcelWriter writer = ExcelUtil.getWriter(true);
        writer.write(exportRecords, true);

        // 设置ContentType和Header
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "UTF-8") + ".csv");

        // 关流
        ServletOutputStream outputStream = response.getOutputStream();
        writer.flush(outputStream, true);
        writer.close();
        outputStream.flush();
        outputStream.close();
    }
}
