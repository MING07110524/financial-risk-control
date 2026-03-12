<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, reactive, ref, watch } from "vue";
import { useRoute, useRouter } from "vue-router";
import { assessmentService, riskDataService } from "@/services";
import type { AssessmentDetailVO, AssessmentQuery, AssessmentVO, RiskDataDetailVO } from "@/types/risk";
import { dataStatusLabel, formatDateTime, formatScore, riskLevelLabel, riskLevelTagType, yesNoLabel } from "@/utils/format";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

const route = useRoute();
const router = useRouter();

const loading = ref(false);
const executing = ref(false);
const detailVisible = ref(false);
const total = ref(0);
const records = ref<AssessmentVO[]>([]);
const selectedRiskData = ref<RiskDataDetailVO | null>(null);
const detail = ref<AssessmentDetailVO | null>(null);
// Explain the scoring formula in both Chinese and English so the user can
// understand the result without reading source code. / 使用中英双语说明评分公式，
// 让体验时不需要看代码也能理解总分来源。
const detailScoreExplanation = ref("总分 = 所有命中规则后的原始得分 × 指标权重 ÷ 100，再把各项加权得分求和。");

const query = reactive<AssessmentQuery>({
  businessNo: "",
  riskLevel: "",
  assessmentStatus: "",
  startTime: "",
  endTime: "",
  pageNum: 1,
  pageSize: 10,
  riskDataId: null,
});

const routeRiskDataId = computed(() => {
  const raw = route.query.riskDataId;
  if (typeof raw !== "string") {
    return null;
  }
  const parsed = Number(raw);
  return Number.isNaN(parsed) ? null : parsed;
});
const hasFilters = computed(() =>
  Boolean(query.businessNo || query.riskLevel || query.assessmentStatus !== "" || query.riskDataId),
);
const routeContextText = computed(() => {
  if (!selectedRiskData.value) {
    return "";
  }
  if (selectedRiskData.value.dataStatus === 1) {
    return "这条业务已有有效评估。再次执行评估会让旧的有效记录失效，并生成新的评估记录。";
  }
  if (selectedRiskData.value.dataStatus === 2) {
    return "这条业务已处于待重评状态。执行评估后会生成新的有效评估，并恢复为已评估状态。";
  }
  return "这条业务还没有评估记录，可以直接执行首次评估。";
});

function assessmentStatusLabel(status: 0 | 1): string {
  return status === 1 ? "当前有效" : "已失效";
}

function assessmentStatusTagType(status: 0 | 1): "success" | "info" {
  return status === 1 ? "success" : "info";
}

function buildExecuteConfirmMessage(riskData: RiskDataDetailVO): string {
  if (riskData.dataStatus === 1) {
    return `业务 ${riskData.businessNo} 当前已有有效评估。继续后，旧的有效评估会失效，并生成新的评估结果。是否继续？`;
  }
  if (riskData.dataStatus === 2) {
    return `业务 ${riskData.businessNo} 当前处于待重评状态。继续后会基于最新指标值生成新的有效评估。是否继续？`;
  }
  return `确认对业务 ${riskData.businessNo} 执行首次评估吗？`;
}

async function loadSelectedRiskData(riskDataId: number | null) {
  if (!riskDataId) {
    selectedRiskData.value = null;
    query.riskDataId = null;
    return;
  }

  query.riskDataId = riskDataId;
  try {
    selectedRiskData.value = ensureSuccess(await riskDataService.getRiskDataDetail(riskDataId));
  } catch (error) {
    selectedRiskData.value = null;
    ElMessage.error(getErrorMessage(error));
  }
}

