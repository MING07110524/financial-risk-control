# SpringBoot 项目代码目录与类清单

本文档面向后续 AI 编程与实际编码实现，给出推荐的后端项目目录结构、核心包划分、主要类清单及职责说明。默认项目名可命名为 `financial-risk-control`，包名建议使用 `com.cmj.risk`。

## 1. 推荐项目目录结构

```text
financial-risk-control
├─ src/main/java/com/cmj/risk
│  ├─ FinancialRiskControlApplication.java
│  ├─ controller
│  │  ├─ AuthController.java
│  │  ├─ UserController.java
│  │  ├─ RoleController.java
│  │  ├─ RiskDataController.java
│  │  ├─ RiskIndexController.java
│  │  ├─ RiskRuleController.java
│  │  ├─ AssessmentController.java
│  │  ├─ WarningController.java
│  │  ├─ StatisticsController.java
│  │  └─ LogController.java
│  ├─ service
│  │  ├─ AuthService.java
│  │  ├─ UserService.java
│  │  ├─ RoleService.java
│  │  ├─ RiskDataService.java
│  │  ├─ RiskIndexService.java
│  │  ├─ RiskRuleService.java
│  │  ├─ AssessmentService.java
│  │  ├─ WarningService.java
│  │  ├─ StatisticsService.java
│  │  └─ LogService.java
│  ├─ service/impl
│  │  ├─ AuthServiceImpl.java
│  │  ├─ UserServiceImpl.java
│  │  ├─ RoleServiceImpl.java
│  │  ├─ RiskDataServiceImpl.java
│  │  ├─ RiskIndexServiceImpl.java
│  │  ├─ RiskRuleServiceImpl.java
│  │  ├─ AssessmentServiceImpl.java
│  │  ├─ WarningServiceImpl.java
│  │  ├─ StatisticsServiceImpl.java
│  │  └─ LogServiceImpl.java
│  ├─ mapper
│  │  ├─ UserMapper.java
│  │  ├─ RoleMapper.java
│  │  ├─ UserRoleMapper.java
│  │  ├─ RiskDataMapper.java
│  │  ├─ RiskIndexMapper.java
│  │  ├─ RiskRuleMapper.java
│  │  ├─ AssessmentMapper.java
│  │  ├─ WarningMapper.java
│  │  ├─ WarningHandleRecordMapper.java
│  │  └─ LogMapper.java
│  ├─ entity
│  │  ├─ SysUser.java
│  │  ├─ SysRole.java
│  │  ├─ SysUserRole.java
│  │  ├─ RiskData.java
│  │  ├─ RiskIndex.java
│  │  ├─ RiskRule.java
│  │  ├─ RiskAssessment.java
│  │  ├─ RiskWarning.java
│  │  ├─ WarningHandleRecord.java
│  │  └─ SysLog.java
│  ├─ dto
│  │  ├─ auth
│  │  ├─ user
│  │  ├─ riskdata
│  │  ├─ riskindex
│  │  ├─ riskrule
│  │  ├─ assessment
│  │  ├─ warning
│  │  └─ statistics
│  ├─ vo
│  │  ├─ auth
│  │  ├─ user
│  │  ├─ riskdata
│  │  ├─ assessment
│  │  ├─ warning
│  │  └─ statistics
│  ├─ common
│  │  ├─ Result.java
│  │  ├─ PageResult.java
│  │  ├─ ErrorCode.java
│  │  └─ Constants.java
│  ├─ config
│  │  ├─ MyBatisConfig.java
│  │  ├─ WebMvcConfig.java
│  │  ├─ CorsConfig.java
│  │  └─ JacksonConfig.java
│  ├─ exception
│  │  ├─ BusinessException.java
│  │  └─ GlobalExceptionHandler.java
│  ├─ enums
│  │  ├─ UserStatusEnum.java
│  │  ├─ RiskLevelEnum.java
│  │  ├─ WarningStatusEnum.java
│  │  └─ RoleCodeEnum.java
│  ├─ utils
│  │  ├─ PasswordUtils.java
│  │  ├─ DateUtils.java
│  │  ├─ IdUtils.java
│  │  └─ SecurityUtils.java
│  └─ component
│     ├─ RiskScoreCalculator.java
│     ├─ WarningGenerator.java
│     └─ LogRecorder.java
├─ src/main/resources
│  ├─ application.yml
│  ├─ mapper
│  │  ├─ UserMapper.xml
│  │  ├─ RoleMapper.xml
│  │  ├─ UserRoleMapper.xml
│  │  ├─ RiskDataMapper.xml
│  │  ├─ RiskIndexMapper.xml
│  │  ├─ RiskRuleMapper.xml
│  │  ├─ AssessmentMapper.xml
│  │  ├─ WarningMapper.xml
│  │  ├─ WarningHandleRecordMapper.xml
│  │  └─ LogMapper.xml
│  └─ db
│     └─ database-schema.sql
└─ pom.xml
```

