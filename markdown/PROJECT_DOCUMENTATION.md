# 毕业论文管理系统 - 项目详细说明文档

## 📋 目录

1. [项目概述](#项目概述)
2. [技术栈](#技术栈)
3. [项目结构](#项目结构)
4. [数据库设计](#数据库设计)
5. [后端架构](#后端架构)
6. [前端页面](#前端页面)
7. [API接口文档](#api接口文档)
8. [配置说明](#配置说明)
9. [功能说明](#功能说明)
10. [部署说明](#部署说明)

---

## 项目概述

**项目名称**: 毕业论文管理系统 (UMLmyself)

**项目描述**: 基于 Spring Boot 的毕业论文管理系统，支持多角色登录、密码找回等功能。

**开发环境**:
- Java 21
- Spring Boot 4.0.1
- MySQL 5.7.26
- Redis (用于验证码存储)

---

## 技术栈

### 后端技术

| 技术 | 版本 | 用途 |
|------|------|------|
| Spring Boot | 4.0.1 | 核心框架 |
| Spring Data JPA | - | 数据访问层 |
| Spring Web MVC | - | Web 框架 |
| MySQL Connector | - | 数据库驱动 |
| JWT (jjwt) | 0.12.3 | 身份认证 |
| Spring Security Crypto | - | 密码加密 (BCrypt) |
| Spring Data Redis | - | Redis 集成 |
| Lombok | - | 代码简化 |

### 前端技术

| 技术 | 用途 |
|------|------|
| HTML5 | 页面结构 |
| CSS3 / Tailwind CSS | 样式设计 |
| JavaScript (ES6+) | 交互逻辑 |
| Fetch API | HTTP 请求 |

---

## 项目结构

```
UMLmyself/
├── pom.xml                          # Maven 配置文件
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/sxy/umlmyself/
│   │   │       ├── UmLmyselfApplication.java    # 启动类
│   │   │       ├── common/                      # 公共类
│   │   │       │   └── ApiResponse.java         # 统一响应类
│   │   │       ├── config/                      # 配置类
│   │   │       │   └── CorsConfig.java          # 跨域配置
│   │   │       ├── controller/                  # 控制器层
│   │   │       │   ├── LoginController.java     # 登录控制器
│   │   │       │   └── PasswordController.java  # 密码管理控制器
│   │   │       ├── dto/                         # 数据传输对象
│   │   │       │   ├── LoginRequestDTO.java
│   │   │       │   ├── LoginResponseDTO.java
│   │   │       │   ├── ConfirmRoleRequestDTO.java
│   │   │       │   ├── ConfirmRoleResponseDTO.java
│   │   │       │   ├── RoleDTO.java
│   │   │       │   ├── SendSmsCodeRequestDTO.java
│   │   │       │   ├── VerifyCodeRequestDTO.java
│   │   │       │   └── ResetPasswordRequestDTO.java
│   │   │       ├── entity/                      # 实体类
│   │   │       │   ├── User.java
│   │   │       │   ├── Role.java
│   │   │       │   ├── UserRole.java
│   │   │       │   ├── College.java
│   │   │       │   └── Major.java
│   │   │       ├── repository/                  # 数据访问层
│   │   │       │   ├── UserRepository.java
│   │   │       │   ├── RoleRepository.java
│   │   │       │   └── UserRoleRepository.java
│   │   │       ├── service/                     # 服务层
│   │   │       │   ├── UserService.java
│   │   │       │   ├── PasswordService.java
│   │   │       │   └── impl/
│   │   │       │       ├── UserServiceImpl.java
│   │   │       │       └── PasswordServiceImpl.java
│   │   │       └── util/                        # 工具类
│   │   │           └── JwtUtil.java             # JWT 工具类
│   │   └── resources/
│   │       ├── application.properties           # 应用配置
│   │       └── static/                         # 静态资源
│   │           ├── login_page.html              # 登录页面
│   │           ├── management_system.html        # 管理系统页面
│   │           └── db/                          # 数据库脚本
│   │               ├── user.sql
│   │               ├── role.sql
│   │               ├── user_role.sql
│   │               ├── college.sql
│   │               └── major.sql
│   └── test/                                    # 测试代码
└── target/                                      # 编译输出
```

---

## 数据库设计

### 数据库名称
`graduation_thesis_system_myself`

### 数据表结构

#### 1. user 表（用户表）

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| user_id | INT | 用户ID | 主键，自增 |
| username | VARCHAR(50) | 用户名 | 唯一，非空 |
| password | VARCHAR(255) | 密码（BCrypt加密） | 非空 |
| real_name | VARCHAR(50) | 真实姓名 | - |
| phone | VARCHAR(20) | 手机号 | 唯一 |
| email | VARCHAR(100) | 邮箱 | - |
| major_id | INT | 专业ID | 外键 |
| college_id | INT | 学院ID | 外键 |
| status | INT | 账号状态 | 1=正常，0=禁用 |

**索引**:
- PRIMARY KEY (user_id)
- UNIQUE KEY uk_user_username (username)
- UNIQUE KEY uk_user_phone (phone)
- INDEX fk_user_college (college_id)
- INDEX fk_user_major (major_id)

#### 2. role 表（角色表）

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| role_id | INT | 角色ID | 主键，自增 |
| role_code | VARCHAR(50) | 角色代码 | 唯一 |
| role_name | VARCHAR(50) | 角色名称 | - |

**角色数据**:
- STUDENT (学生)
- SUPERVISOR (指导老师)
- MAJOR_LEADER (专业负责人)
- COLLEGE_LEADER (二级学院领导)
- REVIEWER (评阅老师)
- ADMIN (管理员)
- DEFENSE_LEADER (答辩小组组长)
- DEFENSE_MEMBER (答辩小组组员)

#### 3. user_role 表（用户角色关联表）

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| user_role_id | INT | 主键ID | 主键，自增 |
| user_id | INT | 用户ID | 外键，非空 |
| role_id | INT | 角色ID | 外键，非空 |

**索引**:
- PRIMARY KEY (user_role_id)
- UNIQUE KEY uk_user_role (user_id, role_id)
- INDEX idx_user_role_user (user_id)
- INDEX idx_user_role_role (role_id)

#### 4. college 表（学院表）

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| college_id | INT | 学院ID | 主键，自增 |
| college_name | VARCHAR(100) | 学院名称 | - |
| college_code | VARCHAR(50) | 学院代码 | 唯一 |

#### 5. major 表（专业表）

| 字段名 | 类型 | 说明 | 约束 |
|--------|------|------|------|
| major_id | INT | 专业ID | 主键，自增 |
| major_name | VARCHAR(100) | 专业名称 | - |
| major_code | VARCHAR(50) | 专业代码 | - |
| college_id | INT | 学院ID | 外键 |

### 表关系图

```
user ──┬── user_role ──┬── role
       │               │
       ├── college     │
       │               │
       └── major       │
```

---

## 后端架构

### 架构分层

项目采用**五层架构**设计：

1. **Entity 层** - 实体类，对应数据库表
2. **DTO 层** - 数据传输对象，用于请求和响应
3. **Repository 层** - 数据访问层，使用 Spring Data JPA
4. **Service 层** - 业务逻辑层
5. **Controller 层** - 控制器层，处理 HTTP 请求

### 核心类说明

#### Entity 层

**User.java** - 用户实体类
- 对应 `user` 表
- 包含用户基本信息字段

**Role.java** - 角色实体类
- 对应 `role` 表
- 包含角色代码和名称

**UserRole.java** - 用户角色关联实体类
- 对应 `user_role` 表
- 实现用户和角色的多对多关系

**College.java** - 学院实体类
- 对应 `college` 表

**Major.java** - 专业实体类
- 对应 `major` 表

#### DTO 层

**LoginRequestDTO** - 登录请求 DTO
- 包含：username, password

**LoginResponseDTO** - 登录响应 DTO（阶段一）
- 包含：userId, username, roles

**ConfirmRoleRequestDTO** - 确认角色请求 DTO
- 包含：userId, roleId

**ConfirmRoleResponseDTO** - 确认角色响应 DTO（阶段二）
- 包含：token, userId, username, roleId, roleCode

**RoleDTO** - 角色信息 DTO
- 包含：roleId, roleCode, roleName

**SendSmsCodeRequestDTO** - 发送验证码请求 DTO
- 包含：phone

**VerifyCodeRequestDTO** - 校验验证码请求 DTO
- 包含：phone, code

**ResetPasswordRequestDTO** - 重置密码请求 DTO
- 包含：phone, newPassword, confirmPassword

#### Repository 层

**UserRepository** - 用户数据访问接口
- `findByUsername(String username)` - 根据用户名查询
- `findByPhone(String phone)` - 根据手机号查询

**RoleRepository** - 角色数据访问接口
- `findRolesByUserId(Integer userId)` - 根据用户ID查询所有角色
- `findByRoleId(Integer roleId)` - 根据角色ID查询

**UserRoleRepository** - 用户角色关联数据访问接口
- `findByUserId(Integer userId)` - 查询用户的所有角色关联
- `findByUserIdAndRoleId(Integer userId, Integer roleId)` - 查询特定关联
- `existsByUserIdAndRoleId(Integer userId, Integer roleId)` - 判断用户是否拥有指定角色

#### Service 层

**UserService** - 用户服务接口
- `login(LoginRequestDTO)` - 用户登录（阶段一）
- `confirmRole(ConfirmRoleRequestDTO)` - 确认角色（阶段二）

**PasswordService** - 密码服务接口
- `sendSmsCode(String phone)` - 发送验证码
- `verifyCode(String phone, String code)` - 校验验证码
- `resetPassword(String phone, String newPassword, String confirmPassword)` - 重置密码

#### Controller 层

**LoginController** - 登录控制器
- `POST /login` - 用户登录接口
- `POST /login/confirm-role` - 确认角色接口

**PasswordController** - 密码管理控制器
- `POST /password/sms-code` - 发送验证码接口
- `POST /password/verify-code` - 校验验证码接口
- `POST /password/reset` - 重置密码接口

#### 工具类

**JwtUtil** - JWT 工具类
- `generateToken(Integer userId, Integer roleId)` - 生成 Token
- `getUserIdFromToken(String token)` - 从 Token 获取用户ID
- `getRoleIdFromToken(String token)` - 从 Token 获取角色ID
- `validateToken(String token)` - 校验 Token 是否有效

**ApiResponse** - 统一响应类
- 提供统一的响应格式：code, message, data
- 静态方法：success(), error()

---

## 前端页面

### 1. login_page.html（登录页面）

**功能模块**:
- 用户登录
- 角色选择
- 忘记密码/重置密码
- 记住我功能

**主要功能**:

1. **登录功能**
   - 用户名/密码登录
   - 记住我（保存用户名到 localStorage）
   - 多角色登录支持

2. **角色选择**
   - 如果用户只有一个角色，自动选择
   - 如果用户有多个角色，显示选择页面

3. **找回密码**
   - 发送验证码（调用 `/password/sms-code`）
   - 校验验证码（调用 `/password/verify-code`）
   - 重置密码（调用 `/password/reset`）

**页面元素**:
- 登录表单（用户名、密码、记住我）
- 角色选择表单
- 忘记密码表单（手机号、验证码）
- 重置密码表单（新密码、确认密码）

### 2. management_system.html（管理系统页面）

**功能模块**:
- 角色门户展示
- 菜单导航
- 用户信息显示

**角色支持**:
- STUDENT (学生)
- TEACHER (指导老师)
- PRO_LEADER (专业负责人)
- DEAN (二级学院领导)
- REVIEWER (评阅老师)
- ADMIN (管理员)
- DEFENSE_MEMBER (答辩小组成员)
- DEFENSE_CHAIRMAN (答辩小组组长)

**角色代码映射**:
- 数据库角色代码 → 前端角色代码
- MAJOR_LEADER → PRO_LEADER
- SUPERVISOR → TEACHER
- COLLEGE_LEADER → DEAN
- DEFENSE_LEADER → DEFENSE_CHAIRMAN

---

## API接口文档

### 登录相关接口

#### 1. 用户登录（阶段一：身份认证）

**接口**: `POST /login`

**请求体**:
```json
{
  "username": "student01",
  "password": "123456"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功，请选择角色",
  "data": {
    "userId": 1,
    "username": "student01",
    "roles": [
      {
        "roleId": 1,
        "roleCode": "STUDENT",
        "roleName": "学生"
      }
    ]
  }
}
```

#### 2. 确认角色（阶段二：生成Token）

**接口**: `POST /login/confirm-role`

**请求体**:
```json
{
  "userId": 1,
  "roleId": 1
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "student01",
    "roleId": 1,
    "roleCode": "STUDENT"
  }
}
```

**响应头**:
```
Authorization: Bearer {token}
```

### 密码管理接口

#### 1. 发送验证码

**接口**: `POST /password/sms-code`

**请求体**:
```json
{
  "phone": "13800000001"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": null
}
```

#### 2. 校验验证码

**接口**: `POST /password/verify-code`

**请求体**:
```json
{
  "phone": "13800000001",
  "code": "123456"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "验证码校验成功",
  "data": null
}
```

#### 3. 重置密码

**接口**: `POST /password/reset`

**请求体**:
```json
{
  "phone": "13800000001",
  "newPassword": "newPassword123",
  "confirmPassword": "newPassword123"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "密码重置成功",
  "data": null
}
```

### 统一响应格式

所有接口都遵循统一的响应格式：

```json
{
  "code": 200,        // 状态码：200=成功，400=业务错误，500=系统错误
  "message": "提示信息",
  "data": {}          // 数据对象，可能为 null
}
```

---

## 配置说明

### application.properties

```properties
# 应用配置
spring.application.name=UMLmyself
server.port=8080

# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/graduation_thesis_system_myself?serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA 配置
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=update

# JWT 配置
jwt.secret=UMLmyselfSecretKeyForJWTTokenGeneration2024
jwt.expiration=604800000  # 7天（毫秒）

# Redis 配置
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=5000ms
```

### 环境要求

1. **Java**: JDK 21 或更高版本
2. **MySQL**: 5.7.26 或更高版本
3. **Redis**: 用于验证码存储（可选，如果不需要找回密码功能）
4. **Maven**: 用于构建项目

---

## 功能说明

### 1. 多角色登录功能

**流程**:
1. 用户输入用户名和密码
2. 后端校验身份，返回用户的所有角色
3. 如果只有一个角色，自动选择
4. 如果有多个角色，显示角色选择页面
5. 用户选择角色后，生成 JWT Token

**特点**:
- 支持一个用户拥有多个角色
- 每次登录可以选择不同的角色
- Token 中包含 userId 和 roleId

### 2. 找回密码功能

**流程**:
1. 用户输入手机号
2. 发送验证码（存储到 Redis，5分钟过期）
3. 用户输入验证码
4. 校验验证码（校验成功后设置标记，10分钟有效）
5. 用户输入新密码和确认密码
6. 重置密码（使用 BCrypt 加密）

**特点**:
- 验证码存储在 Redis 中
- 验证码校验成功后立即删除，防止重复使用
- 密码使用 BCrypt 加密存储

### 3. 记住我功能

**功能**:
- 勾选"记住我"后，保存用户名到 localStorage
- 下次访问时自动填充用户名
- 不存储密码（安全考虑）

### 4. JWT Token 认证

**特点**:
- 使用 HS256 算法
- Token 包含 userId 和 roleId
- 默认有效期 7 天
- Token 通过响应头 `Authorization: Bearer {token}` 返回

---

## 部署说明

### 1. 环境准备

```bash
# 安装 Java 21
# 安装 MySQL 5.7+
# 安装 Redis（可选）
# 安装 Maven
```

### 2. 数据库初始化

```sql
-- 创建数据库
CREATE DATABASE graduation_thesis_system_myself DEFAULT CHARSET=utf8mb4;

-- 执行 SQL 脚本
-- 1. user.sql
-- 2. role.sql
-- 3. user_role.sql
-- 4. college.sql
-- 5. major.sql
```

### 3. 配置文件修改

修改 `application.properties`:
- 数据库连接信息
- Redis 连接信息（如需要）
- JWT Secret Key（生产环境建议使用更复杂的密钥）

### 4. 编译运行

```bash
# 使用 Maven 编译
mvn clean package

# 运行项目
java -jar target/umlmyself-0.0.1-SNAPSHOT.jar

# 或使用 Maven 运行
mvn spring-boot:run
```

### 5. 访问应用

- 前端页面: `http://localhost:8080/login_page.html`
- API 接口: `http://localhost:8080`

---

## 测试数据

### 测试账号

根据数据库中的测试数据，可以使用以下账号进行测试：

| 用户名 | 密码 | 角色 | 手机号 |
|--------|------|------|--------|
| student01 | 123456 | STUDENT | 13800000001 |
| supervisor01 | 123456 | SUPERVISOR, MAJOR_LEADER | 13800000002 |
| majorleader01 | 123456 | MAJOR_LEADER | 13800000003 |
| collegeleader01 | 123456 | COLLEGE_LEADER | 13800000004 |
| reviewer01 | 123456 | REVIEWER | 13800000005 |
| admin01 | 123456 | ADMIN | 13800000006 |
| defenseleader01 | 123456 | DEFENSE_LEADER | 13800000007 |
| defensemember01 | 123456 | DEFENSE_MEMBER | 13800000008 |

**注意**: 所有测试账号的密码都是 `123456`（BCrypt 加密后存储）

---

## 注意事项

1. **安全性**
   - 生产环境请修改 JWT Secret Key
   - 建议使用 HTTPS
   - 密码使用 BCrypt 加密存储

2. **Redis**
   - 找回密码功能需要 Redis 支持
   - 验证码存储在 Redis 中，5分钟过期

3. **数据库**
   - 建议定期备份数据库
   - 生产环境建议关闭 `ddl-auto=update`

4. **前端**
   - 密码不存储在 localStorage（安全考虑）
   - 只存储用户名用于"记住我"功能

---

## 相关文档

- [登录接口文档](./API_DOCUMENTATION_LGOIN.md) - 详细的登录接口说明
- [密码管理接口文档](./PASSWORD_API_DOCUMENTATION.md) - 详细的密码管理接口说明

---

## 更新日志

**2024年** - 初始版本
- 实现多角色登录功能
- 实现找回密码功能
- 实现记住我功能
- 完成前后端集成

---

**文档最后更新时间**: 2024年

