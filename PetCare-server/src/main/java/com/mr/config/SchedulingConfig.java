package com.mr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling   // 开启 Spring 原生定时（无需引新依赖）
public class SchedulingConfig {
}