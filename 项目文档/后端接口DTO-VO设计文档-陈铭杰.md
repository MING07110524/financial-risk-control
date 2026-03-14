# 后端接口 DTO / VO 设计文档

本文档用于给后续 AI 编程或人工编码提供明确的接口对象设计方案，避免在实现阶段重复定义、命名混乱或直接暴露实体对象。

## 1. 设计原则

### 1.1 DTO 设计原则

- DTO 用于接收前端请求参数
- 不直接复用 Entity，避免数据库字段直接暴露到接口层
- 按模块拆分 DTO，保证职责单一
- 新增、修改、查询条件应使用不同 DTO

### 1.2 VO 设计原则

- VO 用于返回前端展示数据
- 可根据页面需求组合多个字段
- 尽量避免把密码等敏感信息返回前端

## 2. 公共对象设计

### `Result<T>`

统一接口返回对象，建议字段如下：

- `code`：响应码
- `message`：响应信息
- `data`：业务数据

### `PageResult<T>`

分页结果对象，建议字段如下：

- `total`：总条数
- `records`：数据列表

## 3. 认证模块 DTO / VO

### `LoginRequestDTO`

字段：

- `username`：String，用户名
- `password`：String，密码

### `LoginUserVO`

字段：

- `userId`：Long，用户ID
- `username`：String，用户名
- `realName`：String，真实姓名
- `roleCode`：String，角色编码
- `roleName`：String，角色名称
- `token`：String，登录令牌或会话标识

### `CurrentUserVO`

字段：

- `userId`：Long
- `username`：String
- `realName`：String
- `roleCode`：String
- `roleName`：String

## 4. 用户模块 DTO / VO

### `UserCreateDTO`

字段：

- `username`：String
- `password`：String
- `realName`：String
- `phone`：String
- `roleId`：Long
- `status`：Integer

### `UserUpdateDTO`

字段：

- `id`：Long
- `realName`：String
- `phone`：String
- `roleId`：Long
- `status`：Integer

### `UserQueryDTO`

字段：

- `username`：String
- `realName`：String
- `status`：Integer
- `pageNum`：Integer
- `pageSize`：Integer

### `UserVO`

字段：

- `id`：Long
- `username`：String
- `realName`：String
- `phone`：String
- `status`：Integer
- `roleId`：Long
- `roleName`：String
- `createTime`：String

## 5. 角色模块 DTO / VO

### `RoleCreateDTO`

字段：

- `roleName`：String
- `roleCode`：String
- `remark`：String

### `RoleUpdateDTO`

字段：

- `id`：Long
- `roleName`：String
- `remark`：String

### `RoleVO`

字段：

- `id`：Long
- `roleName`：String
- `roleCode`：String
- `remark`：String

## 6. 风险数据模块 DTO / VO

### `RiskDataCreateDTO`

字段：

- `businessNo`：String
- `customerName`：String
- `businessType`：String
- `riskDesc`：String

### `RiskDataUpdateDTO`

字段：

- `id`：Long
- `customerName`：String
- `businessType`：String
- `riskDesc`：String

### `RiskDataQueryDTO`

字段：

- `businessNo`：String
- `customerName`：String
- `businessType`：String
- `dataStatus`：Integer
- `pageNum`：Integer
- `pageSize`：Integer

### `RiskDataVO`

字段：

- `id`：Long
- `businessNo`：String
- `customerName`：String
- `businessType`：String
- `riskDesc`：String
- `dataStatus`：Integer
- `createBy`：Long
- `createByName`：String
- `createTime`：String

## 7. 风险指标模块 DTO / VO

### `RiskIndexCreateDTO`

字段：

- `indexName`：String
- `indexCode`：String
- `weightValue`：BigDecimal
- `indexDesc`：String
- `status`：Integer

### `RiskIndexUpdateDTO`

字段：

- `id`：Long
- `indexName`：String
- `weightValue`：BigDecimal
- `indexDesc`：String
- `status`：Integer

### `RiskIndexQueryDTO`

字段：

- `indexName`：String
- `status`：Integer

### `RiskIndexVO`

字段：

- `id`：Long
- `indexName`：String
- `indexCode`：String
- `weightValue`：BigDecimal
- `indexDesc`：String
- `status`：Integer

## 8. 评分规则模块 DTO / VO

### `RiskRuleCreateDTO`

字段：

- `indexId`：Long
- `scoreMin`：BigDecimal
- `scoreMax`：BigDecimal
- `scoreValue`：BigDecimal
- `warningLevel`：String

### `RiskRuleUpdateDTO`

字段：

- `id`：Long
- `scoreMin`：BigDecimal
- `scoreMax`：BigDecimal
- `scoreValue`：BigDecimal
- `warningLevel`：String

### `RiskRuleVO`

字段：

- `id`：Long
- `indexId`：Long
- `indexName`：String
- `scoreMin`：BigDecimal
- `scoreMax`：BigDecimal
- `scoreValue`：BigDecimal
- `warningLevel`：String

## 9. 风险评估模块 DTO / VO

### `AssessmentQueryDTO`

字段：

- `businessNo`：String
- `riskLevel`：String
- `assessmentStatus`：Integer
- `startTime`：String
- `endTime`：String
- `pageNum`：Integer
- `pageSize`：Integer

### `AssessmentExecuteDTO`

字段：

- `riskDataId`：Long

说明：也可以直接用路径参数，不单独建 DTO。

### `AssessmentVO`

字段：

