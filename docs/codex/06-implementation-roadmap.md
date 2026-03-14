# 实施路线图

## 1. 总体顺序

固定实施顺序如下：

1. 脚手架与公共层
2. 认证与用户
3. 风险数据与指标值
4. 规则、评估与预警
5. 统计与日志
6. 前端联调

任何新会话都应按此顺序推进，除非明确在修复某个已存在阶段的问题。

## 1.1 当前阶段判断

- 当前代码已经完成认证、用户、风险数据、指标规则、评估、预警、统计、日志的真实后端闭环。
- 默认前端环境已固定为真实后端模式，mock 仅作为独立环境回退。
- 本文前 6 个阶段已基本完成；当前重点转为真库联调、页面收口、文档同步与演示脚本固化。
- 当前最终验收口径为：真实 MySQL + 默认真实前端模式；mock 仅做回退冒烟，不作为正式答辩路径。

## 2. Phase 1：脚手架与公共层

### 目标

- 将仓库从当前空骨架扩展为同仓全栈基础结构
- 建立后端公共能力、前端基本工程与文档基线

### 输入文档

- [00-README.md](/E:/Project/fengxian031117/docs/codex/00-README.md)
- [02-architecture-and-repo.md](/E:/Project/fengxian031117/docs/codex/02-architecture-and-repo.md)
- [03-domain-and-database.md](/E:/Project/fengxian031117/docs/codex/03-domain-and-database.md)

### 任务

- 创建 `backend` Maven 工程
- 创建 `frontend` Vite 工程
- 建立后端基础包结构
- 建立前端基础目录结构
- 实现 `Result<T>`、`PageResult<T>`、错误码、全局异常
- 配置 `Spring Security + JWT` 基础骨架
- 引入 MyBatis、MySQL、Lombok、Validation 等依赖
- 准备数据库初始化脚本，按 `V1` 口径修正表结构

### 完成定义

- `backend` 可启动
- `frontend` 可启动
- 后端已具备统一返回和异常处理
- 数据库初始化脚本包含 11 张核心表

### 交付物

- `backend/pom.xml`
- 后端基础包与配置类
- `frontend/package.json`
- 前端基础布局与路由空壳
- `backend/src/main/resources/db` 初始化脚本

## 3. Phase 2：认证与用户

### 目标

- 跑通登录、当前用户、用户管理和角色列表查询

### 输入文档

- [01-project-brief.md](/E:/Project/fengxian031117/docs/codex/01-project-brief.md)
- [04-api-and-backend-spec.md](/E:/Project/fengxian031117/docs/codex/04-api-and-backend-spec.md)
- [05-frontend-spec.md](/E:/Project/fengxian031117/docs/codex/05-frontend-spec.md)

### 任务

- 实现 `auth` 模块登录和 `me`
- 实现 JWT 生成与解析
- 实现 `sys_user`、`sys_role`、`sys_user_role` 的实体、Mapper、Service、Controller
- 实现用户列表、详情、新增、编辑、状态切换、删除
- 实现角色列表读取
- 前端实现登录页、基础布局、用户管理页
- 前端实现登录态持久化、路由守卫、权限菜单

### 完成定义

- 三类角色可以正常登录
- 未登录访问受保护接口会返回 `40100`
- `ADMIN` 可完成用户管理
- 非 `ADMIN` 无法访问用户管理接口

### 交付物

- 后端认证与用户模块
- 前端登录页与用户管理页
- 基础菜单与权限路由

## 4. Phase 3：风险数据与指标值

### 目标

- 跑通风险数据主记录和指标值明细的录入、查询、修改、删除

### 输入文档

- [03-domain-and-database.md](/E:/Project/fengxian031117/docs/codex/03-domain-and-database.md)
- [04-api-and-backend-spec.md](/E:/Project/fengxian031117/docs/codex/04-api-and-backend-spec.md)
- [05-frontend-spec.md](/E:/Project/fengxian031117/docs/codex/05-frontend-spec.md)

### 任务

- 建立 `risk_data` 与 `risk_data_index_value` 对应对象与 Mapper
- 实现风险数据查询、详情、新增、修改、删除
- 实现写入时校验“所有启用指标都有且只有一个指标值”
- 指标值变更后正确设置 `data_status`
- 前端实现风险数据管理页和动态指标值录入区域

### 完成定义

- `RISK_USER` 可保存一条主记录及多条指标值明细
- 业务编号重复时后端返回明确错误
- 编辑后能再次打开详情并看到指标值明细

### 交付物

- 风险数据模块后端
- 风险数据管理页前端

