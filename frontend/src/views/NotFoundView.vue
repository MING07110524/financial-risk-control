<script setup lang="ts">
import { useRouter } from "vue-router";

const router = useRouter();
</script>

<template>
  <div class="login-shell">
    <el-card class="login-card" shadow="never">
      <template #header>
        <h2 class="login-card__title">页面不存在</h2>
      </template>
      <p class="page-subtitle">当前访问的路由不存在，请返回仪表盘继续操作。</p>
      <el-button type="primary" @click="router.push('/dashboard')">返回仪表盘</el-button>
    </el-card>
  </div>
</template>
