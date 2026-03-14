# API 与后端规范

## 1. 后端总原则

- 所有接口统一前缀 `/api`
- 所有响应统一使用 `Result<T>`
- 分页结果统一使用 `Result<PageResult<T>>`
- 所有受保护接口都必须校验 `Authorization: Bearer <token>`
- 所有关键写操作都必须记录 `sys_log`
- `Entity` 不直接作为请求体或响应体

## 2. 公共对象

### `Result<T>`

字段：

- `code`
- `message`
- `data`

成功时固定：

- `code = 0`
- `message = "success"`

### `PageResult<T>`

字段：

- `total`
- `records`

## 3. 公共错误码

| 错误码 | 含义 |
|---|---|
| `0` | 成功 |
| `40000` | 参数错误 |
| `40100` | 未登录或 token 无效 |
| `40300` | 无权限 |
| `40400` | 资源不存在 |
| `40900` | 资源冲突 |
| `50000` | 系统异常 |
| `41001` | 用户名或密码错误 |
| `41002` | 账号已停用 |
| `42001` | 业务编号重复 |
| `42002` | 指标编码重复 |
| `42003` | 指标规则区间冲突 |
| `42004` | 指标值不完整 |
| `42005` | 缺少可用评分规则 |
| `42006` | 预警当前状态不可处理 |
| `50100` | 功能未启用 |

## 4. 鉴权接口

### `POST /api/auth/login`

权限：公开

请求 DTO：`LoginRequestDTO`

```json
{
  "username": "admin",
  "password": "123456"
}
```

响应 VO：`LoginUserVO`

```json
{
  "userId": 1,
  "username": "admin",
  "realName": "系统管理员",
  "roleCode": "ADMIN",
  "roleName": "系统管理员",
  "token": "jwt-token"
}
```

### `POST /api/auth/logout`

权限：已登录

说明：

- `V1` 为无状态登出
- 后端只返回成功
- 前端负责清理 token 与用户状态

### `GET /api/auth/me`

权限：已登录

响应 VO：`CurrentUserVO`

## 5. 用户与角色接口

### `GET /api/users`

权限：`ADMIN`

查询 DTO：`UserQueryDTO`

- `username`
- `realName`
- `status`
- `roleCode`
- `pageNum`
- `pageSize`

返回：`PageResult<UserVO>`

### `GET /api/users/{id}`

权限：`ADMIN`

返回：`UserVO`

### `POST /api/users`

权限：`ADMIN`

请求 DTO：`UserCreateDTO`

字段：

- `username`
- `password`
- `realName`
- `phone`
- `roleIds`

规则：

- 用户名唯一
- `roleIds` 当前必须且只能选择一个有效角色
- 密码入库前转 `BCrypt`
- 创建时插入 `sys_user` 和 `sys_user_role`

### `PUT /api/users/{id}`

权限：`ADMIN`

请求 DTO：`UserUpdateDTO`

字段：

- `username`
- `password`（可选，留空表示不修改）
- `realName`
- `phone`
- `roleIds`

规则：

- 若修改角色，更新 `sys_user_role`
- V1 保持一个用户一条角色映射
- 前端页面语义也应明确为单角色覆盖，而不是多角色叠加

### `PUT /api/users/{id}/status`

权限：`ADMIN`

请求 DTO：

```json
{
  "status": 0
}
```

### `DELETE /api/users/{id}`

权限：`ADMIN`

规则：

- 被业务数据、评估、处置、日志引用时拒绝删除

### `GET /api/roles`

权限：`ADMIN`

说明：

- 用于下拉选择和系统展示
- `V1` 不开放角色新增与修改接口

## 6. 风险数据接口

### DTO 与 VO

#### `RiskDataIndexValueItemDTO`

- `indexId: Long`
- `indexValue: BigDecimal`

#### `RiskDataCreateDTO`

- `businessNo`
- `customerName`
- `businessType`
- `riskDesc`
- `indexValues: List<RiskDataIndexValueItemDTO>`

#### `RiskDataUpdateDTO`

