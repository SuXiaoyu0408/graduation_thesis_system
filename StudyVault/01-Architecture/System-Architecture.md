---
module: architecture
path: 01-Architecture
keywords: architecture, layered, spring-boot, thesis-management
---

# 系统架构 (重要性: ★★★)

#arch-layered #pattern-mvc #arch-aop

## 概述
UMLmyself 是一个基于 Spring Boot 3.2 的毕业论文管理系统，采用经典的分层架构，支持多角色协同工作，覆盖论文从选题到答辩的完整生命周期。

## 架构图

```text
┌─────────────────────────────────────────────────────┐
│                    前端 (SPA)                        │
│           login_page.html / management_system.html  │
│           Tailwind CSS + Vanilla JS                  │
└──────────────────────┬──────────────────────────────┘
                       │ HTTP (JSON) + JWT Bearer Token
┌──────────────────────▼──────────────────────────────┐
│                  Controller 层                       │
│  LoginController  AdminController  UserController   │
│  StudentThesisController  SupervisorThesisController│
│  ReviewerThesisController  NoticeController  ...    │
│  → 接收请求, 参数校验(@Validated), 调用Service       │
│  → 返回 ApiResponse<T> 统一格式                      │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              AOP 切面 (横切关注点)                     │
│  RoleCheckAspect: 拦截 @RequireRole → JWT验证+鉴权    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                   Service 层                         │
│  UserServiceImpl  AdminServiceImpl                   │
│  StudentThesisServiceImpl  SupervisorThesisServiceImpl│
│  → 业务逻辑, 事务管理, DTO转换                        │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                 Repository 层                        │
│  UserRepository  RoleRepository  ThesisProcessRepo  │
│  → Spring Data JPA 接口, 数据访问抽象                 │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│                   数据层                             │
│  ┌─────────┐  ┌──────────┐  ┌────────────────┐     │
│  │  MySQL  │  │  Redis   │  │  File System   │     │
│  │  主库   │  │ 验证码   │  │  uploads/ 目录  │     │
│  └─────────┘  └──────────┘  └────────────────┘     │
└─────────────────────────────────────────────────────┘
```

## 技术选型理由
| 技术 | 用途 | 选型原因 |
|------|------|----------|
| Spring Boot 3.2 | 应用框架 | 快速启动、自动配置、生态成熟 |
| Spring Data JPA | 数据访问 | 减少样板代码、自动查询生成 |
| MySQL | 主存储 | 关系型数据、事务支持 |
| Redis | 缓存 | 验证码5分钟TTL、高性能读写 |
| JWT (jjwt 0.12) | 认证 | 无状态、适合前后端分离 |
| BCrypt | 密码加密 | 自适应哈希、抗暴力破解 |
| Spring AOP | 权限切面 | 解耦权限逻辑、声明式注解 |
| Swagger/OpenAPI | API文档 | 自动生成、交互式测试 |
| Lombok | 代码简化 | 减少getter/setter样板 |

## 模块边界

```text
com.sxy.umlmyself
├── aspect/           ← AOP 切面（权限拦截）
├── common/           ← 通用组件（响应、异常、注解）
├── config/           ← Spring 配置（CORS、OpenAPI、类型转换）
├── controller/       ← REST 控制器
├── converter/        ← 类型转换器
├── dto/              ← 数据传输对象
├── entity/           ← JPA 实体
├── enums/            ← 枚举类型
├── repository/       ← 数据访问接口
├── service/          ← 业务接口 + impl/ 实现
└── util/             ← 工具类（JWT等）
```

## 外部依赖
| 系统 | 用途 | 配置 |
|------|------|------|
| MySQL | 持久化存储 | `spring.datasource.*` |
| Redis | 短信验证码缓存(TTL 5分钟) | `spring.data.redis.*` |
| 短信网关 | 发送验证码(PasswordService调用) | 代码中硬编码 |

## 相关笔记
- [[Request-Flow]]
- [[JWT-Authorization]]
- [[Infrastructure]]
- [[Data-Model]]
