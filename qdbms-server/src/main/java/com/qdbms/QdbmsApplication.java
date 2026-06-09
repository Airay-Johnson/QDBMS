package com.qdbms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.qdbms.mapper")
public class QdbmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QdbmsApplication.class, args);
    }
}
