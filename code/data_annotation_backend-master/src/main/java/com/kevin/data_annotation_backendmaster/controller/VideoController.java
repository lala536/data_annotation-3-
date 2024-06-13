package com.kevin.data_annotation_backendmaster.controller;

import cn.hutool.core.util.StrUtil;
import com.kevin.data_annotation_backendmaster.common.Result;
import com.kevin.data_annotation_backendmaster.service.IVideoService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RestController
@RequestMapping("/video")
public class VideoController {
    @Resource
    private IVideoService videoService;

    // 获取所有视频
    @GetMapping("/getAllVideo")
    public Result getAllVideo() {
        return Result.success(videoService.getAllVideo());
    }


    // 播放视频
    @GetMapping("showVideo")
//    @Async("asyncServiceExecutor")
    public void play(HttpServletRequest request, HttpServletResponse response,
                     @RequestParam(defaultValue = "") String videoPath,
                     @RequestParam(defaultValue = "") String videoName) throws IOException {
        response.reset();
        String filePath = videoPath + "/" + videoName;
        File videoFile = new File(filePath);
        if (!videoFile.exists()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        long fileLength = videoFile.length();
        // 随机读文件
        RandomAccessFile randomAccessFile = new RandomAccessFile(videoFile, "r");

        //获取从那个字节开始读取文件
        String rangeString = request.getHeader("Range");
        long range=0;
        if (StrUtil.isNotBlank(rangeString)) {
            range = Long.valueOf(rangeString.substring(rangeString.indexOf("=") + 1, rangeString.indexOf("-")));
        }
        //获取响应的输出流
        OutputStream outputStream = response.getOutputStream();
        //设置内容类型
        response.setHeader("Content-Type", "video/mp4");
        //返回码需要为206，代表只处理了部分请求，响应了部分数据
        response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);

        // 移动访问指针到指定位置
        randomAccessFile.seek(range);
        // 每次请求只返回1MB的视频流
        byte[] bytes = new byte[1024 * 1024];
        int len = randomAccessFile.read(bytes);
        //设置此次相应返回的数据长度
        response.setContentLength(len);
        //设置此次相应返回的数据范围
        response.setHeader("Content-Range", "bytes " + range+"-" + (fileLength - 1) + "/" + fileLength);
        // 将这1MB的视频流响应给客户端
        outputStream.write(bytes, 0, len);
        outputStream.close();
        randomAccessFile.close();
//        System.out.println("返回数据区间:【" + range + "-" + (range + len) + "】");
    }

    // 获取该视频对应的小组成员id
    @GetMapping("/getMember")
    public Result getMember(@RequestParam Integer classId,
                          @RequestParam Integer groupId) {
        return Result.success(videoService.getMember(classId, groupId));
    }

}
