---
module: common
path: 03-Common
keywords: ApiResponse, BusinessException, GlobalExceptionHandler, RequireRole, validation
---

# 公共设施 (重要性: ★★★)

#module-common #pattern-uniform-response #config-validation

## 概述
公共设施层提供贯穿所有模块的基础能力：统一的 API 响应格式、业务异常体系、全局异常处理、以及参数校验支持。理解这一层是理解整个项目 API 约定的前提。

## 关键文件
| 文件 | 作用 |
|------|------|
| `common/ApiResponse.java` | 统一响应封装，所有接口返回此类型 |
| `common/BusinessException.java` | 业务异常，由 GlobalExceptionHandler 统一处理 |
| `common/GlobalExceptionHandler.java` | @RestControllerAdvice 全局异常处理器 |
| `common/RequireRole.java` | 权限注解（配合 RoleCheckAspect） |
| `config/CorsConfig.java` | CORS 跨域配置 |
| `config/WebConfig.java` | 类型转换器注册 (StringToMaterialTypeConverter) |
| `config/OpenApiConfig.java` | Swagger/OpenAPI 文档配置 |

## 公共接口
| 导出 | 类型 | 描述 |
|------|------|------|
| `ApiResponse<T>` | 泛型类 | 统一响应: `{code, message, data}` |
| `ApiResponse.success()` | 静态方法 | 成功响应(无数据) |
| `ApiResponse.success(T data)` | 静态方法 | 成功响应(带数据) |
| `ApiResponse.success(String msg, T data)` | 静态方法 | 成功响应(自定义消息) |
| `ApiResponse.error(int code, String msg)` | 静态方法 | 失败响应(指定状态码) |
| `ApiResponse.error(String msg)` | 静态方法 | 失败响应(默认400) |
| `BusinessException` | 异常类 | 携带 code 和 message 的业务异常 |
| `GlobalExceptionHandler` | 切面类 | 统一拦截5种异常类型 |

## 内部流程

### 异常处理流程
```text
Controller / Service 抛出异常
  │
  ▼
GlobalExceptionHandler (@RestControllerAdvice)
  │
  ├─ BusinessException        → 400 + ApiResponse.error(code, msg)
  ├─ MethodArgumentNotValid   → 400 + "参数校验失败: {field: error}"
  ├─ BindException            → 400 + "参数绑定失败: {field: error}"
  ├─ IllegalArgumentException → 400 + ApiResponse.error(msg)
  └─ Exception (兜底)         → 500 + "系统内部错误: msg"
```

### 统一响应格式
```json
// 成功
{"code": 200, "message": "操作成功", "data": {...}}

// 失败
{"code": 400, "message": "密码错误", "data": null}

// 系统错误
{"code": 500, "message": "系统内部错误: ...", "data": null}
```

> [!important] 所有 Controller 必须返回 ApiResponse
> 这是前端约定。前端 JavaScript 通过 `response.code === 200` 判断成功与否。不一致的响应格式会破坏前端逻辑。

### CORS 配置
```text
CorsConfig: 允许所有来源、所有方法、所有头、支持凭证
→ 开发环境友好，生产环境需收紧
```

### 参数校验链
```text
@Validated (Controller) 
  → DTO 字段上的 @NotNull/@NotBlank/@Size 等
  → 校验失败 → MethodArgumentNotValidException
  → GlobalExceptionHandler 捕获 → 返回字段级错误信息
```

## 依赖
| 方向 | 模块/服务 | 方式 |
|------|-----------|------|
| **被使用** | 所有 Controller | 返回 ApiResponse |
| **被使用** | 所有 Service | 抛出 BusinessException |
| **被使用** | 所有 @RequireRole 方法 | RoleCheckAspect 拦截 |

## 配置
| 配置项 | 文件 | 说明 |
|--------|------|------|
| CORS 策略 | `CorsConfig.java` | 全局开放，生产需收紧 |
| 类型转换 | `WebConfig.java` | String → MaterialType 转换器 |
| OpenAPI | `OpenApiConfig.java` + `application.properties` | Swagger UI 路径 |

## 相关笔记
- [[JWT-Authorization]]
- [[System-Architecture]]
- [[API-Surface]]
