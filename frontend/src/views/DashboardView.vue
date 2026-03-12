<script setup lang="ts">
import { ElMessage } from "element-plus";
import { computed, onMounted, ref } from "vue";
import type { EChartsOption } from "echarts";
import { useRouter } from "vue-router";
import AppChart from "@/components/AppChart.vue";
import { dashboardService, statisticsService } from "@/services";
import { useUserStore } from "@/stores/user";
import type {
  DashboardStatisticsVO,
  HandleSummaryStatisticsVO,
  RiskLevelStatisticsVO,
  WarningTrendStatisticsVO,
  WarningVO,
} from "@/types/risk";
import {
  formatDateTime,
  riskLevelLabel,
  riskLevelTagType,
  warningStatusLabel,
  warningStatusTagType,
} from "@/utils/format";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

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
const riskLevelStats = ref<RiskLevelStatisticsVO[]>([]);
const warningTrend = ref<WarningTrendStatisticsVO[]>([]);
const handleSummary = ref<HandleSummaryStatisticsVO[]>([]);
const showAnalysisCharts = computed(() => userStore.roleCode !== "ADMIN");

const summaryCards = computed(() => [
  {
    label: "风险数据总数",
    value: statistics.value.riskDataCount,
    route: userStore.roleCode === "RISK_USER" ? "/risk/data" : undefined,
    tone: "calm",
  },
  {
    label: "评估记录总数",
    value: statistics.value.assessmentCount,
    route: userStore.roleCode === "RISK_USER" ? "/risk/assessments" : undefined,
    tone: "calm",
  },
  {
    label: "预警总数",
    value: statistics.value.warningCount,
    route: userStore.roleCode === "ADMIN" ? undefined : "/risk/warnings",
    tone: "alert",
  },
  {
    label: "已处理预警",
    value: statistics.value.handledWarningCount,
    route: userStore.roleCode === "ADMIN" ? undefined : "/risk/warnings",
    tone: "success",
  },
  {
    label: "高风险业务",
    value: statistics.value.highRiskCount,
    route: userStore.roleCode === "ADMIN" ? undefined : "/analysis/statistics",
    tone: "danger",
  },
]);

const quickActions = computed(() => {
  if (userStore.roleCode === "ADMIN") {
    return [
      { label: "查看指标规则", route: "/risk/indexes", description: "确认当前评估闭环依赖的指标、权重和评分规则。" },
      { label: "查看用户管理入口", route: "/system/users", description: "该页目前仍是占位，用于说明后续后台能力会放在这里。" },
    ];
  }

  if (userStore.roleCode === "MANAGER") {
    return [
      { label: "查看预警列表", route: "/risk/warnings", description: "从管理视角查看待处理与已处理预警及其处置时间线。" },
      { label: "查看统计分析", route: "/analysis/statistics", description: "查看风险等级分布、预警趋势和处置汇总变化。" },
    ];
  }

  return [
    { label: "录入风险数据", route: "/risk/data", description: "新增业务主记录并填写所有启用指标值。" },
    { label: "执行风险评估", route: "/risk/assessments", description: "基于已落库指标值计算总分并自动触发预警。" },
    { label: "处理预警", route: "/risk/warnings", description: "查看预警详情、填写处理意见并推动状态流转。" },
  ];
});

const roleFocusTitle = computed(() => {
  if (userStore.roleCode === "ADMIN") {
    return "管理员关注点";
  }
  if (userStore.roleCode === "MANAGER") {
    return "管理视角关注点";
  }
  return "风控主线关注点";
});

const roleFocusText = computed(() => {
  if (userStore.roleCode === "ADMIN") {
    return "管理员当前主要负责确认指标和规则口径是否可支撑整条风控闭环，而不是直接参与业务处理。";
  }
  if (userStore.roleCode === "MANAGER") {
    return "管理人员当前重点关注预警数量、高风险占比和处置完成度，用于快速判断系统运行状态。";
  }
  return "风控人员当前最短路径是：录入风险数据 -> 执行评估 -> 处理预警 -> 回到统计页确认数据回流。";
});

