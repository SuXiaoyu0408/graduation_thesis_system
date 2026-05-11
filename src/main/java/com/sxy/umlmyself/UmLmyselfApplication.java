package com.sxy.umlmyself;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 毕业论文管理系统 - 主启动类
 * 
 * @author System
 * @version 1.0.0
 */
@SpringBootApplication
@EnableAspectJAutoProxy  // 开启 Spring AOP 注解支持
public class UmLmyselfApplication {

    public static void main(String[] args) {
        SpringApplication.run(UmLmyselfApplication.class, args);
    }

}
