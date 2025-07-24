package com.example.filmsfinder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 添加 MapperScan，扫描 com.example.filmsfinder.mapper 下所有 @Mapper 接口
@MapperScan("com.example.filmsfinder.mapper")
public class FilmsFinderApplication {

    public static void main(String[] args) {

        SpringApplication.run(FilmsFinderApplication.class, args);
    }

}
