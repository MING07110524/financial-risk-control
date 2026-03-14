<script setup lang="ts">
import { ElMessage } from "element-plus";
import { computed, onMounted, reactive, ref } from "vue";
import type { EChartsOption } from "echarts";
import AppChart from "@/components/AppChart.vue";
import { statisticsService } from "@/services";
import type {
  HandleSummaryStatisticsVO,
  RiskLevelStatisticsVO,
  StatisticsQuery,
  WarningStatus,
  WarningTrendStatisticsVO,
} from "@/types/risk";
import { riskLevelLabel, warningStatusLabel, warningStatusTagType } from "@/utils/format";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

const loading = ref(false);
const dateRange = ref<[string, string] | []>([]);
const query = reactive<StatisticsQuery>({
  startTime: "",
  endTime: "",
  riskLevel: "",
  warningStatus: "",
});

const riskLevelStats = ref<RiskLevelStatisticsVO[]>([]);
const warningTrend = ref<WarningTrendStatisticsVO[]>([]);
const handleSummary = ref<HandleSummaryStatisticsVO[]>([]);

const totalRiskCount = computed(() => riskLevelStats.value.reduce((sum, item) => sum + item.count, 0));
const totalWarningCount = computed(() => warningTrend.value.reduce((sum, item) => sum + item.total, 0));
const pendingWarningCount = computed(() => handleSummary.value.find((item) => item.warningStatus === 0)?.count ?? 0);
const handledWarningCount = computed(() => handleSummary.value.find((item) => item.warningStatus === 2)?.count ?? 0);
const hasFilters = computed(() => Boolean(dateRange.value.length || query.riskLevel || query.warningStatus !== ""));

function syncDateRangeToQuery() {
  if (dateRange.value.length === 2) {
    query.startTime = dateRange.value[0];
    query.endTime = dateRange.value[1];
  } else {
    query.startTime = "";
    query.endTime = "";
  }
}

async function loadStatistics() {
  // Keep all charts sourced from the same query object so filtering one view
  // never makes different panels disagree with each other. / 让所有图表都从同一份
  // 查询条件取数，避免筛选后不同面板之间出现口径不一致。
  syncDateRangeToQuery();
  loading.value = true;
  try {
    const [levels, trend, summary] = await Promise.all([
      statisticsService.getRiskLevelStatistics(query),
      statisticsService.getWarningTrendStatistics(query),
      statisticsService.getHandleSummaryStatistics(query),
    ]);
    riskLevelStats.value = ensureSuccess(levels);
    warningTrend.value = ensureSuccess(trend);
    handleSummary.value = ensureSuccess(summary);
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function handleReset() {
  dateRange.value = [];
  query.startTime = "";
  query.endTime = "";
  query.riskLevel = "";
  query.warningStatus = "";
  void loadStatistics();
}

function getRatio(value: number, total: number): number {
  if (total === 0) {
    return 0;
  }
  return Number(((value / total) * 100).toFixed(2));
}

function statusTagType(status: WarningStatus) {
  return warningStatusTagType(status);
}

const riskLevelChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: "item" as const },
  legend: {
    orient: "vertical",
    right: 0,
    top: "middle",
    icon: "circle",
    textStyle: { color: "#58738f" },
  },
  series: [
    {
      type: "pie" as const,
      radius: ["42%", "72%"],
      center: ["34%", "50%"],
      label: {
        formatter: "{b}\n{d}%",
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
    left: 20,
    right: 20,
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
      symbolSize: 8,
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
      symbolSize: 8,
      data: warningTrend.value.map((item) => item.handled),
      color: "#2563eb",
    },
    {
      name: "未完成",
      type: "line" as const,
      smooth: true,
      symbolSize: 8,
      data: warningTrend.value.map((item) => item.pending),
      color: "#f59e0b",
    },
  ],
}));

const handleSummaryChartOption = computed<EChartsOption>(() => ({
  tooltip: { trigger: "item" as const },
  series: [
    {
      type: "pie" as const,
      radius: ["45%", "68%"],
      center: ["50%", "46%"],
      label: {
        formatter: "{b}\n{c}",
        color: "#17324d",
      },
      data: handleSummary.value.map((item) => ({
        name: warningStatusLabel(item.warningStatus),
        value: item.count,
      })),
      color: ["#ef4444", "#f59e0b", "#10b981"],
    },
  ],
}));

// Keep the headline insight readable for a non-technical audience as well as
// developers. / 用一句话概括当前统计状态，让答辩或演示时不需要先解释图表。
const analyticsInsight = computed(() => {
  if (totalWarningCount.value === 0) {
    return "当前没有预警，系统处于平稳状态。";
  }
  if (pendingWarningCount.value > 0) {
    return `当前共有 ${totalWarningCount.value} 条预警，其中 ${pendingWarningCount.value} 条尚未完成处理，建议优先跟进。`;
  }
  return `当前共有 ${totalWarningCount.value} 条预警，且都已完成处理，可继续关注风险等级分布变化。`;
});

