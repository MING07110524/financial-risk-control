<script setup lang="ts">
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import { dashboardService } from "@/services";
import { useUserStore } from "@/stores/user";
import type { DashboardStatisticsVO, WarningVO } from "@/types/risk";
import { formatDateTime, riskLevelLabel, riskLevelTagType, warningStatusLabel, warningStatusTagType } from "@/utils/format";
import { getErrorMessage, ensureSuccess } from "@/utils/result";

const router = useRouter();
const userStore = useUserStore();

const loading = ref(false);
const statistics = ref<DashboardStatisticsVO>({
  riskDataCount: 0,
  assessmentCount: 0,
  warningCount: 0,
  handledWarningCount: 0,
  highRiskCount: 0,
});
const recentWarnings = ref<WarningVO[]>([]);

const summaryCards = computed(() => [
  {
    label: "风险数据总数",
    value: statistics.value.riskDataCount,
    route: userStore.roleCode === "RISK_USER" ? "/risk/data" : undefined,
  },
  {
    label: "评估记录总数",
    value: statistics.value.assessmentCount,
    route: userStore.roleCode === "RISK_USER" ? "/risk/assessments" : undefined,
  },
  {
    label: "预警总数",
    value: statistics.value.warningCount,
    route: userStore.roleCode === "ADMIN" ? undefined : "/risk/warnings",
  },
  {
    label: "已处理预警",
    value: statistics.value.handledWarningCount,
    route: userStore.roleCode === "ADMIN" ? undefined : "/risk/warnings",
  },
  {
    label: "高风险业务",
    value: statistics.value.highRiskCount,
    route: userStore.roleCode === "RISK_USER" || userStore.roleCode === "MANAGER" ? "/analysis/statistics" : undefined,
  },
]);

const quickActions = computed(() => {
  if (userStore.roleCode === "ADMIN") {
    return [
      { label: "查看指标规则", route: "/risk/indexes", description: "浏览当前最小闭环依赖的预置指标和评分规则。" },
      { label: "查看用户管理占位页", route: "/system/users", description: "用户 CRUD 留待真后端阶段实现。" },
    ];
  }

  if (userStore.roleCode === "MANAGER") {
    return [
      { label: "查看预警列表", route: "/risk/warnings", description: "从只读视角查看待处理与已处理预警。" },
      { label: "查看统计分析", route: "/analysis/statistics", description: "查看风险等级分布、预警趋势与处置汇总。" },
    ];
  }

  return [
    { label: "录入风险数据", route: "/risk/data", description: "新增业务主记录并填写所有启用指标值。" },
    { label: "执行风险评估", route: "/risk/assessments", description: "基于已落库指标值计算总分并自动触发预警。" },
    { label: "处理预警", route: "/risk/warnings", description: "查看预警详情、填写处理意见并更新状态。" },
  ];
});

async function loadDashboard() {
  loading.value = true;
  try {
    statistics.value = ensureSuccess(await dashboardService.getDashboardStatistics());
    recentWarnings.value = ensureSuccess(await dashboardService.listRecentWarnings());
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

onMounted(loadDashboard);
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">仪表盘</h1>
        <p class="page-subtitle">
          这里汇总了当前 mock 业务链路的核心结果，适合作为演示入口页。
        </p>
      </div>
      <el-button :loading="loading" @click="loadDashboard">刷新数据</el-button>
    </div>

    <section class="stat-grid">
      <article
        v-for="item in summaryCards"
        :key="item.label"
        class="section-card stat-card"
        :class="{ 'stat-card--clickable': Boolean(item.route) }"
        @click="item.route && router.push(item.route)"
      >
        <div class="stat-card__label">{{ item.label }}</div>
        <div class="stat-card__value">{{ item.value }}</div>
        <div class="stat-card__hint">{{ item.route ? "点击进入" : "当前角色仅查看" }}</div>
      </article>
    </section>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="11">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
              <el-tag type="info">{{ userStore.currentUser?.roleName }}</el-tag>
            </div>
          </template>
          <div class="action-list">
            <button
              v-for="action in quickActions"
              :key="action.route"
              class="action-item"
              type="button"
              @click="router.push(action.route)"
            >
              <strong>{{ action.label }}</strong>
              <span>{{ action.description }}</span>
            </button>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="13">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近预警</span>
              <el-button link type="primary" @click="router.push('/risk/warnings')">查看全部</el-button>
            </div>
          </template>
          <el-empty v-if="recentWarnings.length === 0" description="当前没有预警数据" />
          <el-table v-else :data="recentWarnings" size="small">
            <el-table-column prop="warningCode" label="预警编号" min-width="150" />
            <el-table-column label="预警等级" min-width="110">
              <template #default="{ row }">
                <el-tag :type="riskLevelTagType(row.warningLevel)">{{ riskLevelLabel(row.warningLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="businessNo" label="业务编号" min-width="150" />
            <el-table-column label="状态" min-width="110">
              <template #default="{ row }">
                <el-tag :type="warningStatusTagType(row.warningStatus)">{{ warningStatusLabel(row.warningStatus) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="140">
              <template #default="{ row }">
                {{ formatDateTime(row.createTime) }}
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.stat-card--clickable {
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.stat-card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 20px 36px rgba(15, 39, 66, 0.12);
}

.stat-card__hint {
  margin-top: 12px;
  color: #8b5e00;
  font-size: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.action-list {
  display: grid;
  gap: 12px;
}

.action-item {
  display: grid;
  gap: 4px;
  padding: 16px 18px;
  border: 1px solid rgba(15, 39, 66, 0.08);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(247, 250, 252, 0.94), rgba(255, 255, 255, 0.98));
  text-align: left;
  cursor: pointer;
}

.action-item strong {
  color: #0f2742;
}

.action-item span {
  color: #58738f;
  font-size: 13px;
}
</style>
