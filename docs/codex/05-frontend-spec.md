# 前端页面与交互规范

## 1. 前端目标

- 基于 `Vue 3 + Vite + TypeScript` 实现后台管理界面
- 通过 `Pinia` 维护登录态和用户信息
- 通过 `Vue Router` 管理权限菜单和路由守卫
- 通过 `Axios` 调用后端 REST API
- 通过 `Element Plus` 和 `ECharts` 完成表单、表格、图表展示

## 2. 路由结构

```text
/login
/
├─ /dashboard
├─ /system/users
├─ /risk/data
├─ /risk/indexes
├─ /risk/assessments
├─ /risk/warnings
├─ /analysis/statistics
└─ /system/logs
```

## 3. 角色菜单

### `ADMIN`

- 仪表盘
- 用户管理
- 风险指标与规则
- 操作日志

### `RISK_USER`

- 仪表盘
- 风险数据管理
- 风险评估
- 预警管理
- 统计分析

### `MANAGER`

- 仪表盘
- 预警管理
- 统计分析

说明：

- 前端隐藏无权限菜单
- 后端接口鉴权依旧必须生效

## 4. 页面清单

| 页面 | 路由 | 角色 | 主要功能 |
|---|---|---|---|
| 登录页 | `/login` | 全部 | 登录 |
| 仪表盘 | `/dashboard` | 全部 | 查看核心统计卡片 |
| 用户管理 | `/system/users` | `ADMIN` | 用户查询、创建、编辑、停用、删除 |
| 风险数据管理 | `/risk/data` | `RISK_USER` | 主记录与指标值明细 CRUD |
| 风险指标与规则 | `/risk/indexes` | `ADMIN` | 指标管理与规则管理 |
| 风险评估 | `/risk/assessments` | `RISK_USER` | 执行评估、查看结果 |
| 预警管理 | `/risk/warnings` | `RISK_USER`、`MANAGER` | 查看预警、处理预警、查看记录 |
| 统计分析 | `/analysis/statistics` | `RISK_USER`、`MANAGER` | 风险分布、预警趋势、处置汇总 |
| 操作日志 | `/system/logs` | `ADMIN` | 日志查询 |

## 5. 登录页

### 表单字段

- 用户名
- 密码

### 交互

- 提交 `POST /api/auth/login`
- 成功后保存 token 和当前用户信息
- 根据角色跳转默认首页 `/dashboard`
- 失败时展示后端返回信息

### 状态管理

- `userStore.token`
- `userStore.userInfo`
- `userStore.roleCode`

## 6. 仪表盘页

### 统计卡片

- 风险数据总数
- 评估总数
- 预警总数
- 已处理预警数
- 高风险数量

### 数据来源

- `GET /api/statistics/dashboard`

### 图表与组件

- 顶部 5 个统计卡片
- 中部可复用风险等级饼图和预警趋势折线图

### 角色差异

- `ADMIN` 查看系统总览卡片
- `RISK_USER`、`MANAGER` 查看业务总览卡片

## 7. 用户管理页

### 查询区

- 用户名
- 真实姓名
- 状态
- 角色

### 列表列

- 用户名
- 真实姓名
- 手机号
- 角色名称
- 状态
- 创建时间
- 操作

### 操作按钮

- 新增用户
- 编辑用户
- 启用 / 停用
- 删除

### 表单字段

- 用户名
- 登录密码
- 真实姓名
- 手机号
- 角色
- 状态

### 数据来源

- `GET /api/users`
- `GET /api/roles`
- `POST /api/users`
- `PUT /api/users/{id}`
- `PUT /api/users/{id}/status`
- `DELETE /api/users/{id}`

## 8. 风险数据管理页

### 查询区

- 业务编号
- 客户名称
- 业务类型
- 数据状态

### 列表列

- 业务编号
- 客户名称
- 业务类型
- 风险说明
- 数据状态
- 录入人
- 录入时间
- 操作

### 操作按钮

- 新增
- 编辑
- 删除
- 查看详情
- 去评估

### 新增/编辑弹窗结构

#### 主记录字段

- 业务编号
- 客户名称
- 业务类型
- 风险说明

#### 指标值明细区

以表格或动态表单形式展示：

- 指标名称
- 指标编码
- 权重
- 指标值输入框

说明：

- 页面初始化时调用 `GET /api/risk-indexes?status=1`
- 前端按启用指标动态生成指标值输入区域
- 保存时提交 `indexValues`

