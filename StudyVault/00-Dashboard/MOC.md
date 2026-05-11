---
module: dashboard
path: 00-Dashboard
keywords: MOC, onboarding, graduation-thesis, spring-boot, UMLmyself
---

# UMLmyself — 毕业论文管理系统入门地图

#dashboard #onboarding

## 架构概览
- 模式: 分层架构 (Controller → Service → Repository → Entity) + AOP 横切面
- 技术栈: Spring Boot 3.2, Java 21, Spring Data JPA, MySQL, Redis, JWT, BCrypt
- → [[System-Architecture]]
- → [[Request-Flow]]

## 模块地图
| 模块 | 用途 | 关键入口 | 笔记 |
|------|------|----------|------|
| 认证/登录 | 2阶段登录 + JWT 令牌签发 | `LoginController` | [[JWT-Authorization]] |
| 权限控制 | AOP 切面 + @RequireRole 注解拦截 | `RoleCheckAspect` | [[JWT-Authorization]] |
| 公共设施 | 统一响应、异常处理、工具类 | `common/`, `util/` | [[Infrastructure]] |
| 学生论文 | 材料上传、流程查看 | `StudentThesisController` | [[API-Surface]] |
| 指导教师 | 材料审核、评分 | `SupervisorThesisController` | [[API-Surface]] |
| 评阅教师 | 论文评阅、评分统计 | `ReviewerThesisController` | [[API-Surface]] |
| 专业负责人 | 选题/中期审批、成绩查看 | `MajorLeaderThesisController` | [[API-Surface]] |
| 学院负责人 | 进度监控 | `CollegeLeaderThesisController` | [[API-Surface]] |
| 答辩小组 | 答辩评分 | `DefenseTeamThesisController` | [[API-Surface]] |
| 管理员 | 用户管理、归档导出 | `AdminController` | [[API-Surface]] |
| 通知公告 | 公开+管理 | `NoticeController` | [[API-Surface]] |
| 密码管理 | 短信验证码 + 重置密码 | `PasswordController` | [[API-Surface]] |
| 业务逻辑 | Service 层实现 | `service/impl/` | [[Business-Logic]] |
| 数据模型 | 实体与关联关系 | `entity/` | [[Data-Model]] |

## API 接口全景
| 方法 | 路径 | 角色 | 说明 |
|------|------|------|------|
| POST | `/login` | 公开 | 阶段一：验证凭证，返回可用角色 |
| POST | `/login/confirm-role` | 公开 | 阶段二：确认角色，返回 JWT |
| GET | `/api/user/profile` | 登录用户 | 获取个人信息 |
| PUT | `/api/user/profile` | 登录用户 | 更新个人信息 |
| GET/POST/PUT/DELETE | `/api/admin/**` | ADMIN | 用户 CRUD + 归档 |
| POST/GET | `/api/student/thesis/**` | STUDENT | 材料上传、流程查看 |
| POST/GET | `/api/supervisor/thesis/**` | SUPERVISOR | 审核、评分 |
| POST/GET | `/api/reviewer/thesis/**` | REVIEWER | 评阅、评分 |
| POST/GET | `/api/major-leader/thesis/**` | MAJOR_LEADER | 选题审批、中期报告 |
| GET | `/api/college-leader/thesis/**` | COLLEGE_LEADER | 进度监控 |
| POST/GET | `/api/defense-team/thesis/**` | DEFENSE_MEMBER/LEADER | 答辩评分 |
| GET/POST/PUT/DELETE | `/api/notice/**` | 公开+ADMIN | 通知公告 |
| POST | `/password/**` | 公开 | 验证码发送/校验/重置 |

## 快速开始
1. 环境: Java 21, MySQL 5.7+, Maven 3.6+ (可选 Redis)
2. 配置: 修改 `application.properties` 中的数据库连接、JWT密钥
3. 建表: 执行 `src/main/resources/db/` 下的 SQL 脚本
4. 启动: `./mvnw spring-boot:run`
5. 访问: `http://localhost:8080/login_page.html`
6. API文档: `http://localhost:8080/swagger-ui.html`

## 标签索引
| 标签 | 描述 | 规则 |
|------|------|------|
| `#arch-*` | 架构概念 | 顶层模式标签 |
| `#module-*` | 模块标识 | 每个模块一个 |
| `#pattern-*` | 设计模式 | 分层、AOP等 |
| `#api-*` | API 接口 | 按角色分组 |
| `#config-*` | 配置相关 | 配置文件、环境变量 |
| `#test-*` | 测试相关 | 测试策略 |

## 入门阅读路径
> 推荐新开发者按以下顺序阅读：

1. [[System-Architecture]] — 系统全景
2. [[Request-Flow]] — 请求如何贯穿系统
3. [[JWT-Authorization]] — 认证授权机制
4. [[Infrastructure]] — 公共设施（ApiResponse、异常处理、AOP）
5. [[Data-Model]] — 数据模型与表关系
6. [[API-Surface]] — 所有接口一览
7. [[Business-Logic]] — 核心业务逻辑
8. [[Onboarding-Exercises]] — 动手练习