async function loadAssessments() {
  loading.value = true;
  try {
    const page = ensureSuccess(await assessmentService.pageAssessments(query));
    total.value = page.total;
    records.value = page.records;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

async function bootstrap() {
  const routeBusinessNo = typeof route.query.businessNo === "string" ? route.query.businessNo : "";
  if (routeBusinessNo) {
    query.businessNo = routeBusinessNo;
  }
  await loadSelectedRiskData(routeRiskDataId.value);
  await loadAssessments();
}

function handleSearch() {
  query.pageNum = 1;
  void loadAssessments();
}

function clearRouteContext() {
  query.riskDataId = null;
  selectedRiskData.value = null;
  void router.replace({ path: "/risk/assessments" });
}

async function openDetail(id: number) {
  try {
    detail.value = ensureSuccess(await assessmentService.getAssessmentDetail(id));
    detailVisible.value = true;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  }
}

async function executeAssessment(riskDataId: number) {
  try {
    // Reuse the selected risk data when possible so the confirm copy matches
    // the exact business state the user is looking at. / 优先复用当前上下文中的
    // 业务数据，确保确认弹窗里的说明与用户眼前看到的状态保持一致。
    const riskData =
      selectedRiskData.value?.id === riskDataId
        ? selectedRiskData.value
        : ensureSuccess(await riskDataService.getRiskDataDetail(riskDataId));

    await ElMessageBox.confirm(
      buildExecuteConfirmMessage(riskData),
      "执行评估确认",
      {
        type: "warning",
        confirmButtonText: "确认执行",
        cancelButtonText: "取消",
      },
    );
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(getErrorMessage(error));
    }
    return;
  }

  executing.value = true;
  try {
    // A new assessment should immediately refresh both the route context and
    // the assessment list, because the latest run invalidates old effective
    // records. / 一次新评估会立刻影响当前业务上下文和评估列表，因为旧的有效记录
    // 会被置为失效，所以这里要同步刷新两个区域。
    const result = ensureSuccess(await assessmentService.executeAssessment(riskDataId));
    detail.value = result;
    detailVisible.value = true;
    ElMessage.success("评估已完成");
    await loadSelectedRiskData(routeRiskDataId.value ?? riskDataId);
    await loadAssessments();
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    executing.value = false;
  }
}

onMounted(() => {
  void bootstrap();
});

watch(
  () => route.query.riskDataId,
  async () => {
    await loadSelectedRiskData(routeRiskDataId.value);
    await loadAssessments();
  },
);
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">风险评估</h1>
        <p class="page-subtitle">评估只基于已落库指标值执行，新的评估会使旧有效记录失效。</p>
      </div>
    </div>

    <el-card v-if="selectedRiskData" class="section-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span>当前业务上下文</span>
          <el-button link type="primary" @click="clearRouteContext">清除筛选</el-button>
        </div>
      </template>
      <div class="context-grid">
        <div class="context-item">
          <span>业务编号</span>
          <strong>{{ selectedRiskData.businessNo }}</strong>
        </div>
        <div class="context-item">
          <span>客户名称</span>
          <strong>{{ selectedRiskData.customerName }}</strong>
        </div>
        <div class="context-item">
          <span>业务类型</span>
          <strong>{{ selectedRiskData.businessType }}</strong>
        </div>
        <div class="context-item">
          <span>当前状态</span>
          <strong>{{ dataStatusLabel(selectedRiskData.dataStatus) }}</strong>
        </div>
      </div>
      <el-alert
        class="context-alert"
        type="info"
        :closable="false"
        :title="routeContextText"
      />
      <div class="context-actions">
        <el-button type="primary" :loading="executing" @click="executeAssessment(selectedRiskData.id)">执行评估</el-button>
        <el-button @click="router.push('/risk/data')">返回风险数据</el-button>
      </div>
    </el-card>

    <el-card class="section-card" shadow="never">
      <div class="filter-grid">
        <el-input v-model="query.businessNo" placeholder="业务编号" clearable />
        <el-select v-model="query.riskLevel" placeholder="风险等级" clearable>
          <el-option label="低风险" value="LOW" />
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="高风险" value="HIGH" />
        </el-select>
        <el-select v-model="query.assessmentStatus" placeholder="评估状态" clearable>
          <el-option label="当前有效" :value="1" />
          <el-option label="已失效" :value="0" />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="clearRouteContext">清除业务上下文</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="records" style="width: 100%; margin-top: 18px;">
        <template #empty>
          <el-empty :description="hasFilters ? '当前筛选条件下没有匹配的评估记录' : '还没有评估记录，先从风险数据页或当前业务上下文执行一次评估吧'">
            <el-button v-if="hasFilters" @click="clearRouteContext">清除筛选</el-button>
            <el-button v-else type="primary" @click="router.push('/risk/data')">前往风险数据</el-button>
          </el-empty>
        </template>
        <el-table-column prop="businessNo" label="业务编号" min-width="150" />
        <el-table-column prop="customerName" label="客户名称" min-width="150" />
        <el-table-column label="总分" min-width="100">
          <template #default="{ row }">
            {{ formatScore(row.totalScore) }}
          </template>
        </el-table-column>
        <el-table-column label="风险等级" min-width="110">
          <template #default="{ row }">
            <el-tag :type="riskLevelTagType(row.riskLevel)">{{ riskLevelLabel(row.riskLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="评估状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="assessmentStatusTagType(row.assessmentStatus)">{{ assessmentStatusLabel(row.assessmentStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="生成预警" min-width="100">
          <template #default="{ row }">
            {{ yesNoLabel(row.warningGenerated) }}
          </template>
        </el-table-column>
        <el-table-column label="评估时间" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.assessmentTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="assessmentByName" label="评估人" min-width="110" />
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
              <el-button link type="warning" @click="executeAssessment(row.riskDataId)">再次评估</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-shell">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          background
          layout="total, prev, pager, next"
          :total="total"
          @current-change="loadAssessments"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="评估详情" width="920px" destroy-on-close>
      <div v-if="detail" class="detail-shell">
        <div class="detail-grid">
          <div class="detail-item">
            <span>业务编号</span>
            <strong>{{ detail.businessNo }}</strong>
          </div>
          <div class="detail-item">
            <span>客户名称</span>
            <strong>{{ detail.customerName }}</strong>
          </div>
          <div class="detail-item">
            <span>总分</span>
            <strong>{{ formatScore(detail.totalScore) }}</strong>
          </div>
          <div class="detail-item">
            <span>风险等级</span>
            <strong>{{ riskLevelLabel(detail.riskLevel) }}</strong>
          </div>
          <div class="detail-item">
            <span>评估状态</span>
            <strong>{{ assessmentStatusLabel(detail.assessmentStatus) }}</strong>
          </div>
          <div class="detail-item">
            <span>评估时间</span>
            <strong>{{ formatDateTime(detail.assessmentTime) }}</strong>
          </div>
          <div class="detail-item">
            <span>评估人</span>
            <strong>{{ detail.assessmentByName }}</strong>
          </div>
          <div class="detail-item detail-item--full">
            <span>风险说明</span>
            <strong>{{ detail.riskDesc }}</strong>
          </div>
        </div>

        <el-alert
          v-if="detail.assessmentStatus === 0"
          type="info"
          :closable="false"
          title="这条评估记录已经失效，仅用于保留历史。通常是因为该业务后来被重新评估，或评估前的业务数据发生了修改。"
        />

        <el-alert
          v-if="detail.warningInfo"
          :title="`本次评估已生成 ${riskLevelLabel(detail.warningInfo.warningLevel)} 预警：${detail.warningInfo.warningCode}`"
          type="warning"
          :closable="false"
        />
        <el-alert
          v-else
          title="本次评估未触发预警。"
          type="success"
          :closable="false"
        />

        <el-card class="score-card" shadow="never">
          <template #header>评分说明</template>
          <p class="score-card__text">{{ detailScoreExplanation }}</p>
          <p class="score-card__text">风险等级映射：低风险 &lt; 60，中风险 60-79.99，高风险 80-100。</p>
        </el-card>

        <el-table :data="detail.indexResults" style="width: 100%;">
          <el-table-column prop="indexName" label="指标名称" min-width="150" />
          <el-table-column prop="indexValue" label="指标值" min-width="100" />
          <el-table-column prop="weightValue" label="权重(%)" min-width="100" />
          <el-table-column prop="scoreValue" label="原始得分" min-width="110" />
          <el-table-column prop="weightedScore" label="加权得分" min-width="110" />
          <el-table-column label="说明" min-width="170">
            <template #default="{ row }">
              {{ formatScore(row.scoreValue) }} × {{ row.weightValue }}% = {{ formatScore(row.weightedScore) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.context-grid,
.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.context-item,
.detail-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(247, 250, 252, 0.96);
}

.context-item span,
.detail-item span {
  color: #58738f;
  font-size: 13px;
}

.detail-item--full {
  grid-column: 1 / -1;
}

.context-actions,
.row-actions,
.pagination-shell {
  display: flex;
}

.context-actions,
.row-actions {
  gap: 10px;
}

.context-actions {
  margin-top: 18px;
}

.context-alert {
  margin-top: 18px;
}

.pagination-shell {
  justify-content: flex-end;
  margin-top: 18px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) auto;
  gap: 12px;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.detail-shell {
  display: grid;
  gap: 16px;
}

.score-card {
  border-radius: 18px;
}

.score-card__text {
  margin: 0 0 8px;
  color: #58738f;
}

.score-card__text:last-child {
  margin-bottom: 0;
}

@media (max-width: 960px) {
  .context-grid,
  .detail-grid,
  .filter-grid {
    grid-template-columns: 1fr;
  }
}
</style>