// Convert raw counts into a one-sentence briefing so each role can immediately
// understand what deserves attention after login. / 把统计数字转成一句“当前该
// 关注什么”的摘要，让不同角色登录后第一眼就知道下一步重点。
const dashboardInsight = computed(() => {
  const pendingCount = handleSummary.value.find((item) => item.warningStatus === 0)?.count ?? 0;
  const handledCount = handleSummary.value.find((item) => item.warningStatus === 2)?.count ?? 0;

  if (userStore.roleCode === "ADMIN") {
    return `当前演示数据中共有 ${statistics.value.warningCount} 条预警，说明指标规则已经足以驱动后续业务链。`;
  }
  if (pendingCount > 0) {
    return `当前仍有 ${pendingCount} 条预警待处理，优先进入预警管理页查看处置进展。`;
  }
  return `当前预警都已完成处理，累计已处理 ${handledCount} 条，可进入统计页查看趋势变化。`;
});

const riskLevelChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: "item" as const },
  legend: {
    bottom: 0,
    icon: "circle",
    textStyle: { color: "#58738f" },
  },
  series: [
    {
      type: "pie" as const,
      radius: ["48%", "72%"],
      center: ["50%", "42%"],
      label: {
        formatter: "{b}\n{c}",
        color: "#17324d",
      },
      data: riskLevelStats.value.map((item) => ({
        name: riskLevelLabel(item.riskLevel),
        value: item.count,
      })),
      color: ["#2e7d32", "#d97706", "#c62828"],
    },
  ],
}));

const warningTrendChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: "axis" as const },
  legend: {
    top: 0,
    textStyle: { color: "#58738f" },
  },
  grid: {
    left: 16,
    right: 16,
    top: 40,
    bottom: 24,
    containLabel: true,
  },
  xAxis: {
    type: "category" as const,
    data: warningTrend.value.map((item) => item.date.slice(5)),
    axisLine: { lineStyle: { color: "#d8e2eb" } },
    axisLabel: { color: "#58738f" },
  },
  yAxis: {
    type: "value" as const,
    axisLine: { show: false },
    splitLine: { lineStyle: { color: "#eef3f7" } },
    axisLabel: { color: "#58738f" },
  },
  series: [
    {
      name: "预警总数",
      type: "line" as const,
      smooth: true,
      data: warningTrend.value.map((item) => item.total),
      color: "#0f766e",
      areaStyle: {
        color: "rgba(15, 118, 110, 0.12)",
      },
    },
    {
      name: "已处理",
      type: "line" as const,
      smooth: true,
      data: warningTrend.value.map((item) => item.handled),
      color: "#2563eb",
    },
  ],
}));