- `customerName`
- `businessType`
- `riskDesc`
- `indexValues: List<RiskDataIndexValueItemDTO>`

#### `RiskDataVO`

- `id`
- `businessNo`
- `customerName`
- `businessType`
- `riskDesc`
- `dataStatus`
- `createBy`
- `createByName`
- `createTime`

#### `RiskDataDetailVO`

- `RiskDataVO` 全字段
- `indexValues: List<RiskDataIndexValueVO>`
- `missingEnabledIndexNames: List<String>`

#### `RiskDataIndexValueVO`

- `indexId`
- `indexCode`
- `indexName`
- `indexValue`
- `weightValue`

### `GET /api/risk-data`

权限：`RISK_USER`

查询 DTO：

- `businessNo`
- `customerName`
- `businessType`
- `dataStatus`
- `pageNum`
- `pageSize`

返回：`PageResult<RiskDataVO>`

### `GET /api/risk-data/{id}`

权限：`RISK_USER`

返回：`RiskDataDetailVO`

### `POST /api/risk-data`

权限：`RISK_USER`

规则：

- 同时写入 `risk_data` 和 `risk_data_index_value`
- `indexValues` 必须覆盖所有启用中的指标
- `businessNo` 唯一
- 创建后 `data_status = 0`

### `PUT /api/risk-data/{id}`

权限：`RISK_USER`

规则：

- 同时更新主记录与指标值明细
- 若该记录已有有效评估，则主记录状态改为 `2`
- 原有效评估记录需在下一次重新评估时失效

### `DELETE /api/risk-data/{id}`

权限：`RISK_USER`

规则：

- 若已有评估、预警或处置链路，V1 推荐逻辑删除或限制删除
- 如果实现物理删除，必须先删除明细并确认无历史依赖

当前实现：

- 一旦已有评估、预警或处置历史，直接拒绝删除并返回冲突错误

## 7. 指标与规则接口

### `GET /api/risk-indexes`

权限：`ADMIN`、`RISK_USER`

查询 DTO：

- `indexName`
- `status`

返回：`List<RiskIndexVO>`

### `POST /api/risk-indexes`

权限：`ADMIN`

请求 DTO：`RiskIndexCreateDTO`

字段：

- `indexName`
- `indexCode`
- `weightValue`
- `indexDesc`
- `status`

规则：

- `indexCode` 唯一
- 启用中的全部指标权重和必须等于 `100`

### `PUT /api/risk-indexes/{id}`

权限：`ADMIN`

请求 DTO：`RiskIndexUpdateDTO`

### `PUT /api/risk-indexes/{id}/status`

权限：`ADMIN`

说明：

- V1 指标推荐停用而不是删除
- 当前实现不提供指标删除接口；测试型指标请保持停用保留

### `GET /api/risk-rules`

权限：`ADMIN`

查询参数：

- `indexId`

返回：`List<RiskRuleVO>`

### `POST /api/risk-rules`

权限：`ADMIN`

请求 DTO：`RiskRuleCreateDTO`

- `indexId`
- `scoreMin`
- `scoreMax`
- `scoreValue`
- `warningLevel`

### `PUT /api/risk-rules/{id}`

权限：`ADMIN`

请求 DTO：`RiskRuleUpdateDTO`

### `DELETE /api/risk-rules/{id}`

权限：`ADMIN`

规则：

- 删除前需确认不会使某个启用指标失去全部规则

## 8. 风险评估接口

### `POST /api/assessments/{riskDataId}/execute`

权限：`RISK_USER`

处理流程：

1. 校验 `risk_data` 存在
2. 加载全部启用指标
3. 加载该条风险数据对应的指标值明细
4. 校验每个启用指标都有明细
5. 加载规则并逐项计算得分
6. 使旧有效评估失效
7. 插入新评估记录
8. 插入评估分项结果快照 `risk_assessment_index_result`
9. 更新 `risk_data.data_status = 1`
10. 根据风险等级决定是否生成预警

返回：`AssessmentDetailVO`

### `GET /api/assessments`

