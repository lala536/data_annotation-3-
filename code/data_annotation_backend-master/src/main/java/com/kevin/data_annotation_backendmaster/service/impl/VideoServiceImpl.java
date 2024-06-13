package com.kevin.data_annotation_backendmaster.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kevin.data_annotation_backendmaster.common.NonStaticResourceHttpRequestHandler;
import com.kevin.data_annotation_backendmaster.entity.Video;
import com.kevin.data_annotation_backendmaster.mapper.VideoMapper;
import com.kevin.data_annotation_backendmaster.service.IVideoService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class VideoServiceImpl extends ServiceImpl<VideoMapper, Video> implements IVideoService {
    @Resource
    private VideoMapper videoMapper;

    @Override
    public List<Video> getAllVideo() {
        return videoMapper.getAllVideo();
    }

    @Override
    public List<Video> getMember(Integer classId, Integer groupId) {
        return videoMapper.getMember(classId, groupId);
    }
}
