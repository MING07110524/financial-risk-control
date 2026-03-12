<script setup lang="ts">
import { ElMessage } from "element-plus";
import { computed, onMounted, reactive, ref } from "vue";
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

onMounted(() => {
  void loadStatistics();
});
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">统计分析</h1>
        <p class="page-subtitle">统计数据直接来自同一份 mock 业务数据，处理预警后这里会同步变化。</p>
      </div>
      <el-button :loading="loading" @click="loadStatistics">刷新统计</el-button>
    </div>

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
      <article class="section-card stat-card">
        <div class="stat-card__label">有效评估总数</div>
        <div class="stat-card__value">{{ totalRiskCount }}</div>
      </article>
      <article class="section-card stat-card">
        <div class="stat-card__label">预警总数</div>
        <div class="stat-card__value">{{ totalWarningCount }}</div>
      </article>
      <article class="section-card stat-card">
        <div class="stat-card__label">待处理预警</div>
        <div class="stat-card__value">{{ handleSummary.find((item) => item.warningStatus === 0)?.count ?? 0 }}</div>
      </article>
      <article class="section-card stat-card">
        <div class="stat-card__label">已处理预警</div>
        <div class="stat-card__value">{{ handleSummary.find((item) => item.warningStatus === 2)?.count ?? 0 }}</div>
      </article>
    </section>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="8">
        <el-card class="section-card" shadow="never">
          <template #header>风险等级分布</template>
          <div class="summary-list">
            <div v-for="item in riskLevelStats" :key="item.riskLevel" class="summary-item">
              <div class="summary-item__row">
                <strong>{{ riskLevelLabel(item.riskLevel) }}</strong>
                <span>{{ item.count }} 条</span>
              </div>
              <el-progress :percentage="getRatio(item.count, totalRiskCount)" :stroke-width="10" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card class="section-card" shadow="never">
          <template #header>预警趋势</template>
          <el-table :data="warningTrend" size="small">
            <el-table-column prop="date" label="日期" min-width="110" />
            <el-table-column prop="total" label="总数" min-width="70" />
            <el-table-column prop="pending" label="未完成" min-width="80" />
            <el-table-column prop="handled" label="已处理" min-width="80" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="8">
        <el-card class="section-card" shadow="never">
          <template #header>处置汇总</template>
          <div class="summary-list">
            <div v-for="item in handleSummary" :key="item.warningStatus" class="summary-item summary-item--plain">
              <div class="summary-item__row">
                <el-tag :type="statusTagType(item.warningStatus)">{{ warningStatusLabel(item.warningStatus) }}</el-tag>
                <strong>{{ item.count }} 条</strong>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.filter-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr 1fr auto;
  gap: 12px;
}

.filter-actions {
  display: flex;
  gap: 10px;
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

@media (max-width: 960px) {
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