## 5. Phase 4：规则、评估与预警

### 目标

- 跑通“指标/规则配置 -> 执行评估 -> 自动生成预警 -> 手工处理预警”

### 输入文档

- [03-domain-and-database.md](/E:/Project/fengxian031117/docs/codex/03-domain-and-database.md)
- [04-api-and-backend-spec.md](/E:/Project/fengxian031117/docs/codex/04-api-and-backend-spec.md)
- [05-frontend-spec.md](/E:/Project/fengxian031117/docs/codex/05-frontend-spec.md)

### 任务

- 实现指标管理和规则管理
- 实现权重总和、规则区间冲突校验
- 实现 `RiskScoreCalculator`
- 实现评估执行与旧评估失效逻辑
- 实现 `WarningGenerator`
- 实现预警列表、详情、处理记录、处理接口
- 前端实现指标与规则页、评估页、预警页

### 完成定义

- 评估只基于已落库指标值执行
- `MEDIUM/HIGH` 评估结果会自动生成预警
- 同一评估不会生成重复预警
- `RISK_USER` 可以处理预警，`MANAGER` 只能查看

### 交付物

- 指标与规则模块
- 评估模块
- 预警模块
- 对应前端页面

## 6. Phase 5：统计与日志

### 目标

- 完成仪表盘、统计分析和操作日志查询

### 输入文档

- [01-project-brief.md](/E:/Project/fengxian031117/docs/codex/01-project-brief.md)
- [04-api-and-backend-spec.md](/E:/Project/fengxian031117/docs/codex/04-api-and-backend-spec.md)
- [05-frontend-spec.md](/E:/Project/fengxian031117/docs/codex/05-frontend-spec.md)

### 任务

- 实现首页仪表盘统计接口
- 实现风险等级分布、预警趋势、处置汇总接口
- 实现日志记录与日志查询接口
- 前端实现仪表盘、统计分析页、日志页
- 把关键写操作统一接入 `LogRecorder`

### 完成定义

- 统计数据可随业务操作变化
- `ADMIN` 可查看日志
- `RISK_USER` 与 `MANAGER` 可查看统计分析
- 图表与筛选条件一致

### 交付物

- 统计模块
- 日志模块
- 仪表盘、统计、日志前端页面

## 7. Phase 6：前端联调

### 目标

- 打通完整演示链路并修复跨页面体验问题

### 输入文档

- [全部文档](/E:/Project/fengxian031117/docs/codex/00-README.md)

### 任务

- 统一接口错误提示
- 统一时间格式展示
- 优化空态、加载态、无权限态
- 校验按钮权限、菜单权限、接口权限是否一致
- 整理答辩演示路径
- 补齐 AI 占位接口禁用提示

### 完成定义

- 三类角色都能完成各自的关键操作
- 六个核心场景均可演示
- 前端核心业务链默认走真实后端接口，保留 mock 框架仅用于本地演示切换和回退
- 真库联调结果、文档口径、演示脚本三者保持一致

### 交付物

- 完整可联调前后端
- 演示顺序说明

## 8. 阶段验收清单

每完成一个阶段，都执行以下检查：

- 模块、表、DTO/VO、接口、页面、用例是否一一对应
- 是否新增了文档未说明的字段、状态或接口
- 是否出现前端能点但后端不支持的按钮
- 是否出现后端有接口但前端无入口的功能
- 是否为关键写操作补齐日志记录

## 9. 最终验收

### 业务验收

- 用户登录
- 用户管理
- 风险数据录入
- 指标规则配置
- 风险评估与预警
- 统计查询

### 技术验收

- 同仓结构清晰
- 鉴权方案统一
- 当前运行事实、目标数据库结构、后续迁移边界三者描述一致
- 文档与代码一致

## 10. 下一阶段：工程化与交付优化

### 目标

- 在现有真实链路稳定的基础上，继续收敛工程化能力与交付体验。

### 边界

- 尽量保持现有接口 contract、页面路径与演示脚本稳定。
- 优先处理构建体积、接口分页 SQL 下沉、数据库迁移版本化、演示数据回收策略、部署脚本等工程问题。
- AI 助手仍保持占位，不在下一阶段扩展真实能力。

### 完成定义

- 默认真实环境可稳定演示、答辩和交接。
- 数据库初始化与旧库兼容迁移进一步版本化。
- 文档、演示脚本、验收清单与实际系统行为持续一致。

### 文档验收

- 新会话只读 `docs/codex/` 即可开始实现
- 不需要再临时决定技术栈、表结构、接口命名和开发顺序
