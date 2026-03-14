# 金融风控管理系统

这是一个同仓前后端项目，当前已经能跑通一条完整的风控演示闭环：

- 真实后端用户、认证与操作日志持久化
- 真实后端风险数据 CRUD
- 真实后端风险评估
- 真实后端预警生成与处理
- 真实后端仪表盘与统计接口
- 真实后端用户管理、操作日志、指标规则管理
- AI 助手禁用占位接口与页面入口
- 前端 Vue 3 管理台展示

## 仓库结构

- `backend/`
  Spring Boot 后端工程
- `frontend/`
  Vue 3 + Vite 前端工程
- `docs/codex/`
  当前项目的 Codex 执行文档
- `项目文档/`
  原始项目资料，保留为参考

## 当前运行事实

- 用户、认证、角色、操作日志已经接入数据库持久化实现
- 风险数据、评估、预警、统计、指标规则都已接入真实后端数据库闭环
- 默认前端环境直接走真实后端，mock 仅作为独立回退环境保留
- 默认真实链路下，核心业务模块不再依赖内存态演示存储；仅前端 mock 环境保留本地回退数据
- `/api/assistant/*` 已提供占位接口，当前统一返回 `50100 / AI assistant is disabled in V1`
- `backend/src/main/resources/db/` 下的 SQL 已用于当前运行态初始化，登录/退出/失败日志也会真实落库
- `MySQL` 用于本地运行，`H2` 仅用于后端测试与 CI

## 启动方式

推荐启动顺序：

1. 启动 MySQL
2. 启动 `backend`
3. 启动 `frontend`

### 1. 启动后端

```powershell
cd E:\Project\fengxian031117\backend
.\mvnw.cmd spring-boot:run
```

默认端口：`8080`

默认会连接本地 MySQL：

- 数据库名：`financial_risk_control`
- 用户名：`root`
- 密码：`root`

也可以通过环境变量覆盖：

- `APP_DB_URL`
- `APP_DB_USERNAME`
- `APP_DB_PASSWORD`

### 2. 启动前端

```powershell
cd E:\Project\fengxian031117\frontend
npm install
npm run dev
```

默认开发地址通常为：`http://localhost:5173`

如需启动独立 mock 回退环境：

```powershell
cd E:\Project\fengxian031117\frontend
npm run dev:mock
```

## 本地验证命令

- 后端测试：`cd backend && .\mvnw.cmd clean test`
- 后端启动：`cd backend && .\mvnw.cmd spring-boot:run`
- 前端真实构建：`cd frontend && npm run build`
- 前端 Mock 构建：`cd frontend && npm run build:mock`
- 前端真实开发：`cd frontend && npm run dev`
- 前端 Mock 开发：`cd frontend && npm run dev:mock`

## 演示账号

三类账号统一密码都是：`demo`

- `risk-demo`
  风控人员，体验完整业务闭环
- `manager-demo`
  管理人员，只读查看预警与统计
- `admin-demo`
  管理员，体验指标规则、用户管理与操作日志

## 推荐体验路径

### 风控人员

1. 登录 `risk-demo`
2. 进入“风险数据”，新增或编辑一条业务
3. 点击“去评估 / 重新评估”
4. 在“风险评估”页执行评估并查看评分详情
5. 进入“预警管理”提交处理意见
6. 回到“仪表盘”或“统计分析”刷新数据，确认结果回流

### 管理人员

1. 登录 `manager-demo`
2. 进入“预警管理”查看预警详情和处理时间线
3. 进入“统计分析”切换筛选条件，确认图表同步变化

### 管理员

1. 登录 `admin-demo`
2. 查看首页管理员职责说明
3. 进入“指标规则”查看指标、权重、评分区间与启停能力
4. 进入“用户管理”验证管理员写操作与当前登录用户保护
5. 进入“操作日志”筛选查看后台写操作记录
6. 进入“AI 助手”页验证占位禁用提示

## AI 助手占位

- 前端提供独立页面入口：`/assistant`
- 后端提供占位接口：`POST /api/assistant/query`、`POST /api/assistant/action`
- 当前 `V1` 统一返回：`50100 / AI assistant is disabled in V1`

## 删除与回收边界

- 风险数据一旦形成评估、预警或处理历史，将拒绝删除，这是当前真实库的明确保护策略
- 指标当前支持新增、编辑、启停与规则维护，不提供物理删除接口；测试型指标请保持停用

## 常见问题

### 登录时报网络错误

先确认 `backend` 是否已经启动，并且监听 `8080` 端口。

### 执行评估时报 400

前端现在会优先显示后端返回的中文业务错误。优先检查：

- 风险数据是否填写了全部启用指标值
- 指标值是否超出当前规则覆盖范围

### 为什么日志现在重启后还在

因为用户、认证和操作日志已经改为数据库持久化，不再依赖内存 `LogStore`。

## 当前回归验证

- `backend`：`./mvnw.cmd clean test`
- `frontend`：`npm run build`
- `frontend mock`：`npm run build:mock`

## 最终演示脚本

- 三角色最终演示脚本与故障排查清单见 `docs/codex/07-demo-walkthrough.md`

## 文档入口

- 项目级执行文档：`docs/codex/00-README.md`
- 前端补充说明：`frontend/README.md`
- 交付检查清单：`docs/codex/08-delivery-checklist.md`

## 下一阶段

- 当前优先方向转为剩余文档口径收敛与业务细节持续打磨
- AI 助手后续如需启用，再在现有 `/api/assistant/*` 占位接口上扩展真实能力
