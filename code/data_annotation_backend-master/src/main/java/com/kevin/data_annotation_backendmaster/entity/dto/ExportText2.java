package com.kevin.data_annotation_backendmaster.entity.dto;

import cn.hutool.core.annotation.Alias;

/**
 * 导出数据DTO
 */
public class ExportText2 {
    @Alias("开始时间")
    private String start;

    @Alias("结束时间")
    private String end;

    @Alias("标签")
    private String text;

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
