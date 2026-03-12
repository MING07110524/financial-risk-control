<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">操作日志</h1>
        <p class="page-subtitle">日志查询会在真实后端阶段接入，目前保留为管理端占位入口。</p>
      </div>
    </div>

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
