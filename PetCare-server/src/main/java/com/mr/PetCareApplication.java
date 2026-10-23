package com.mr;

import com.mr.common.utils.CryptoUtil;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement //开启注解方式的事务管理
@MapperScan("com.mr.mapper") //扫描 mapper 接口
@Slf4j
@EnableScheduling
public class PetCareApplication {
    public static void main(String[] args) {
       SpringApplication.run(PetCareApplication.class, args);
    }
}
