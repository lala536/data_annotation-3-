package com.kevin.data_annotation_backendmaster.common;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import java.nio.file.Path;

/**
 * 配置视频文件传输的类
 */
@Component
public class NonStaticResourceHttpRequestHandler extends ResourceHttpRequestHandler {

//    public final static String ATTR_FILE = "NON-STATIC-FILE";
//
//    @Override
//    protected Resource getResource(HttpServletRequest request) {
//        final Path filePath = (Path) request.getAttribute(ATTR_FILE);
//        return new FileSystemResource(filePath);
//    }
}