权限：`RISK_USER`

查询 DTO：

- `businessNo`
- `riskLevel`
- `assessmentStatus`
- `startTime`
- `endTime`
- `pageNum`
- `pageSize`

返回：`PageResult<AssessmentVO>`

### `GET /api/assessments/{id}`

权限：`RISK_USER`

返回：`AssessmentDetailVO`

说明：

- `indexResults` 直接读取历史快照，不按最新规则重算

## 9. 预警接口

### `GET /api/warnings`

权限：`RISK_USER`、`MANAGER`

查询 DTO：

- `warningCode`
- `warningLevel`
- `warningStatus`
- `startTime`
- `endTime`
- `pageNum`
- `pageSize`

返回：`PageResult<WarningVO>`

### `GET /api/warnings/{id}`

权限：`RISK_USER`、`MANAGER`

返回：`WarningDetailVO`

### `GET /api/warnings/{id}/records`

权限：`RISK_USER`、`MANAGER`

返回：`List<WarningHandleRecordVO>`

### `POST /api/warnings/{id}/handle`

权限：`RISK_USER`

请求 DTO：`WarningHandleDTO`

- `handleOpinion`
- `handleResult`
- `nextStatus`

`nextStatus` 允许值：

- `1`：处理中
- `2`：已处理

规则：

- 待处理和处理中允许提交处理记录
- 已处理状态不可重复提交
- `handleOpinion` 和 `handleResult` 必填

## 10. 统计接口

### `GET /api/statistics/dashboard`

权限：`ADMIN`、`RISK_USER`、`MANAGER`

返回：`DashboardStatisticsVO`

字段：

- `riskDataCount`
- `assessmentCount`
- `warningCount`
- `handledWarningCount`
- `highRiskCount`

### `GET /api/statistics/risk-level`

权限：`RISK_USER`、`MANAGER`

返回：`List<RiskLevelStatisticsVO>`

### `GET /api/statistics/recent-warnings`

权限：`ADMIN`、`RISK_USER`、`MANAGER`

查询参数：

- `limit`

返回：`List<WarningVO>`

### `GET /api/statistics/warning-trend`

权限：`RISK_USER`、`MANAGER`

返回：`List<WarningTrendStatisticsVO>`

### `GET /api/statistics/handle-summary`

权限：`RISK_USER`、`MANAGER`

返回：`List<HandleSummaryStatisticsVO>`

## 11. 日志接口

### `GET /api/logs`

权限：`ADMIN`

查询 DTO：

- `moduleName`
- `operationType`
- `operator`
- `startTime`
- `endTime`
- `pageNum`
- `pageSize`

返回：`PageResult<LogVO>`

说明：

- 当前仅提供分页查询接口，不提供 `GET /api/logs/{id}`

## 12. AI 占位接口

### `POST /api/assistant/query`

### `POST /api/assistant/action`

权限：已登录

统一行为：

- 返回 `50100`
- `message = "AI assistant is disabled in V1"`

## 13. 后端主要类清单

### Controller

- `AuthController`
- `UserController`
- `RiskDataController`
- `RiskIndexController`
- `AssessmentController`
- `WarningController`
- `StatisticsController`
- `LogController`
- `AssistantController`

### Service

- `AuthService`
- `UserService`
- `RiskDataService`
- `RiskIndexService`
- `AssessmentService`
- `WarningService`
- `StatisticsService`
- `LogService`

### Component

- `RiskDemoStore`
- `RiskWorkflowStore`
- `JwtTokenProvider`

## 14. 必须记录日志的操作

- 登录成功
- 登录失败
- 用户新增、修改、停用、删除
- 风险数据新增、修改、删除
- 指标新增、修改、启用、停用
- 规则新增、修改、删除
- 风险评估执行
- 预警处理
- 退出登录

## 15. 后端实现顺序

1. `common / exception / enums / security`
2. `auth + user`
3. `risk_data + risk_data_index_value`
4. `risk_index + risk_rule`
5. `assessment + warning`
6. `statistics + log`
7. `assistant placeholder`
