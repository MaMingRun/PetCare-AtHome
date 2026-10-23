package com.mr.common.exception;

/**
 * 业务异常基类：业务代码中可预期的错误（如"订单不存在"）都抛它
 */
public class BaseException extends RuntimeException{
    public BaseException(){
    }

    public BaseException(String msg){
        super(msg);
    }
}
