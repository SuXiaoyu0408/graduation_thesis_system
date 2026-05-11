---
module: devops
path: 07-DevOps
keywords: build, deploy, maven, mysql, redis, configuration
---

# 构建与部署 (重要性: ★★)

#config-build #config-deploy

## 概述
项目使用 Maven 构建，Spring Boot 内嵌 Tomcat 运行。配置集中在 `application.properties` 一个文件中，无多环境配置分离。

## 关键文件
| 文件 | 作用 |
|------|------|
| `pom.xml` | Maven 依赖管理、构建插件、启动类配置 |
| `src/main/resources/application.properties` | 所有环境配置（无 profile 分离） |
| `src/main/resources/db/` | 数据库建表脚本 |
| `uploads/` | 文件上传目录（应用启动时需确保存在） |

## 构建命令
| 操作 | 命令 |
|------|------|
| 开发启动 | `./mvnw spring-boot:run` |
| 打包 | `./mvnw clean package` |
| 运行 JAR | `java -jar target/umlmyself-0.0.1-SNAPSHOT.jar` |
| 运行测试 | `./mvnw test` |
| 单独测试类 | `./mvnw test -Dtest=ClassName` |

## 环境依赖

### 必需
| 依赖 | 版本要求 | 说明 |
|------|----------|------|
| Java | 21 | `pom.xml` 中 `<java.version>21</java.version>` |
| MySQL | 5.7+ | 数据库 `graduation_thesis_system_myself_b` |
| Maven | 3.6+ | 或使用项目自带 `mvnw` wrapper |

### 可选
| 依赖 | 用途 | 关闭方式 |
|------|------|----------|
| Redis | 短信验证码存储 | 注释 `spring.data.redis.*` 配置 |

## 部署步骤
1. 创建 MySQL 数据库: `CREATE DATABASE graduation_thesis_system_myself_b;`
2. 执行 `src/main/resources/db/` 下的建表脚本
3. 修改 `application.properties`:
   - `spring.datasource.url` → 目标数据库地址
   - `spring.datasource.username/password` → 数据库凭证
   - `jwt.secret` → 生产环境更换密钥
4. 创建 `uploads/` 目录于 JAR 同级目录
5. 构建: `./mvnw clean package`
6. 启动: `java -jar target/umlmyself-0.0.1-SNAPSHOT.jar`

## 配置清单
| 配置项 | 开发默认值 | 说明 |
|--------|-----------|------|
| `server.port` | `8080` | 服务端口 |
| `spring.datasource.url` | `jdbc:mysql://172.20.10.4:3306/...` | 数据库连接（需修改） |
| `spring.datasource.username` | `root` | 数据库用户 |
| `spring.datasource.password` | `123456` | 数据库密码 |
| `spring.jpa.hibernate.ddl-auto` | `validate` | 表结构验证策略 |
| `jwt.secret` | `UMLmyself...2024` | JWT密钥（生产需更换） |
| `jwt.expiration` | `604800000` | Token过期(7天) |
| `spring.data.redis.host` | `localhost` | Redis地址 |
| `spring.data.redis.port` | `6379` | Redis端口 |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | API文档路径 |

> [!warning] 生产环境注意事项
> - 更换 `jwt.secret` 为强密钥
> - 收紧 CORS 配置（`CorsConfig` 当前允许所有来源）
> - 考虑使用 `spring.jpa.hibernate.ddl-auto=none` 
> - 数据库密码不应硬编码在配置文件中（使用环境变量或配置中心）
> - 前端使用 CDN 加载 Tailwind CSS，确保内网可访问或改为本地引入

## 测试
- 框架: JUnit 5 (Spring Boot Starter Test 自带)
- 数据库: H2 (测试时自动启用)
- 当前测试覆盖: 仅基础 `@SpringBootTest` 冒烟测试
- 建议补充: Service 层单元测试、Controller 层集成测试

## 相关笔记
- [[System-Architecture]]
- [[Quick-Reference]]
