# 毕业设计管理系统

基于 Spring Boot 3.2 的毕业设计全流程管理系统，支持多角色协作与 JWT 认证授权。

## 技术栈

- **后端**: Java 21, Spring Boot 3.2, Spring Data JPA
- **数据库**: MySQL, Redis（可选，验证码缓存）
- **前端**: 原生 HTML/CSS/JS，Tailwind CSS (CDN)
- **认证**: JWT（HS256），支持多角色登录

## 快速开始

### 环境要求

- Java 21+
- MySQL 5.7+
- Maven 3.6+

### 数据库配置

1. 创建数据库 `graduation_thesis_system_myself_b`
2. 执行 `src/main/resources/db/migration/` 下的 SQL 脚本

### 运行

```bash
./mvnw spring-boot:run
```

默认访问地址: http://localhost:8080

### 配置文件

编辑 `src/main/resources/application.properties`:

- `spring.datasource.url` — 数据库连接
- `jwt.secret` — JWT 签名密钥
- `spring.data.redis.host` — Redis 地址（可选）

## 项目结构

```
src/main/java/com/sxy/umlmyself/
├── aspect/        # AOP 切面（角色权限校验）
├── common/        # 公共类（响应包装、异常处理、注解）
├── config/        # 配置类（CORS、Swagger、类型转换）
├── controller/    # REST 接口层
├── converter/     # 类型转换器
├── dto/           # 数据传输对象
├── entity/        # JPA 实体
├── enums/         # 枚举类型
├── repository/    # 数据访问层
├── service/       # 业务逻辑层
└── util/          # 工具类（JWT 等）
```

## 功能模块

- 多角色认证授权（学生、指导教师、评阅教师、答辩组、专业负责人、学院领导、管理员）
- 毕业论文全流程管理（选题、开题、中期、评审、答辩）
- 通知公告、成绩管理、文件上传
- Swagger API 文档: `/swagger-ui.html`
