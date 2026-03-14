# 领域模型与数据库规范

## 1. 核心建模结论

当前项目的核心建模原则如下：

- 一条风险业务记录使用 `risk_data` 表保存主信息。
- 每条风险业务记录对应多个指标值，使用 `risk_data_index_value` 表保存。
- 风险评估不直接读取前端临时打分，而是读取已落库的指标值明细。
- 每次评估写入一条新的 `risk_assessment` 历史记录。
- `V1` 一次评估最多生成一条预警。
- `sys_user_role` 保留扩展性，但 `V1` 一个用户只能拥有一个角色。

## 1.1 当前运行事实

- 当前默认运行环境就是 `MyBatis + MySQL` 真库模式，不再是内存演示版。
- `RiskDemoStore` 与 `RiskWorkflowStore` 已改为数据库驱动，运行时不再依赖内存状态存储业务主数据。
- `backend/src/main/resources/db/schema-v1.sql` 与 `backend/src/main/resources/db/seed-v1.sql` 已用于真实运行态初始化。
- 前端默认也走真实后端，mock 仅保留为独立回退环境。
- 默认真实链路下，用户、角色、日志、风险数据、指标规则、评估、预警、统计均为持久化实现；当前无面向真实链路的内存业务模块。
- 仅 `frontend` 的 mock 环境保留本地回退数据，用于前端独立演示与冒烟。

## 2. 状态与枚举

### 角色编码 `role_code`

| 值 | 含义 |
|---|---|
| `ADMIN` | 系统管理员 |
| `RISK_USER` | 风控人员 |
| `MANAGER` | 管理人员 |

### 用户状态 `sys_user.status`

| 值 | 含义 |
|---|---|
| `1` | 启用 |
| `0` | 停用 |

### 风险数据状态 `risk_data.data_status`

| 值 | 含义 |
|---|---|
| `0` | 待评估 |
| `1` | 已评估 |
| `2` | 待重评 |
| `3` | 待补录 |

### 评估状态 `risk_assessment.assessment_status`

| 值 | 含义 |
|---|---|
| `1` | 当前有效 |
| `0` | 已失效 |

### 风险等级 `risk_assessment.risk_level`

| 值 | 区间 |
|---|---|
| `LOW` | `0 <= totalScore < 60` |
| `MEDIUM` | `60 <= totalScore < 80` |
| `HIGH` | `80 <= totalScore <= 100` |

### 预警状态 `risk_warning.warning_status`

| 值 | 含义 |
|---|---|
| `0` | 待处理 |
| `1` | 处理中 |
| `2` | 已处理 |

### 预警触发规则

- `LOW`：不生成预警
- `MEDIUM`：生成中风险预警
- `HIGH`：生成高风险预警

## 3. 评分规则

### 指标权重

- `risk_index.weight_value` 使用百分比，取值范围 `0 ~ 100`
- 所有启用中的指标权重总和必须等于 `100`

### 指标值

- `V1` 指标值统一为数值型
- 存储类型使用 `DECIMAL(10,2)`
- 每条风险数据对每个启用指标必须有且只有一条指标值

### 规则匹配

- `risk_rule` 按 `index_id` 归属某个指标
- 同一指标下规则区间不可重叠
- 评估时使用 `score_min <= indexValue <= score_max` 匹配规则
- 若某指标没有匹配到规则，评估失败

### 总分计算

对每个启用指标：

```text
itemWeightedScore = scoreValue * weightValue / 100
```

总分计算：

```text
totalScore = 所有 itemWeightedScore 之和
```

结果保留两位小数，四舍五入。

## 4. 数据库表清单

`V1` 当前固定 12 张核心表：

1. `sys_user`
2. `sys_role`
3. `sys_user_role`
4. `risk_data`
5. `risk_index`
6. `risk_rule`
7. `risk_data_index_value`
8. `risk_assessment`
9. `risk_assessment_index_result`
10. `risk_warning`
11. `warning_handle_record`
12. `sys_log`

## 5. 表结构约束

### 5.1 `sys_user`

用途：系统用户。

关键字段：

- `username`：唯一
- `password`：`BCrypt` 哈希
- `status`：启用/停用

说明：

- 默认管理员账号允许初始化，但密码必须写入哈希值，不允许明文 `123456`

### 5.2 `sys_role`

用途：角色字典。

关键字段：

- `role_code`：唯一

V1 约束：

- 初始化固定三条角色
- V1 只读，不开放自定义角色 CRUD

### 5.3 `sys_user_role`

用途：用户与角色映射。

关键字段：

- `(user_id, role_id)`：唯一

V1 约束：

- 每个 `user_id` 只能存在一条映射记录
- 通过该表保留未来多角色扩展空间

### 5.4 `risk_data`

用途：风险业务主记录。

关键字段：

- `business_no`：唯一
- `data_status`：待评估 / 已评估 / 待重评
- `create_by`：录入人

说明：

- 修改主记录或指标值明细后，如果已有有效评估结果，应将 `data_status` 改为 `2`

### 5.5 `risk_index`

用途：风险指标定义。

关键字段：

- `index_code`：唯一
- `weight_value`：百分比权重
- `status`：启用/停用

