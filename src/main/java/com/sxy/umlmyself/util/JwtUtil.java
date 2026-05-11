package com.sxy.umlmyself.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成、解析和校验 JWT Token
 */
@Component
public class JwtUtil {
    
    /**
     * JWT Secret Key（从配置文件中读取，如果没有则使用默认值）
     */
    @Value("${jwt.secret:UMLmyselfSecretKeyForJWTTokenGeneration2024}")
    private String secret;
    
    /**
     * Token 过期时间（单位：毫秒，默认 7 天）
     */
    @Value("${jwt.expiration:604800000}")
    private Long expiration;
    
    /**
     * 获取 Secret Key
     */
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 生成 JWT Token
     * 
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return JWT Token 字符串
     */
    public String generateToken(Integer userId, Integer roleId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("roleId", roleId);
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSecretKey())
                .compact();
    }
    
    /**
     * 清理token中的空白字符和不可见控制字符
     * 
     * @param token 原始token（可能包含"Bearer "前缀）
     * @return 清理后的token
     */
    public String cleanToken(String token) {
        if (token == null) {
            return null;
        }
        // 移除"Bearer "前缀（如果存在）
        String cleaned = token.trim();
        if (cleaned.startsWith("Bearer ")) {
            cleaned = cleaned.substring(7);
        }
        // 去除首尾空白字符
        cleaned = cleaned.trim();
        // 只保留有效的JWT字符 (Base64URL字符和点号、连字符、下划线)，移除所有其他字符
        // 这包括所有空白字符、控制字符等
        cleaned = cleaned.replaceAll("[^A-Za-z0-9\\-_.]", "");
        return cleaned;
    }
    
    /**
     * 从 Token 中获取 Claims
     * 
     * @param token JWT Token（清除空白字符和控制字符）
     * @return Claims 对象
     * @throws IllegalArgumentException 如果token格式不正确或无效
     */
    private Claims getClaimsFromToken(String token) {
        if (token == null) {
            throw new IllegalArgumentException("Token cannot be null");
        }
        // 清除空白字符和不可见控制字符
        String cleanedToken = cleanToken(token);
        
        // 验证清理后的token不为空
        if (cleanedToken == null || cleanedToken.isEmpty()) {
            throw new IllegalArgumentException("Token is empty after cleaning");
        }
        
        try {
            return Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(cleanedToken)
                    .getPayload();
        } catch (Exception e) {
            // 捕获JWT解析异常，提供更友好的错误信息
            throw new IllegalArgumentException("Invalid JWT token: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从 Token 中获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Integer getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("userId", Integer.class);
    }
    
    /**
     * 从 Token 中获取角色ID
     * 
     * @param token JWT Token
     * @return 角色ID
     */
    public Integer getRoleIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.get("roleId", Integer.class);
    }
    
    /**
     * 从 Token 中获取过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims.getExpiration();
    }
    
    /**
     * 判断 Token 是否过期
     * 
     * @param token JWT Token
     * @return true 表示已过期，false 表示未过期
     */
    public Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    /**
     * 校验 Token 是否有效
     * 
     * @param token JWT Token
     * @return true 表示有效，false 表示无效
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
}

