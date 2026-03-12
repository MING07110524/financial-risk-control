<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">操作日志</h1>
        <p class="page-subtitle">日志查询会在真实后端阶段接入，目前保留为管理端占位入口。</p>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      title="日志查询会在真实后端阶段接入。当前先保留入口，是为了让管理员视角在演示时也能讲清后续系统治理能力。"
    />

    <el-row :gutter="20">
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>这里未来记录什么</template>
          <div class="placeholder-copy">
            <p>登录成功/失败</p>
            <p>风险数据新增、编辑、删除</p>
            <p>评估执行与预警处理</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>当前为什么不做</template>
          <div class="placeholder-copy">
            <p>日志依赖真实后端写操作。</p>
            <p>当前先把风控业务闭环和页面体验跑通，再把日志和后台治理能力接进来。</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="section-card" shadow="never">
      <p class="empty-hint">后续将接入模块、操作类型、操作人、时间范围等筛选条件。</p>
      <el-table :data="[]" style="width: 100%; margin-top: 18px;">
        <el-table-column prop="operationTime" label="操作时间" />
        <el-table-column prop="userName" label="操作人" />
        <el-table-column prop="moduleName" label="模块" />
        <el-table-column prop="operationType" label="类型" />
        <el-table-column prop="operationDesc" label="描述" />
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.placeholder-copy {
  display: grid;
  gap: 8px;
}

.placeholder-copy p {
  margin: 0;
  color: #58738f;
}
</style>
