<script setup lang="ts">
import { ArrowRightBold, DataAnalysis, Monitor, Notebook, PieChart, RefreshRight, UserFilled, Warning } from "@element-plus/icons-vue";
import { ElMessage, ElMessageBox } from "element-plus";
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import { isMockMode, systemService } from "@/services";
import { useUserStore } from "@/stores/user";
import { getErrorMessage } from "@/utils/result";

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const iconMap = {
  dashboard: Monitor,
  "system-users": UserFilled,
  "system-logs": Notebook,
  "risk-data": Notebook,
  "risk-indexes": DataAnalysis,
  "risk-assessments": ArrowRightBold,
  "risk-warnings": Warning,
  "analysis-statistics": PieChart,
} as const;

const menuItems = computed(() =>
  router.getRoutes().filter((item) => {
    if (!item.meta?.showInMenu) {
      return false;
    }
    const roles = item.meta.roles as string[] | undefined;
    return !roles || roles.includes(userStore.roleCode);
  }),
);

async function handleLogout() {
  await userStore.logout();
  await router.push("/login");
}

async function handleResetDemoData() {
  try {
    await ElMessageBox.confirm(
      "这会把前端本地演示数据恢复到初始状态，但会保留当前登录态。是否继续？",
      "重置演示数据",
      {
        type: "warning",
        confirmButtonText: "确认重置",
        cancelButtonText: "取消",
      },
    );

    await systemService.resetDemoData();
    ElMessage.success("演示数据已重置，正在刷新页面");
    window.location.reload();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(getErrorMessage(error));
    }
  }
}
</script>

<template>
  <el-container class="layout-shell">
    <el-aside class="layout-aside" width="260px">
      <div class="brand-card">
        <p class="brand-card__eyebrow">financial-risk-control</p>
        <h1 class="brand-card__title">金融风控演示台</h1>
        <p class="brand-card__caption">最小闭环前端原型 · Mock 业务流</p>
      </div>

      <el-menu
        :default-active="route.name?.toString()"
        class="nav-menu"
        router
      >
        <el-menu-item
          v-for="item in menuItems"
          :key="item.name"
          :index="item.name?.toString() ?? item.path"
          :route="{ name: item.name as string }"
        >
          <el-icon><component :is="iconMap[item.name as keyof typeof iconMap] ?? Monitor" /></el-icon>
          <span>{{ item.meta?.title }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div>
          <p class="layout-header__label">当前页面</p>
          <h2 class="layout-header__title">{{ route.meta.title ?? "金融风控系统" }}</h2>
          <p class="layout-header__subtitle">{{ userStore.currentUser?.roleName ?? "未登录用户" }}</p>
        </div>
        <div class="layout-header__actions">
          <el-tag v-if="isMockMode" type="warning" effect="dark">Mock 模式</el-tag>
          <span class="layout-header__user">{{ userStore.currentUser?.realName ?? "未知用户" }}</span>
          <el-button v-if="isMockMode" plain :icon="RefreshRight" @click="handleResetDemoData">重置演示数据</el-button>
          <el-button type="primary" plain @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="layout-main">
        <RouterView />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.layout-shell {
  min-height: 100vh;
}

.layout-aside {
  padding: 18px;
  background: linear-gradient(180deg, #0f2742 0%, #173b5c 100%);
}

.brand-card {
  padding: 18px;
  border-radius: 22px;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.14), rgba(255, 255, 255, 0.04));
  color: #f7fbff;
}

.brand-card__eyebrow {
  margin: 0;
  font-size: 12px;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(247, 251, 255, 0.72);
}

.brand-card__title {
  margin: 12px 0 8px;
  font-size: 24px;
  line-height: 1.2;
}

.brand-card__caption {
  margin: 0;
  color: rgba(247, 251, 255, 0.72);
  font-size: 13px;
}

.nav-menu {
  margin-top: 18px;
  border: none;
  border-radius: 22px;
  overflow: hidden;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  height: auto;
  min-height: 84px;
  padding: 20px 28px 12px;
}

.layout-header__label {
  margin: 0;
  font-size: 12px;
  color: #6a8298;
  text-transform: uppercase;
  letter-spacing: 0.08em;
}

.layout-header__title {
  margin: 8px 0 0;
  color: #0f2742;
}

.layout-header__subtitle {
  margin: 6px 0 0;
  color: #58738f;
}

.layout-header__actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.layout-header__user {
  color: #17324d;
  font-weight: 600;
}

.layout-main {
  padding: 0 28px 28px;
}

@media (max-width: 960px) {
  .layout-shell {
    display: block;
  }

  .layout-aside {
    width: 100%;
  }

  .layout-header {
    padding: 20px 20px 12px;
  }

  .layout-main {
    padding: 0 20px 20px;
  }
}
</style>