- `id`：Long
- `riskDataId`：Long
- `businessNo`：String
- `customerName`：String
- `totalScore`：BigDecimal
- `riskLevel`：String
- `assessmentStatus`：Integer
- `assessmentTime`：String
- `assessmentBy`：Long
- `assessmentByName`：String
- `warningGenerated`：Boolean

### `AssessmentDetailVO`

字段：

- `id`：Long
- `riskDataId`：Long
- `businessNo`：String
- `customerName`：String
- `businessType`：String
- `riskDesc`：String
- `totalScore`：BigDecimal
- `riskLevel`：String
- `assessmentTime`：String
- `assessmentByName`：String
- `warningInfo`：WarningSimpleVO

## 10. 预警模块 DTO / VO

### `WarningQueryDTO`

字段：

- `warningCode`：String
- `warningLevel`：String
- `warningStatus`：Integer
- `startTime`：String
- `endTime`：String
- `pageNum`：Integer
- `pageSize`：Integer

### `WarningHandleDTO`

字段：

- `warningId`：Long
- `handleOpinion`：String
- `handleResult`：String

### `WarningVO`

字段：

- `id`：Long
- `warningCode`：String
- `warningLevel`：String
- `warningContent`：String
- `warningStatus`：Integer
- `assessmentId`：Long
- `businessNo`：String
- `customerName`：String
- `createTime`：String

### `WarningDetailVO`

字段：

- `id`：Long
- `warningCode`：String
- `warningLevel`：String
- `warningContent`：String
- `warningStatus`：Integer
- `businessNo`：String
- `customerName`：String
- `riskLevel`：String
- `totalScore`：BigDecimal
- `createTime`：String
- `handleRecords`：List<WarningHandleRecordVO>

### `WarningHandleRecordVO`

字段：

- `id`：Long
- `warningId`：Long
- `handleUserId`：Long
- `handleUserName`：String
- `handleOpinion`：String
- `handleResult`：String
- `handleTime`：String

### `WarningSimpleVO`

字段：

- `warningId`：Long
- `warningCode`：String
- `warningLevel`：String
- `warningStatus`：Integer

## 11. 统计模块 DTO / VO

### `StatisticsQueryDTO`

字段：

- `startTime`：String
- `endTime`：String
- `riskLevel`：String
- `warningStatus`：Integer

### `DashboardStatisticsVO`

字段：

- `riskDataCount`：Long
- `assessmentCount`：Long
- `warningCount`：Long
- `handledWarningCount`：Long
- `highRiskCount`：Long

### `RiskLevelStatisticsVO`

字段：

- `riskLevel`：String
- `count`：Long

### `WarningTrendStatisticsVO`

字段：

- `date`：String
- `count`：Long

### `HandleSummaryStatisticsVO`

字段：

- `statusName`：String
- `count`：Long

## 12. 日志模块 DTO / VO

### `LogQueryDTO`

字段：

- `moduleName`：String
- `operationType`：String
- `userId`：Long
- `startTime`：String
- `endTime`：String
- `pageNum`：Integer
- `pageSize`：Integer

### `LogVO`

字段：

- `id`：Long
- `userId`：Long
- `userName`：String
- `moduleName`：String
- `operationType`：String
- `operationDesc`：String
- `operationTime`：String

## 13. 推荐的包内文件清单

### `dto/auth`

- `LoginRequestDTO.java`

### `dto/user`

- `UserCreateDTO.java`
- `UserUpdateDTO.java`
- `UserQueryDTO.java`
- `RoleCreateDTO.java`
- `RoleUpdateDTO.java`

### `dto/riskdata`

- `RiskDataCreateDTO.java`
- `RiskDataUpdateDTO.java`
- `RiskDataQueryDTO.java`

### `dto/riskindex`

- `RiskIndexCreateDTO.java`
- `RiskIndexUpdateDTO.java`
- `RiskIndexQueryDTO.java`

### `dto/riskrule`

- `RiskRuleCreateDTO.java`
- `RiskRuleUpdateDTO.java`

### `dto/assessment`

- `AssessmentQueryDTO.java`

### `dto/warning`

- `WarningQueryDTO.java`
- `WarningHandleDTO.java`

### `dto/statistics`

- `StatisticsQueryDTO.java`

### `dto/log`

- `LogQueryDTO.java`

### `vo/auth`

- `LoginUserVO.java`
- `CurrentUserVO.java`

### `vo/user`

- `UserVO.java`
- `RoleVO.java`

### `vo/riskdata`

- `RiskDataVO.java`

### `vo/assessment`

- `AssessmentVO.java`
- `AssessmentDetailVO.java`

### `vo/warning`

- `WarningVO.java`
- `WarningDetailVO.java`
- `WarningHandleRecordVO.java`
- `WarningSimpleVO.java`

### `vo/statistics`

- `DashboardStatisticsVO.java`
- `RiskLevelStatisticsVO.java`
- `WarningTrendStatisticsVO.java`
- `HandleSummaryStatisticsVO.java`

### `vo/log`

- `LogVO.java`

## 14. 适合喂给 AI 的对象设计约束

后续让 AI 生成 DTO / VO 代码时，可直接附带以下约束：

- 使用 `Lombok` 简化 getter/setter
- DTO 和 VO 使用 `@Data`
- DTO 中分页字段统一使用 `pageNum`、`pageSize`
- 时间字段对外统一使用 `String`，格式在 Service 层处理
- BigDecimal 用于分值、权重、统计数值相关字段
- 所有 VO 不返回密码字段
- 查询 DTO 不要继承 Entity
- DTO/VO 命名与模块保持一致
