package com.sxy.umlmyself.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 权限校验注解
 * 用于标记需要特定角色才能访问的方法
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequireRole {
    
    /**
     * 允许访问的角色代码列表
     */
    String[] value() default {};
    
    /**
     * 是否要求管理员权限
     */
    boolean requireAdmin() default false;
}

