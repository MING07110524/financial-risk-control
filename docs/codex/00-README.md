# Codex 执行文档包

## 1. 文档定位

本目录是当前项目的 `Codex 权威执行文档`。后续使用 Codex、人工开发、联调、补文档时，应优先遵循本目录内容。

`项目文档/` 目录继续保留，作为原始素材与毕设参考材料，不再作为实现阶段的最终口径。

## 2. 适用范围

- 项目类型：本科毕业设计，金融风控管理系统
- 交付形态：同仓全栈项目
- 默认命名：`financial-risk-control`
- 后端包名：`com.cmj.risk`
- 前端技术栈：`Vue 3 + Vite + TypeScript + Pinia + Vue Router + Element Plus + ECharts`
- 后端技术栈：`Spring Boot + Spring Security + JWT + MyBatis + MySQL + Maven`

## 3. 当前现状与目标现状

### 当前现状

- 仓库已经按同仓全栈结构落地：`backend / frontend / docs / 项目文档`。
- 认证、风险数据 CRUD、风险评估、预警生成与处理、仪表盘、统计分析已经可以联调运行。
- 用户、认证、操作日志已接入 `MyBatis + MySQL` 持久化，演示账号来自数据库初始化数据而不是配置文件。
- `backend/src/main/resources/db/` 下的 SQL 已支撑当前运行态，登录/退出/失败日志与后台操作日志都会真实落库。
- `用户管理`、`操作日志`、`指标规则 CRUD` 已接通真实后端与前端页面。
- 默认前端环境已固定为真实后端模式，mock 仅保留为独立回退环境。
- 默认真实链路下，核心业务模块均为数据库持久化实现；当前仅前端 mock 环境保留本地回退数据。
- `AI 助手` 当前为已落地的占位接口与占位页面，统一返回 `50100` 禁用提示。
- `MySQL` 是默认运行数据库，`H2` 仅用于后端测试和 CI。

### 目标现状

- 仓库按同仓全栈结构组织：`backend / frontend / docs`。
- `docs/codex/` 作为实现阶段唯一权威文档入口。
- 风险评估、预警、统计围绕统一的数据闭环构建：`risk_data -> risk_data_index_value -> risk_assessment -> risk_warning -> warning_handle_record -> statistics`。

## 4. 阅读顺序

1. [00-README.md](/E:/Project/fengxian031117/docs/codex/00-README.md)
2. [01-project-brief.md](/E:/Project/fengxian031117/docs/codex/01-project-brief.md)
3. [02-architecture-and-repo.md](/E:/Project/fengxian031117/docs/codex/02-architecture-and-repo.md)
4. [03-domain-and-database.md](/E:/Project/fengxian031117/docs/codex/03-domain-and-database.md)
5. [04-api-and-backend-spec.md](/E:/Project/fengxian031117/docs/codex/04-api-and-backend-spec.md)
6. [05-frontend-spec.md](/E:/Project/fengxian031117/docs/codex/05-frontend-spec.md)
7. [06-implementation-roadmap.md](/E:/Project/fengxian031117/docs/codex/06-implementation-roadmap.md)
8. [07-demo-walkthrough.md](/E:/Project/fengxian031117/docs/codex/07-demo-walkthrough.md)

## 5. 各文档职责

| 文档 | 作用 | 适用场景 |
|---|---|---|
| `00-README.md` | 说明权威来源、阅读顺序、命名规范、协作规则 | 新会话接管项目、统一口径 |
| `01-project-brief.md` | 提炼项目目标、范围、角色、核心用例和答辩重点 | 开始前理解业务 |
| `02-architecture-and-repo.md` | 固定仓库结构、技术栈、模块边界、分层职责 | 搭脚手架、定目录、定依赖 |
| `03-domain-and-database.md` | 固定领域模型、状态枚举、表结构约束和建模规则 | 建库、写实体、写 SQL |
| `04-api-and-backend-spec.md` | 固定接口、DTO/VO、鉴权、错误码、后端实现边界 | 写 Controller、Service、Mapper |
| `05-frontend-spec.md` | 固定页面、路由、菜单、表单、列表、图表与联调方式 | 写前端页面和状态管理 |
| `06-implementation-roadmap.md` | 固定分阶段开发顺序、完成定义和交付物 | 把任务派给 Codex 或人工执行 |

## 6. 原始资料来源

当前执行文档包主要基于以下资料整理并统一：

- `项目文档/项目立项说明-陈铭杰.md`
- `项目文档/需求规格说明书-陈铭杰.md`
- `项目文档/概要设计说明书-陈铭杰.md`
- `项目文档/核心用例说明-陈铭杰.md`
- `项目文档/SpringBoot项目代码目录与类清单-陈铭杰.md`
- `项目文档/后端接口DTO-VO设计文档-陈铭杰.md`
- `项目文档/database-schema.sql`
- `项目文档/*Mermaid*.md`

## 7. 权威决策

以下内容从本目录开始统一为权威口径：

- 项目采用同仓全栈结构，不再以当前根目录单 `src/Main.java` 工程作为目标形态。
- 认证方案固定为 `JWT`，前后端分离。
- 风险数据新增“指标值明细”建模，表名固定为 `risk_data_index_value`。
- 接口统一返回 `Result<T>` 或 `PageResult<T>`。
- `Entity` 不直接作为接口入参和出参。
- `sys_user_role` 结构保留多角色扩展能力，但 `V1` 业务约束为“一个用户一个角色”。
- `V1` 约束“一次评估最多生成一条预警”。
- 密码必须以 `BCrypt` 哈希存储，禁止明文入库。

## 8. Codex 协作规则

### 实现时默认遵循

- 先读本目录，再动代码。
- 如果本目录与 `项目文档/` 冲突，以本目录为准。
- 如果实现中发现本目录内部冲突，先更新文档，再改代码。
- 如果只是优化文案、不影响实现，可只更新对应文档。

### 新增或修改功能时

- 影响业务目标或范围：更新 `01-project-brief.md`
- 影响目录、技术栈、分层、组件职责：更新 `02-architecture-and-repo.md`
- 影响表结构、字段、状态、评分逻辑：更新 `03-domain-and-database.md`
- 影响接口、DTO/VO、错误码、权限：更新 `04-api-and-backend-spec.md`
- 影响页面、路由、交互、图表：更新 `05-frontend-spec.md`
- 影响开发顺序或阶段拆分：更新 `06-implementation-roadmap.md`

## 9. 实现阶段默认约束

- 文档主语言使用中文，代码标识保持英文。
- `V1` 不实现真实 AI 助手，只保留接口占位与禁用提示。
- `V1` 不引入复杂机器学习模型，只使用规则评分与阈值预警。
- `V1` 不做多租户、消息中心、工作流引擎、附件上传、Excel 导入导出。
- 所有统计结果必须来自真实业务表，不允许前端写死演示数据。

## 10. 快速开始

如果你是新接手该项目的 Codex，会话开始时按下面顺序行动：

1. 阅读本目录 7 份文档。
2. 先对照根级 `README.md` 确认当前运行事实，再按 [06-implementation-roadmap.md](/E:/Project/fengxian031117/docs/codex/06-implementation-roadmap.md) 的阶段顺序实现。
3. 每完成一阶段，回头检查是否需要同步更新本目录。
4. 真库演示与交接时，优先参考 [07-demo-walkthrough.md](/E:/Project/fengxian031117/docs/codex/07-demo-walkthrough.md)。
