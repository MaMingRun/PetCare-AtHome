package com.mr.common.context;

/**
 * 基于 ThreadLocal 封装：保存当前登录者 id（用户/服务师/员工通用）
 * 同一线程内任何地方都能取，请求结束必须清理（见拦截器）
 */
public class BaseContext {

    private static final ThreadLocal<Long> THREAD_LOCAL = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        THREAD_LOCAL.set(id);
    }

    public static Long getCurrentId() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