## 2. 核心包职责说明

### 2.1 `controller`

负责接收前端请求、参数接收、调用 Service、返回统一响应对象，不写复杂业务逻辑。

### 2.2 `service`

定义业务接口，约束模块能力，便于后续替换实现或做单元测试。

### 2.3 `service.impl`

实现具体业务逻辑，包括参数校验、业务规则判断、数据流转、调用 Mapper 和组件。

### 2.4 `mapper`

定义 MyBatis 数据访问接口，对应数据库表的增删改查和统计查询。

### 2.5 `entity`

对应数据库实体对象，字段与表结构保持基本一致。

### 2.6 `dto`

用于接收前端输入参数，避免直接使用 Entity 接收入参。

### 2.7 `vo`

用于封装返回前端的数据结构，避免直接暴露 Entity。

### 2.8 `common`

放置统一响应、分页响应、错误码、系统常量等公共对象。

### 2.9 `config`

放置跨域、JSON 序列化、MyBatis 扫描、Web 配置等框架配置。

### 2.10 `exception`

放置业务异常和全局异常处理类，统一异常返回格式。

### 2.11 `enums`

枚举固定业务值，避免硬编码，如风险等级、用户状态、角色编码等。

### 2.12 `utils`

通用工具类，如密码处理、日期转换、ID 生成、会话用户获取等。

### 2.13 `component`

放置可复用业务组件，如风险评分计算器、预警生成器、日志记录器。

## 3. 启动类设计

### `FinancialRiskControlApplication`

- 作用：Spring Boot 启动入口
- 说明：用于启动整个后端服务，并加载 Spring 容器

## 4. Controller 类清单与职责

### `AuthController`

- 登录
- 退出
- 获取当前登录用户信息

### `UserController`

- 用户分页查询
- 新增用户
- 修改用户
- 删除/停用用户
- 查看用户详情

### `RoleController`

- 角色列表查询
- 新增角色
- 修改角色

### `RiskDataController`

- 风险数据分页查询
- 新增风险数据
- 修改风险数据
- 删除风险数据
- 查看风险数据详情

### `RiskIndexController`

- 风险指标查询
- 新增风险指标
- 修改风险指标
- 启停指标

### `RiskRuleController`

- 查询评分规则
- 新增评分规则
- 修改评分规则
- 删除评分规则

### `AssessmentController`

- 执行风险评估
- 分页查询评估记录
- 查看评估详情

### `WarningController`

- 查询预警列表
- 查看预警详情
- 提交预警处理
- 查看预警处理记录

### `StatisticsController`

- 首页统计概览
- 风险等级分布统计
- 预警趋势统计
- 处置情况统计

### `LogController`

- 日志分页查询
- 日志详情查看

## 5. Service 接口与实现建议

### `AuthService`

- `login(LoginRequestDTO dto)`
- `logout(Long userId)`
- `getCurrentUserInfo(Long userId)`

### `UserService`

- `pageUsers(UserQueryDTO dto)`
- `getUserById(Long id)`
- `saveUser(UserCreateDTO dto)`
- `updateUser(UserUpdateDTO dto)`
- `updateUserStatus(Long id, Integer status)`
- `deleteUser(Long id)`

### `RoleService`

- `listRoles()`
- `saveRole(RoleCreateDTO dto)`
- `updateRole(RoleUpdateDTO dto)`

### `RiskDataService`

- `pageRiskData(RiskDataQueryDTO dto)`
- `getRiskDataById(Long id)`
- `saveRiskData(RiskDataCreateDTO dto)`
- `updateRiskData(RiskDataUpdateDTO dto)`
- `deleteRiskData(Long id)`

### `RiskIndexService`

- `listRiskIndexes(RiskIndexQueryDTO dto)`
- `saveRiskIndex(RiskIndexCreateDTO dto)`
- `updateRiskIndex(RiskIndexUpdateDTO dto)`
- `updateRiskIndexStatus(Long id, Integer status)`

