package com.mr.common.handler;

import com.mr.common.exception.BaseException;
import com.mr.common.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：拦截 Controller 抛出的异常，统一包装成 Result 返回
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常（可预期，msg直接给前端展示）
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(BaseException ex) {
        log.error("业务异常: {}",ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * 兜底：捕获未知异常(不给前端看堆栈，只记日志)
     */
    @ExceptionHandler
    public Result<String> exceptionHandler(Exception ex) {
        log.error("系统异常:{}",ex);
        return Result.error("系统繁忙，请稍后再试");
    }
}