### 数据来源

- `GET /api/risk-data`
- `GET /api/risk-data/{id}`
- `POST /api/risk-data`
- `PUT /api/risk-data/{id}`
- `DELETE /api/risk-data/{id}`
- `GET /api/risk-indexes?status=1`

## 9. 风险指标与规则页

### 页面结构

- 上半部分：指标列表
- 下半部分：当前选中指标的规则列表

### 指标列表列

- 指标名称
- 指标编码
- 权重
- 状态
- 指标说明
- 操作

### 指标操作

- 新增指标
- 编辑指标
- 启用 / 停用

### 规则列表列

- 最小值
- 最大值
- 评分值
- 建议预警等级
- 操作

### 规则操作

- 新增规则
- 编辑规则
- 删除规则

### 数据来源

- `GET /api/risk-indexes`
- `POST /api/risk-indexes`
- `PUT /api/risk-indexes/{id}`
- `PUT /api/risk-indexes/{id}/status`
- `GET /api/risk-rules?indexId=...`
- `POST /api/risk-rules`
- `PUT /api/risk-rules/{id}`
- `DELETE /api/risk-rules/{id}`

## 10. 风险评估页

### 查询区

- 业务编号
- 风险等级
- 评估状态
- 开始时间
- 结束时间

### 列表列

- 业务编号
- 客户名称
- 总分
- 风险等级
- 评估状态
- 评估时间
- 评估人
- 是否生成预警
- 操作

### 操作按钮

- 执行评估
- 查看详情

### 页面交互

- 从风险数据页点击“去评估”可带上 `riskDataId`
- 执行评估后刷新列表
- 评估详情展示风险说明、指标值、总分、等级、预警信息

### 数据来源

- `GET /api/assessments`
- `GET /api/assessments/{id}`
- `POST /api/assessments/{riskDataId}/execute`

## 11. 预警管理页

### 查询区

- 预警编号
- 预警等级
- 预警状态
- 开始时间
- 结束时间

### 列表列

- 预警编号
- 预警等级
- 预警内容
- 业务编号
- 客户名称
- 预警状态
- 生成时间
- 操作

### `RISK_USER` 可执行操作

- 查看详情
- 提交处理
- 查看处理记录

### `MANAGER` 可执行操作

- 查看详情
- 查看处理记录

### 处理弹窗字段

- 处理意见
- 处理结果
- 下一个状态

### 数据来源

- `GET /api/warnings`
- `GET /api/warnings/{id}`
- `GET /api/warnings/{id}/records`
- `POST /api/warnings/{id}/handle`

## 12. 统计分析页

### 页面组成

- 过滤条件栏
- 风险等级分布图
- 预警趋势图
- 处置情况图
- 汇总数据卡片

### 过滤条件

- 开始时间
- 结束时间
- 风险等级
- 预警状态

### 图表类型

- 风险等级分布：饼图
- 预警趋势：折线图
- 处置情况：柱状图或环形图

### 数据来源

- `GET /api/statistics/risk-level`
- `GET /api/statistics/warning-trend`
- `GET /api/statistics/handle-summary`

## 13. 操作日志页

### 查询区

- 模块名称
- 操作类型
- 操作人
- 开始时间
- 结束时间

### 列表列

- 操作时间
- 操作人
- 模块名称
- 操作类型
- 操作说明

### 数据来源

- `GET /api/logs`
- `GET /api/logs/{id}`

## 14. 前端公共行为

### 路由守卫

- 未登录访问受保护路由时跳转 `/login`
- 已登录访问 `/login` 时跳转 `/dashboard`
- 根据 `roleCode` 过滤动态菜单

### Axios 拦截器

- 请求时自动附加 `Authorization`
- 遇到 `40100` 清空登录态并跳转登录页
- 遇到业务错误弹出统一错误提示

### 分页约定

- 表格列表统一使用后端分页
- 查询条件切换后重置到第一页

### 空态与异常态

- 无数据时显示空态提示
- 统计页无数据时显示空图表占位
- AI 助手入口不出现在 `V1` 菜单中

## 15. 前端实现要点

- 风险数据录入页必须动态加载启用指标并生成指标值输入项。
- 评估、预警、统计 3 个页面的数据链路必须一致。
- 管理人员没有处理预警按钮。
- 所有时间字段使用统一格式展示。