### `RiskRuleService`

- `listRulesByIndexId(Long indexId)`
- `saveRiskRule(RiskRuleCreateDTO dto)`
- `updateRiskRule(RiskRuleUpdateDTO dto)`
- `deleteRiskRule(Long id)`

### `AssessmentService`

- `executeAssessment(Long riskDataId, Long operatorId)`
- `pageAssessments(AssessmentQueryDTO dto)`
- `getAssessmentDetail(Long id)`

### `WarningService`

- `pageWarnings(WarningQueryDTO dto)`
- `getWarningDetail(Long id)`
- `handleWarning(WarningHandleDTO dto, Long operatorId)`
- `listHandleRecords(Long warningId)`
- `createWarningIfNecessary(RiskAssessment assessment)`

### `StatisticsService`

- `getDashboardStatistics(StatisticsQueryDTO dto)`
- `getRiskLevelStatistics(StatisticsQueryDTO dto)`
- `getWarningTrendStatistics(StatisticsQueryDTO dto)`
- `getHandleSummaryStatistics(StatisticsQueryDTO dto)`

### `LogService`

- `recordLog(SysLog log)`
- `pageLogs(LogQueryDTO dto)`
- `getLogDetail(Long id)`

## 6. Entity 类清单与说明

### `SysUser`

对应 `sys_user` 表，保存用户账号、密码、姓名、状态等信息。

### `SysRole`

对应 `sys_role` 表，保存角色名称和角色编码。

### `SysUserRole`

对应 `sys_user_role` 表，用于关联用户和角色。

### `RiskData`

对应 `risk_data` 表，保存风险业务数据。

### `RiskIndex`

对应 `risk_index` 表，保存风险指标定义。

### `RiskRule`

对应 `risk_rule` 表，保存评分规则区间和分值。

### `RiskAssessment`

对应 `risk_assessment` 表，保存评估结果、总分和风险等级。

### `RiskWarning`

对应 `risk_warning` 表，保存系统生成的预警信息。

### `WarningHandleRecord`

对应 `warning_handle_record` 表，保存预警处理过程。

### `SysLog`

对应 `sys_log` 表，保存关键业务操作日志。

## 7. 组件类建议

### `RiskScoreCalculator`

- 作用：封装风险评估计算逻辑
- 输入：风险数据、风险指标、评分规则
- 输出：总分、风险等级

### `WarningGenerator`

- 作用：根据评估结果判断是否生成预警
- 输入：评估结果
- 输出：预警对象或空值

### `LogRecorder`

- 作用：统一封装日志记录逻辑
- 输入：操作人、模块名、操作类型、操作描述
- 输出：无

## 8. 枚举类建议

### `UserStatusEnum`

- `ENABLE(1, "启用")`
- `DISABLE(0, "停用")`

### `RiskLevelEnum`

- `LOW("低风险")`
- `MEDIUM("中风险")`
- `HIGH("高风险")`

### `WarningStatusEnum`

- `PENDING(0, "待处理")`
- `PROCESSING(1, "处理中")`
- `FINISHED(2, "已处理")`

### `RoleCodeEnum`

- `ADMIN`
- `RISK_USER`
- `MANAGER`

## 9. 编码阶段建议顺序

建议按以下顺序让 AI 或自己开始实现：

1. 建立公共返回结构、异常类、枚举类
2. 建立 Entity、Mapper、Mapper.xml
3. 实现登录和用户管理模块
4. 实现风险数据管理模块
5. 实现风险指标与规则模块
6. 实现风险评估模块
7. 实现预警处理模块
8. 实现统计分析模块
9. 最后补充日志记录与接口优化

## 10. 适合喂给 AI 的实现约束

后续让 AI 写代码时，可以直接附上这些约束：

- 使用 `Spring Boot + MyBatis + MySQL`
- 包名使用 `com.cmj.risk`
- 所有接口返回统一 `Result<T>`
- 不要把 Entity 直接作为接口入参和出参
- Controller 只做参数接收和结果返回
- 核心业务逻辑写在 ServiceImpl
- 评分逻辑单独抽到 `RiskScoreCalculator`
- 预警逻辑单独抽到 `WarningGenerator`
- 所有关键修改操作都记录日志
- 当前不实现 AI 助手，只保留扩展接口
