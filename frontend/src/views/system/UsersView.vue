<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">该页面保留为第二阶段真实后端接入入口，当前最小闭环中暂不实现用户 CRUD。</p>
      </div>
      <el-button type="primary" disabled>新增用户（待实现）</el-button>
    </div>

    <el-card class="section-card" shadow="never">
      <div class="table-toolbar">
        <div class="empty-hint">后续将接入用户名、姓名、角色、状态筛选和完整用户管理接口。</div>
        <el-tag>管理员入口</el-tag>
      </div>

      <el-table :data="[]" style="width: 100%; margin-top: 18px;">
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="realName" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="roleName" label="角色" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" />
      </el-table>
    </el-card>
  </div>
</template>