说明：

- 评估仅使用启用状态指标

### 5.6 `risk_rule`

用途：指标评分区间规则。

关键字段：

- `index_id`：所属指标
- `score_min` / `score_max`：区间边界
- `score_value`：命中该区间后的原始得分
- `warning_level`：建议预警等级

说明：

- 同一指标下规则区间不能重叠
- 建议服务层校验完整性与重叠性

### 5.7 `risk_data_index_value`

用途：保存风险数据对应的指标值明细。

建议字段：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | `BIGINT` | 主键 |
| `risk_data_id` | `BIGINT` | 风险数据 ID |
| `index_id` | `BIGINT` | 指标 ID |
| `index_value` | `DECIMAL(10,2)` | 指标数值 |
| `create_time` | `DATETIME` | 创建时间 |
| `update_time` | `DATETIME` | 更新时间 |

约束：

- `(risk_data_id, index_id)` 唯一
- 外键分别关联 `risk_data.id` 与 `risk_index.id`

说明：

- 这是当前项目从“能写文档”走向“能实际评估”的关键补充表

### 5.8 `risk_assessment`

用途：风险评估结果历史。

关键字段：

- `risk_data_id`
- `total_score`
- `risk_level`
- `assessment_status`
- `assessment_by`

规则：

- 每次执行评估插入一条新记录
- 同一 `risk_data_id` 原先的有效评估要先改成失效
- 新记录标记为当前有效

### 5.9 `risk_assessment_index_result`

用途：保存评估明细快照，保证旧评估详情不受后续规则变更影响。

关键字段：

- `assessment_id`
- `index_id`
- `index_code`
- `index_name`
- `index_value`
- `weight_value`
- `score_value`
- `weighted_score`
- `warning_level`

规则：

- 每次执行评估都要固化当次分项结果
- 评估详情直接读取该表，不按最新规则重算历史详情

### 5.10 `risk_warning`

用途：评估触发后的预警。

关键字段：

- `assessment_id`
- `warning_code`
- `warning_level`
- `warning_status`

约束：

- `warning_code` 唯一
- `assessment_id` 唯一

说明：

- `assessment_id` 唯一用于保证 `V1` 一次评估最多生成一条预警

### 5.11 `warning_handle_record`

用途：预警处置记录。

关键字段：

- `warning_id`
- `handle_user_id`
- `handle_opinion`
- `handle_result`
- `next_status`
- `handle_time`

说明：

- 一条预警可有多条处理记录
- V1 实际页面支持单人逐次处理

规则：

- `next_status` 只允许 `1`（处理中）或 `2`（已处理）

### 5.12 `sys_log`

用途：关键操作日志。

关键字段：

- `user_id`
- `operator`
- `module_name`
- `operation_type`
- `operation_desc`
- `operation_time`

说明：

- 登录成功、登录失败、用户变更、风险数据变更、评估执行、预警处理都应记日志
- `user_id` 允许为空，用于保存登录失败日志

## 6. 关系总览

| 主体 | 关系 | 客体 |
|---|---|---|
| `sys_user` | 1:N | `sys_user_role` |
| `sys_role` | 1:N | `sys_user_role` |
| `sys_user` | 1:N | `risk_data` |
| `risk_index` | 1:N | `risk_rule` |
| `risk_data` | 1:N | `risk_data_index_value` |
| `risk_index` | 1:N | `risk_data_index_value` |
| `risk_data` | 1:N | `risk_assessment` |
| `risk_assessment` | 1:N | `risk_assessment_index_result` |
| `risk_assessment` | 1:0..1 | `risk_warning` |
| `risk_warning` | 1:N | `warning_handle_record` |
| `sys_user` | 1:N | `warning_handle_record` |
| `sys_user` | 1:N | `sys_log` |

## 7. 初始化数据

### 固定角色

- `ADMIN`
- `RISK_USER`
- `MANAGER`

### 默认管理员

- 用户名：`admin`
- 密码：初始化脚本中写入 `BCrypt` 哈希值
- 角色：`ADMIN`

## 8. 数据一致性规则

- 删除用户前必须检查其是否被 `risk_data`、`risk_assessment`、`warning_handle_record`、`sys_log` 引用。
- 停用用户可以保留历史数据，但不可再登录。
- 删除指标前必须检查是否已有规则或指标值明细引用；当前实现只允许停用，不提供指标删除接口。
- 风险数据修改后，旧评估记录保留，但有效状态失效，主记录进入待重评。
- 风险数据一旦形成评估、预警或处置历史，当前实现拒绝删除。
- 预警处理完成后，状态必须更新为 `2`，并保留处理历史。

## 9. 对现有 SQL 的修正规则

当前 `项目文档/database-schema.sql` 可继续作为参考，但后续实现时应按本文件口径修正：

- 新增 `risk_data_index_value`
- 新增 `risk_assessment_index_result`
- `risk_warning.assessment_id` 增加唯一约束
- 管理员初始化密码改为 `BCrypt` 哈希
- 明确 `sys_user_role` 的 V1 单角色约束
- 明确评估历史保留与旧评估失效逻辑
