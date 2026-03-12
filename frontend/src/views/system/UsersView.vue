<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">该页面保留为第二阶段真实后端接入入口，当前最小闭环中暂不实现用户 CRUD。</p>
      </div>
      <el-button type="primary" disabled>新增用户（待实现）</el-button>
    </div>

    <el-alert
      type="info"
      :closable="false"
      title="这里是管理员后台能力的预留入口。当前最小闭环不实现用户 CRUD，是为了优先完成风控业务链，而不是省略后台设计。"
    />

    <el-row :gutter="20">
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>为什么先占位</template>
          <div class="placeholder-copy">
            <p>V1 当前优先交付的是风控主线：风险数据、评估、预警与统计。</p>
            <p>用户管理会在接入真实认证与后台能力时落到这里，负责账号、角色、状态维护。</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>后续会补什么</template>
          <div class="placeholder-copy">
            <p>1. 用户列表与筛选</p>
            <p>2. 新增、编辑、停用、删除</p>
            <p>3. 角色读取与权限约束</p>
          </div>
        </el-card>
      </el-col>
    </el-row>

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
