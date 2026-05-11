package com.sxy.umlmyself.aspect;

import com.sxy.umlmyself.common.BusinessException;
import com.sxy.umlmyself.common.RequireRole;
import com.sxy.umlmyself.repository.RoleRepository;
import com.sxy.umlmyself.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色权限校验切面
 * 通过AOP实现统一的权限校验
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RoleCheckAspect {
    
    private final JwtUtil jwtUtil;
    private final RoleRepository roleRepository;
    
    /**
     * 拦截带有@RequireRole注解的方法或类
     * 支持方法级别和类级别的注解
     */
    @Before("@annotation(com.sxy.umlmyself.common.RequireRole) || @within(com.sxy.umlmyself.common.RequireRole)")
    public void checkRole(JoinPoint joinPoint) {
        // 获取请求对象
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException("无法获取请求信息");
        }
        
        HttpServletRequest request = attributes.getRequest();
        String auth = request.getHeader("Authorization");
        
        if (auth == null || !auth.startsWith("Bearer ")) {
            throw new BusinessException("认证头格式不正确");
        }
        
        String rawToken = auth.substring(7);
        log.info("Raw token from header: [" + rawToken + "]");
        log.info("Raw token bytes: " + Arrays.toString(rawToken.getBytes(StandardCharsets.UTF_8)));

        String cleanedToken = jwtUtil.cleanToken(rawToken);
        log.info("Cleaned token: [" + cleanedToken + "]");
        log.info("Cleaned token bytes: " + Arrays.toString(cleanedToken.getBytes(StandardCharsets.UTF_8)));

        if (cleanedToken == null || cleanedToken.isEmpty()) {
            throw new BusinessException("未提供认证令牌");
        }
        
        // 获取当前用户ID
        Integer userId;
        try {
            userId = jwtUtil.getUserIdFromToken(cleanedToken);
        } catch (IllegalArgumentException e) {
            log.error("Token解析失败: {}", e.getMessage());
            throw new BusinessException("无效的认证令牌: " + e.getMessage());
        } catch (Exception e) {
            log.error("Token验证失败: {}", e.getMessage(), e);
            throw new BusinessException("认证令牌验证失败: " + e.getMessage());
        }
        
        if (userId == null) {
            throw new BusinessException("无效的认证令牌");
        }
        
        // 获取方法上的注解（优先使用方法级别的注解）
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequireRole requireRole = method.getAnnotation(RequireRole.class);
        
        // 如果方法上没有注解，则检查类级别的注解
        if (requireRole == null) {
            Class<?> targetClass = joinPoint.getTarget().getClass();
            requireRole = targetClass.getAnnotation(RequireRole.class);
        }
        
        // 如果都没有注解，直接返回（理论上不应该发生，因为切点已经过滤了）
        if (requireRole == null) {
            return;
        }
        
        // 检查是否需要管理员权限
        if (requireRole.requireAdmin()) {
            checkAdminRole(userId, cleanedToken);
            return;
        }
        
        // 检查是否需要特定角色
        if (requireRole.value().length > 0) {
            checkSpecificRoles(userId, requireRole.value());
        }
    }
    
    /**
     * 检查管理员权限
     */
    private void checkAdminRole(Integer userId, String token) {
        // 获取用户的所有角色
        List<String> roleCodes = roleRepository.findRolesByUserId(userId).stream()
                .map(role -> role.getRoleCode())
                .collect(Collectors.toList());
        
        if (!roleCodes.contains("ADMIN")) {
            log.warn("用户 {} 尝试访问管理员接口，但无管理员权限", userId);
            throw new BusinessException("无权限访问，需要管理员权限");
        }
    }
    
    /**
     * 检查特定角色权限
     */
    private void checkSpecificRoles(Integer userId, String[] requiredRoles) {
        // 获取用户的所有角色
        List<String> userRoleCodes = roleRepository.findRolesByUserId(userId).stream()
                .map(role -> role.getRoleCode())
                .collect(Collectors.toList());
        
        List<String> requiredRoleList = Arrays.asList(requiredRoles);
        
        // 检查用户是否拥有任一所需角色
        boolean hasRequiredRole = requiredRoleList.stream()
                .anyMatch(userRoleCodes::contains);
        
        if (!hasRequiredRole) {
            log.warn("用户 {} 尝试访问需要角色 {} 的接口，但无相应权限", userId, Arrays.toString(requiredRoles));
            throw new BusinessException("无权限访问，需要以下角色之一: " + Arrays.toString(requiredRoles));
        }
    }
}