async function loadDashboard() {
  loading.value = true;
  try {
    // Load only the data allowed for the current role. Admin can see the
    // dashboard overview, but detailed analytics remain reserved for business
    // roles. / 按当前角色加载允许看到的数据：管理员可看首页总览，但细粒度统计
    // 仍保留给业务角色，避免首页偷偷绕过权限边界。
    const baseResults = await Promise.all([
      dashboardService.getDashboardStatistics(),
      dashboardService.listRecentWarnings(),
    ]);

    statistics.value = ensureSuccess(baseResults[0]);
    recentWarnings.value = ensureSuccess(baseResults[1]);

    if (showAnalysisCharts.value) {
      const [riskLevelResult, warningTrendResult, handleSummaryResult] = await Promise.all([
        statisticsService.getRiskLevelStatistics({
          startTime: "",
          endTime: "",
          riskLevel: "",
          warningStatus: "",
        }),
        statisticsService.getWarningTrendStatistics({
          startTime: "",
          endTime: "",
          riskLevel: "",
          warningStatus: "",
        }),
        statisticsService.getHandleSummaryStatistics({
          startTime: "",
          endTime: "",
          riskLevel: "",
          warningStatus: "",
        }),
      ]);

      riskLevelStats.value = ensureSuccess(riskLevelResult);
      warningTrend.value = ensureSuccess(warningTrendResult);
      handleSummary.value = ensureSuccess(handleSummaryResult);
    } else {
      riskLevelStats.value = [];
      warningTrend.value = [];
      handleSummary.value = [];
    }
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
          这里汇总了当前 mock 业务链路的核心结果，可以直接作为角色化演示入口。
        </p>
      </div>
      <el-button :loading="loading" @click="loadDashboard">刷新数据</el-button>
    </div>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="15">
        <section class="stat-grid">
          <article
            v-for="item in summaryCards"
            :key="item.label"
            class="section-card stat-card"
            :class="[
              { 'stat-card--clickable': Boolean(item.route) },
              `stat-card--${item.tone}`,
            ]"
            @click="item.route && router.push(item.route)"
          >
            <div class="stat-card__label">{{ item.label }}</div>
            <div class="stat-card__value">{{ item.value }}</div>
            <div class="stat-card__hint">{{ item.route ? "点击进入" : "当前角色仅查看" }}</div>
          </article>
        </section>
      </el-col>
      <el-col :xs="24" :xl="9">
        <el-card class="section-card focus-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>{{ roleFocusTitle }}</span>
              <el-tag type="info">{{ userStore.currentUser?.roleName }}</el-tag>
            </div>
          </template>
          <p class="focus-card__text">{{ roleFocusText }}</p>
          <p class="focus-card__insight">{{ dashboardInsight }}</p>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="10">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>快捷操作</span>
              <el-tag type="warning" effect="plain">角色定制</el-tag>
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
      <el-col :xs="24" :xl="14">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header">
              <span>最近预警</span>
              <el-button
                v-if="userStore.roleCode !== 'ADMIN'"
                link
                type="primary"
                @click="router.push('/risk/warnings')"
              >
                查看全部
              </el-button>
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

    <el-row v-if="showAnalysisCharts" :gutter="20">
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>风险等级分布</template>
          <AppChart :option="riskLevelChartOption" height="280px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>预警趋势</template>
          <AppChart :option="warningTrendChartOption" height="280px" />
        </el-card>
      </el-col>
    </el-row>
    <el-card v-else class="section-card admin-panel" shadow="never">
      <template #header>管理员视角说明</template>
      <div class="admin-panel__grid">
        <div class="admin-panel__item">
          <span>当前职责</span>
          <strong>维护指标规则与系统入口，不直接处理业务预警。</strong>
        </div>
        <div class="admin-panel__item">
          <span>建议下一步</span>
          <strong>进入“指标规则”确认权重与评分区间，再回到首页观察总览变化。</strong>
        </div>
        <div class="admin-panel__item">
          <span>权限边界</span>
          <strong>预警和统计页面对管理员保持关闭，避免越权进入业务处理链。</strong>
        </div>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 6px;
  opacity: 0.9;
}

.stat-card--calm::before {
  background: linear-gradient(180deg, #2563eb, #0ea5e9);
}

.stat-card--alert::before {
  background: linear-gradient(180deg, #d97706, #f59e0b);
}

.stat-card--success::before {
  background: linear-gradient(180deg, #059669, #10b981);
}

.stat-card--danger::before {
  background: linear-gradient(180deg, #c62828, #ef4444);
}

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

.focus-card__text,
.focus-card__insight {
  margin: 0;
}

.focus-card__text {
  color: #47627d;
}

.focus-card__insight {
  margin-top: 14px;
  padding: 14px 16px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(255, 241, 214, 0.7), rgba(255, 255, 255, 0.96));
  color: #8b5e00;
  font-weight: 600;
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

.admin-panel__grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.admin-panel__item {
  display: grid;
  gap: 6px;
  padding: 16px;
  border-radius: 18px;
  background: rgba(247, 250, 252, 0.96);
}

.admin-panel__item span {
  color: #58738f;
  font-size: 13px;
}

@media (max-width: 960px) {
  .admin-panel__grid {
    grid-template-columns: 1fr;
  }
}
</style>