onMounted(() => {
  void loadStatistics();
});
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">统计分析</h1>
        <p class="page-subtitle">统计数据直接来自真实业务记录，处理预警后这里会同步变化。</p>
      </div>
      <el-button :loading="loading" @click="loadStatistics">刷新统计</el-button>
    </div>

    <el-card class="section-card analytics-banner" shadow="never">
      <div class="analytics-banner__content">
        <div>
          <h2 class="analytics-banner__title">当前统计洞察</h2>
          <p class="analytics-banner__text">{{ analyticsInsight }}</p>
        </div>
        <el-tag type="warning" effect="dark">{{ hasFilters ? "已按筛选条件查看" : "全量视图" }}</el-tag>
      </div>
    </el-card>

    <el-card class="section-card" shadow="never">
      <div class="filter-grid">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%;"
        />
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable>
          <el-option label="低风险" value="LOW" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="高风险" value="HIGH" />
        </el-select>
        <el-select v-model="query.warningStatus" placeholder="预警状态" clearable>
          <el-option label="待处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已处理" :value="2" />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="loadStatistics">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>
    </el-card>

    <section class="stat-grid">
      <article class="section-card stat-card stat-card--navy">
        <div class="stat-card__label">有效评估总数</div>
        <div class="stat-card__value">{{ totalRiskCount }}</div>
        <div class="stat-card__note">用于观察风险等级分布基础盘面</div>
      </article>
      <article class="section-card stat-card stat-card--teal">
        <div class="stat-card__label">预警总数</div>
        <div class="stat-card__value">{{ totalWarningCount }}</div>
        <div class="stat-card__note">包含处理中与已处理的全部预警</div>
      </article>
      <article class="section-card stat-card stat-card--amber">
        <div class="stat-card__label">待处理预警</div>
        <div class="stat-card__value">{{ pendingWarningCount }}</div>
        <div class="stat-card__note">优先跟进这些预警项</div>
      </article>
      <article class="section-card stat-card stat-card--green">
        <div class="stat-card__label">已处理预警</div>
        <div class="stat-card__value">{{ handledWarningCount }}</div>
        <div class="stat-card__note">已完成处置闭环的预警数量</div>
      </article>
    </section>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="8">
        <el-card class="section-card" shadow="never">
          <template #header>风险等级分布</template>
          <el-empty v-if="totalRiskCount === 0" description="当前筛选条件下暂无风险等级分布" />
          <AppChart v-else :option="riskLevelChartOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card class="section-card" shadow="never">
          <template #header>预警趋势</template>
          <el-empty v-if="totalWarningCount === 0" description="当前筛选条件下暂无预警趋势数据" />
          <AppChart v-else :option="warningTrendChartOption" height="300px" />
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card class="section-card" shadow="never">
          <template #header>处置汇总</template>
          <el-empty v-if="handleSummary.length === 0" description="当前筛选条件下暂无处置汇总数据" />
          <AppChart v-else :option="handleSummaryChartOption" height="300px" />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>风险等级占比说明</template>
          <el-empty v-if="riskLevelStats.length === 0" description="当前筛选条件下暂无风险等级占比说明" />
          <div v-else class="summary-list">
            <div v-for="item in riskLevelStats" :key="item.riskLevel" class="summary-item">
              <div class="summary-item__row">
                <strong>{{ riskLevelLabel(item.riskLevel) }}</strong>
                <span>{{ item.count }} 条 · {{ getRatio(item.count, totalRiskCount) }}%</span>
              </div>
              <el-progress :percentage="getRatio(item.count, totalRiskCount)" :stroke-width="10" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>预警处置明细</template>
          <el-empty v-if="handleSummary.length === 0" description="当前筛选条件下暂无预警处置明细" />
          <div v-else class="summary-list">
            <div v-for="item in handleSummary" :key="item.warningStatus" class="summary-item summary-item--plain">
              <div class="summary-item__row">
                <el-tag :type="statusTagType(item.warningStatus)">{{ warningStatusLabel(item.warningStatus) }}</el-tag>
                <strong>{{ item.count }} 条</strong>
              </div>
              <span class="summary-item__caption">
                {{
                  item.warningStatus === 0
                    ? "尚未进入处理动作"
                    : item.warningStatus === 1
                      ? "已有处理动作，但尚未结案"
                      : "已完成处置并保留历史记录"
                }}
              </span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.analytics-banner__content {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.analytics-banner__title {
  margin: 0;
  color: #0f2742;
}

.analytics-banner__text {
  margin: 8px 0 0;
  color: #58738f;
}

.filter-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr auto;
  gap: 12px;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 6px;
}

.stat-card--navy::before {
  background: linear-gradient(180deg, #1d4ed8, #2563eb);
}

.stat-card--teal::before {
  background: linear-gradient(180deg, #0f766e, #14b8a6);
}

.stat-card--amber::before {
  background: linear-gradient(180deg, #d97706, #f59e0b);
}

.stat-card--green::before {
  background: linear-gradient(180deg, #059669, #10b981);
}

.stat-card__note {
  margin-top: 10px;
  color: #58738f;
  font-size: 13px;
}

.summary-list {
  display: grid;
  gap: 16px;
}

.summary-item {
  display: grid;
  gap: 10px;
}

.summary-item--plain {
  padding: 16px;
  border-radius: 16px;
  background: rgba(247, 250, 252, 0.96);
}

.summary-item__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.summary-item__caption {
  color: #58738f;
  font-size: 13px;
}

@media (max-width: 960px) {
  .analytics-banner__content,
  .filter-grid {
    grid-template-columns: 1fr;
    display: grid;
  }
}
</style>
