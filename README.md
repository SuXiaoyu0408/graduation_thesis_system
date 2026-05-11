# 毕业设计管理系统

基于 Spring Boot 3.2 的毕业设计全流程管理系统，支持多角色协作与 JWT 认证授权。

## 技术栈

- **后端**: Java 21, Spring Boot 3.2, Spring Data JPA, SpringDoc OpenAPI
- **数据库**: MySQL, Redis（验证码缓存）
- **前端**: 原生 HTML/CSS/JS，Tailwind CSS + Font Awesome (CDN)，SPA 单页架构
- **认证**: JWT（HS256），AOP 切面角色权限校验

## 快速开始

### 环境要求

- Java 21+
- MySQL 5.7+
- Redis（可选，用于验证码缓存）
- Maven 3.6+

### 数据库配置

1. 创建数据库 `graduation_thesis_system_myself_b`
2. 执行 `src/main/resources/db/migration/` 下的 SQL 脚本（推荐按文件名顺序执行）

### 运行

```bash
mvn spring-boot:run
```

默认访问地址: http://localhost:8080

- 登录页面: `/login_page.html`
- 管理系统门户: `/management_system.html`
- Swagger API 文档: `/swagger-ui.html`

### 配置文件

编辑 `src/main/resources/application.properties`:

- `spring.datasource.url` — 数据库连接
- `spring.datasource.username` / `spring.datasource.password` — 数据库账号
- `jwt.secret` — JWT 签名密钥
- `jwt.expiration` — Token 过期时间（默认 7 天）
- `spring.data.redis.*` — Redis 连接配置
- `spring.jpa.hibernate.ddl-auto` — 生产环境建议 `validate`，开发环境可用 `update`

## 项目结构

```
src/main/java/com/sxy/umlmyself/
├── aspect/        # AOP 切面（角色权限校验）
├── common/        # 公共类（响应包装、全局异常处理、@RequireRole 注解）
├── config/        # 配置类（CORS、OpenAPI/Swagger、Web MVC）
├── controller/    # REST 接口层（按角色与功能拆分）
├── converter/     # 类型转换器
├── dto/           # 数据传输对象
├── entity/        # JPA 实体
├── enums/         # 枚举类型（论文状态、成绩等级、材料类型）
├── repository/    # 数据访问层
├── service/       # 业务逻辑接口
│   └── impl/      # 业务逻辑实现
└── util/          # 工具类（JWT 等）

src/main/resources/static/
├── login_page.html           # 登录页面
├── management_system.html    # 管理门户（SPA 主框架）
└── js/
    ├── common.js             # 公共工具函数（HTTP 请求封装等）
    ├── main.js               # 主入口（角色识别、路由、页面加载）
    └── roles/
        ├── admin.js          # 管理员功能（用户管理、专业/学院配置、归档统计）
        ├── student.js        # 学生功能（选题、开题、中期、论文提交）
        ├── teacher.js        # 指导教师功能（选题审批、指导评分、进度监控）
        ├── reviewer.js       # 评阅教师功能（论文评阅、评分）
        ├── defense.js        # 答辩组功能（答辩安排、答辩评分）
        ├── major_leader.js   # 专业负责人功能（流程管理、成绩汇总）
        └── dean.js           # 学院领导功能（总体监控、统计分析）
```

## 功能模块

### 论文全流程管理
- **选题**: 学生提交选题 → 指导教师审批 → 专业负责人审核
- **开题报告**: 提交 → 指导审核
- **中期检查**: 中期报告提交与审核
- **论文评审**: 指导教师评分 + 评阅教师评分（支持多评阅人）
- **答辩**: 答辩组安排 → 答辩评分 → 综合成绩评定

### 多角色协作
| 角色 | 主要功能 |
|------|----------|
| 管理员 (admin) | 用户管理、专业/学院维护、角色分配、归档统计 |
| 学生 (student) | 选题、开题、中期、论文提交、查看成绩 |
| 指导教师 (teacher) | 审批选题、指导评分、进度监控 |
| 评阅教师 (reviewer) | 论文评阅、交叉评分 |
| 答辩组 (defense_team) | 答辩安排、答辩评分 |
| 专业负责人 (major_leader) | 流程审批、成绩汇总、答辩安排 |
| 学院领导 (college_leader) | 总体监控、统计分析 |

### 其他特性
- 文件上传/下载，支持材料历史版本管理
- 通知公告发布
- 验证码登录（Redis 缓存）
- 密码修改与重置
- Swagger API 文档: `/swagger-ui.html`
