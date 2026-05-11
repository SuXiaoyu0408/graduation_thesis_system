---
module: dashboard
path: 00-Dashboard
keywords: quick-reference, commands, setup, debugging
---

# 快速参考

#dashboard #quick-reference

## 关键命令
| 操作 | 命令 |
|------|------|
| 启动应用 | `./mvnw spring-boot:run` |
| 构建 JAR | `./mvnw clean package` |
| 运行测试 | `./mvnw test` |
| 运行指定测试 | `./mvnw test -Dtest=ClassName` |

## 环境配置
| 配置项 | 文件 | 说明 |
|--------|------|------|
| 数据库URL | `application.properties` → `spring.datasource.url` | 默认指向 `172.20.10.4:3306` |
| JWT密钥 | `application.properties` → `jwt.secret` | 生产环境需更换 |
| JWT过期时间 | `application.properties` → `jwt.expiration` | 默认7天(604800000ms) |
| Redis | `application.properties` → `spring.data.redis.*` | 用于验证码存储 |
| DDL策略 | `application.properties` → `spring.jpa.hibernate.ddl-auto` | 当前为`validate` |
| 服务端口 | `application.properties` → `server.port` | 默认8080 |

## 重要文件位置
| 文件/目录 | 用途 |
|-----------|------|
| `pom.xml` | Maven 依赖与构建配置 |
| `src/main/resources/application.properties` | 应用配置中心 |
| `src/main/java/.../UmLmyselfApplication.java` | Spring Boot 启动类 |
| `src/main/java/.../common/ApiResponse.java` | 统一响应封装 |
| `src/main/java/.../common/RequireRole.java` | 角色权限注解 |
| `src/main/java/.../aspect/RoleCheckAspect.java` | AOP 权限拦截切面 |
| `src/main/java/.../util/JwtUtil.java` | JWT 工具类 |
| `src/main/java/.../common/GlobalExceptionHandler.java` | 全局异常处理 |
| `src/main/java/.../config/CorsConfig.java` | CORS 跨域配置 |
| `src/main/java/.../config/WebConfig.java` | 类型转换器注册 |
| `src/main/resources/static/` | 前端静态页面 |
| `src/main/resources/db/` | 数据库建表脚本 |
| `uploads/` | 文件上传目录 |

## 常见调试
| 症状           | 排查位置                                                          | 参考笔记                    |
| ------------ | ------------------------------------------------------------- | ----------------------- |
| JWT Token 无效 | 检查 `JwtUtil.cleanToken()` 是否清理了控制字符                           | [[JWT-Authorization]]   |
| 权限拒绝         | 检查 `RoleCheckAspect` 日志和 `@RequireRole` 注解值                   | [[JWT-Authorization]]   |
| 数据库连接失败      | 检查 `spring.datasource.url` 和 MySQL 服务状态                       | [[System-Architecture]] |
| DDL 验证错误     | `spring.jpa.hibernate.ddl-auto=validate` 时表结构不匹配              | [[Data-Model]]          |
| 文件上传失败       | 确认 `uploads/` 目录存在且有写权限                                       | [[Business-Logic]]      |
| Redis 连接失败   | 如不用验证码功能可注释 Redis 配置                                          | [[Build-Deploy]]      |
| 参数校验不生效      | 确认 DTO 使用了 `@Validated` 且依赖了 `spring-boot-starter-validation` | [[Infrastructure]]      |
