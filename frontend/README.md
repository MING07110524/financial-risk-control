# 金融风控前端演示说明

## 启动

```powershell
cd E:\Project\fengxian031117\frontend
npm install
npm run dev
```

默认真实环境使用脚本：`npm run dev`

独立 Mock 环境使用脚本：`npm run dev:mock`

## 环境说明

当前前端只保留两套明确环境：

- 默认环境：全真实后端
- Mock 环境：全 Mock 回退
- 默认环境读取 `frontend/.env`，固定为 `VITE_USE_MOCK=false`
- Mock 环境读取 `frontend/.env.mock`，固定为 `VITE_USE_MOCK=true`
- 默认环境要求后端已启动并连接真实 MySQL
- `H2` 仅用于后端测试，不参与前端日常运行

默认环境开关在 `.env`：

```env
VITE_USE_MOCK=false
```

Mock 回退环境开关在 `.env.mock`：

```env
VITE_USE_MOCK=true
```

如需启用独立 Mock 回退环境：

```powershell
npm run dev:mock
```

## 构建与验证

- 真实环境构建：`npm run build`
- Mock 环境构建：`npm run build:mock`

## 演示账号

- `risk-demo`：风控人员，体验完整业务闭环
- `manager-demo`：管理人员，只读查看预警与统计
- `admin-demo`：管理员，体验指标规则、用户管理、操作日志与 AI 助手占位页

## 推荐演示路径

### 风控人员

1. 登录 `risk-demo`
2. 进入“风险数据”新增或编辑一条业务
3. 点击“去评估 / 重新评估”
4. 在“风险评估”页执行评估并查看评分详情
5. 进入“预警管理”提交处理意见
6. 回到“仪表盘”或“统计分析”刷新数据，确认结果回流

### 管理人员

1. 登录 `manager-demo`
2. 在首页查看角色关注点和图表
3. 进入“预警管理”查看预警详情和处理时间线
4. 进入“统计分析”切换筛选，确认图表同步变化

### 管理员

1. 登录 `admin-demo`
2. 在首页查看管理员职责说明
3. 进入“指标规则”完成指标新增/编辑/启停、规则新增/编辑/删除
4. 进入“用户管理”完成用户新增/编辑/启停/删除
5. 进入“操作日志”筛选查看真实后台写操作
6. 进入“AI 助手”页验证占位接口提示

## 体验提示

- 顶部“演示路径”按钮会按当前角色展示推荐操作顺序
- 默认环境已走真实后端，不再依赖“重置本地 Mock 数据”作为主要体验路径
- Mock 环境仅用于本地回退演示，真实联调与答辩路径请使用默认环境
- “重置本地 Mock 数据”按钮只会在 Mock 环境显示
- 统计图表使用 `ECharts`，构建时可能出现 chunk size warning，不影响本地运行
