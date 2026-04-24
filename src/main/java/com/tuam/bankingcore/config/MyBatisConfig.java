package com.tuam.bankingcore.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.tuam.bankingcore.mapper")
public class MyBatisConfig {
}