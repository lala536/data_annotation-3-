package com.kevin.data_annotation_backendmaster.exception;

import com.kevin.data_annotation_backendmaster.common.Result;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobalException {
    /**
     * 业务异常
     * @param e
     * @return
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Result serviceException(ServiceException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 全局异常
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result globalException(Exception e) {
        e.printStackTrace();
        return Result.error("500", "系统错误");
    }
}
