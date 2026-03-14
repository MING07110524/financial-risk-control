# 架构与仓库规范

## 1. 目标仓库结构

项目采用 `同仓前后端` 结构，目标目录如下：

```text
financial-risk-control
├─ backend
│  ├─ pom.xml
│  └─ src
│     ├─ main
│     │  ├─ java/com/cmj/risk
│     │  └─ resources
│     └─ test
├─ frontend
│  ├─ package.json
│  ├─ vite.config.ts
│  └─ src
├─ docs
│  └─ codex
└─ 项目文档
```

说明：

- `docs/codex/` 是执行文档。
- `项目文档/` 是原始资料。
- 当前根目录旧 `src/Main.java` 视为历史空骨架，不作为目标结构的一部分。

## 2. 技术栈

| 层级 | 技术 | 说明 |
|---|---|---|
| 前端 | Vue 3 | 组合式 API |
| 前端工程 | Vite | 开发与构建 |
| 前端语言 | TypeScript | 提升类型约束 |
| 状态管理 | Pinia | 管理登录状态、菜单、缓存条件 |
| 路由 | Vue Router | 权限路由与页面组织 |
| HTTP | Axios | 与后端 RESTful API 交互 |
| UI | Element Plus | 后台管理页面组件 |
| 图表 | ECharts | 统计分析图表展示 |
| 后端 | Spring Boot | Web 应用基础框架 |
| 安全 | Spring Security + JWT | 登录认证与接口鉴权 |
| ORM | MyBatis | SQL 显式可控 |
| 数据库 | MySQL 8 | 持久化存储 |
| 构建 | Maven | 后端依赖管理 |

## 3. 后端分层

### 控制层 `controller`

- 接收请求
- 参数校验
- 调用 `service`
- 返回统一 `Result<T>`

### 业务层 `service` / `service.impl`

- 实现业务规则
- 完成权限内业务校验
- 协调 `mapper` 与组件
- 触发日志记录

### 数据访问层 `mapper`

- 执行数据库增删改查
- 提供统计查询
- 保持接口命名与业务含义一致

### 领域对象层

- `entity` 对应数据库结构
- `dto` 接收入参
- `vo` 返回展示对象
- `enums` 承载固定业务值

### 通用能力层

- `common`：统一返回、分页、错误码、常量
- `config`：MyBatis、跨域、JSON、Security 配置
- `exception`：业务异常与全局异常处理
- `component`：评分计算、预警生成、日志记录
- `security`：JWT 解析、认证上下文、访问控制

## 4. 后端推荐包结构

```text
com.cmj.risk
├─ FinancialRiskControlApplication
├─ controller
├─ service
├─ service/impl
├─ mapper
├─ entity
├─ dto
├─ vo
├─ common
├─ config
├─ exception
├─ enums
├─ utils
├─ component
└─ security
```

`security` 包至少包含：

- `SecurityConfig`
- `JwtTokenProvider`
- `JwtAuthenticationFilter`
- `CustomUserDetailsService`
- `SecurityUser`

## 5. 前端推荐目录

```text
frontend/src
├─ api
├─ assets
├─ components
├─ layout
├─ router
├─ stores
├─ types
├─ utils
└─ views
```

### 目录职责

- `api`：按模块拆分请求函数
- `layout`：后台布局、侧边栏、顶栏
- `router`：常量路由、权限路由守卫
- `stores`：用户、字典、查询条件缓存
- `types`：接口返回类型、页面对象类型
- `views`：按业务页面拆分

## 6. 认证与授权方案

### 认证

- 登录成功后由后端签发 `JWT access token`
- Token 由前端保存在内存与本地持久层
- 前端后续请求统一通过 `Authorization: Bearer <token>` 传递

### JWT 载荷

- `userId`
- `username`
- `roleCode`

### V1 默认策略

- 仅实现 `access token`
- 默认有效期 `2 小时`
- `logout` 为客户端主动清除 token，后端返回成功占位
- 不实现刷新 token、黑名单、单点登录

### 授权

- 由 `Spring Security` 完成接口拦截
- 以 `roleCode` 做接口级权限控制
- 前端基于 `roleCode` 控制菜单和按钮显示
- 后端权限判断是最终准绳，前端权限控制只做体验优化

## 7. 接口统一约定

### 路径

- 所有业务接口统一前缀：`/api`
- AI 占位接口统一前缀：`/api/assistant`

### 返回格式

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

分页返回：

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "total": 0,
    "records": []
  }
}
```

### 时间格式

- 接口对外统一使用字符串
- 默认格式：`yyyy-MM-dd HH:mm:ss`

### 分页

- 查询 DTO 统一使用 `pageNum`、`pageSize`
- 后端分页返回统一使用 `PageResult<T>`

## 8. 模块边界

| 模块 | 职责 | 不负责 |
|---|---|---|
| 认证模块 | 登录、当前用户信息、JWT 生成与解析 | 用户 CRUD |
| 用户模块 | 用户增删改查、状态管理、角色分配 | 风险评估 |
| 指标模块 | 指标定义、权重管理 | 业务数据录入 |
| 规则模块 | 区间规则维护、评分阈值 | 直接生成统计图 |
| 风险数据模块 | 风险主记录和指标值明细管理 | 自动生成评估 |
| 评估模块 | 汇总指标值并计算总分、风险等级 | 手工处理预警 |
| 预警模块 | 根据评估结果生成并处理预警 | 修改评估逻辑 |
| 统计模块 | 仪表盘与统计查询 | 修改底层业务数据 |
| 日志模块 | 记录关键操作、分页查询 | 审批与通知 |

## 9. 关键组件职责

### `RiskScoreCalculator`

- 输入：启用指标、对应规则、风险数据指标值
- 输出：总分、风险等级、每项得分明细

### `WarningGenerator`

- 输入：评估结果
- 输出：是否生成预警、预警等级、预警内容

### `LogRecorder`

- 输入：操作人、模块、动作、描述
- 输出：写入 `sys_log`

## 10. 架构约束

- Controller 不直接操作 Mapper。
- Service 不直接返回 Entity 给前端。
- 所有新增、修改、删除、执行评估、处理预警操作都要记录日志。
- 风险评估只能基于数据库中已保存的指标值明细执行。
- 统计模块只读，不允许写业务主表。
- AI 助手接口只返回“功能未启用”。
