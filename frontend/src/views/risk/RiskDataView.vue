<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { riskDataService, riskIndexService } from "@/services";
import type {
  RiskDataCreateDTO,
  RiskDataDetailVO,
  RiskDataQuery,
  RiskDataStatus,
  RiskDataVO,
  RiskIndexVO,
} from "@/types/risk";
import { dataStatusLabel, dataStatusTagType, formatDateTime } from "@/utils/format";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

interface RiskDataFormIndexItem {
  indexId: number;
  indexName: string;
  indexCode: string;
  weightValue: number;
  indexValue: number | null;
}

const router = useRouter();

const loading = ref(false);
const submitting = ref(false);
const dialogVisible = ref(false);
const detailVisible = ref(false);
const formMode = ref<"create" | "edit">("create");
const total = ref(0);
const records = ref<RiskDataVO[]>([]);
const enabledIndexes = ref<RiskIndexVO[]>([]);
const detail = ref<RiskDataDetailVO | null>(null);
const currentEditId = ref<number | null>(null);
const dialogRecordStatus = ref<RiskDataStatus | null>(null);
const formSnapshot = ref("");
const skipNextDialogGuard = ref(false);

const query = reactive<RiskDataQuery>({
  businessNo: "",
  customerName: "",
  businessType: "",
  dataStatus: "",
  pageNum: 1,
  pageSize: 10,
});

const form = reactive({
  businessNo: "",
  customerName: "",
  businessType: "",
  riskDesc: "",
  indexValues: [] as RiskDataFormIndexItem[],
});

const dialogTitle = computed(() => (formMode.value === "create" ? "新增风险数据" : "编辑风险数据"));
const hasFilters = computed(() =>
  Boolean(query.businessNo || query.customerName || query.businessType || query.dataStatus !== ""),
);
const hasUnsavedChanges = computed(() => dialogVisible.value && formSnapshot.value !== createFormSnapshot());
const showReassessmentHint = computed(() => formMode.value === "edit" && dialogRecordStatus.value === 1);

function createEmptyIndexValues(): RiskDataFormIndexItem[] {
  return enabledIndexes.value.map((item) => ({
    indexId: item.id,
    indexName: item.indexName,
    indexCode: item.indexCode,
    weightValue: item.weightValue,
    indexValue: null,
  }));
}

function resetFormState() {
  currentEditId.value = null;
  dialogRecordStatus.value = null;
  form.businessNo = "";
  form.customerName = "";
  form.businessType = "";
  form.riskDesc = "";
  form.indexValues = createEmptyIndexValues();
}

function createFormSnapshot(): string {
  return JSON.stringify({
    businessNo: form.businessNo.trim(),
    customerName: form.customerName.trim(),
    businessType: form.businessType.trim(),
    riskDesc: form.riskDesc.trim(),
    indexValues: form.indexValues.map((item) => ({
      indexId: item.indexId,
      indexValue: item.indexValue,
    })),
  });
}

function syncFormSnapshot() {
  formSnapshot.value = createFormSnapshot();
}

function buildGoAssessLabel(status: RiskDataStatus): string {
  if (status === 1) {
    return "重新评估";
  }
  if (status === 2) {
    return "立即重评";
  }
  return "去评估";
}

async function handleDialogBeforeClose(done: () => void) {
  // Guard unsaved changes before closing so the user does not accidentally
  // lose a half-finished business record. / 在关闭弹窗前拦截未保存改动，避免用户
  // 不小心丢失一条尚未填写完成的业务数据。
  if (skipNextDialogGuard.value || !hasUnsavedChanges.value) {
    done();
    return;
  }

  try {
    await ElMessageBox.confirm(
      "当前表单还有未保存的改动，关闭后将丢失。确认继续关闭吗？",
      "放弃未保存内容",
      {
        type: "warning",
        confirmButtonText: "放弃修改",
        cancelButtonText: "继续编辑",
      },
    );
    done();
  } catch {
    return;
  }
}

function handleDialogClosed() {
  skipNextDialogGuard.value = false;
  formSnapshot.value = "";
  resetFormState();
}

async function closeDialog() {
  if (hasUnsavedChanges.value) {
    try {
      await ElMessageBox.confirm(
        "当前表单还有未保存的改动，关闭后将丢失。确认继续关闭吗？",
        "放弃未保存内容",
        {
          type: "warning",
          confirmButtonText: "放弃修改",
          cancelButtonText: "继续编辑",
        },
      );
    } catch {
      return;
    }
  }

  skipNextDialogGuard.value = true;
  dialogVisible.value = false;
}

async function promptNextStep(savedData: RiskDataDetailVO, mode: "create" | "edit") {
  // Offer the next logical action right after save, because risk data entry is
  // usually followed by assessment in this demo flow. / 保存后直接提供“去评估”
  // 是为了让录入 -> 评估这条最小闭环更顺，不需要用户自己再回列表找按钮。
  const title = mode === "create" ? "风险数据已保存" : "风险数据已更新";
  const message =
    savedData.dataStatus === 2
      ? `业务 ${savedData.businessNo} 已更新，并自动标记为“待重评”。是否立即前往评估？`
      : `业务 ${savedData.businessNo} 已保存。是否立即前往评估？`;

  try {
    await ElMessageBox.confirm(message, title, {
      type: "success",
      confirmButtonText: "去评估",
      cancelButtonText: "留在列表",
      distinguishCancelAndClose: true,
    });
    goToAssess(savedData);
  } catch {
    return;
  }
}

function toPayload(): RiskDataCreateDTO | null {
  if (!form.businessNo.trim()) {
    ElMessage.warning("请输入业务编号");
    return null;
  }
  if (!form.customerName.trim()) {
    ElMessage.warning("请输入客户名称");
    return null;
  }
  if (!form.businessType.trim()) {
    ElMessage.warning("请输入业务类型");
    return null;
  }
  if (!form.riskDesc.trim()) {
    ElMessage.warning("请输入风险说明");
    return null;
  }
  if (form.indexValues.some((item) => item.indexValue === null)) {
    ElMessage.warning("请补全所有启用指标值后再保存");
    return null;
  }

  return {
    businessNo: form.businessNo.trim(),
    customerName: form.customerName.trim(),
    businessType: form.businessType.trim(),
    riskDesc: form.riskDesc.trim(),
    indexValues: form.indexValues.map((item) => ({
      indexId: item.indexId,
      indexValue: item.indexValue,
    })),
  };
}

async function loadEnabledIndexes() {
  enabledIndexes.value = ensureSuccess(await riskIndexService.listRiskIndexes({ status: 1 }));
  if (form.indexValues.length === 0) {
    form.indexValues = createEmptyIndexValues();
  }
}

async function loadRiskData() {
  loading.value = true;
  try {
    const page = ensureSuccess(await riskDataService.pageRiskData(query));
    total.value = page.total;
    records.value = page.records;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

async function bootstrap() {
  loading.value = true;
  try {
    await loadEnabledIndexes();
    const page = ensureSuccess(await riskDataService.pageRiskData(query));
    total.value = page.total;
    records.value = page.records;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  void loadRiskData();
}

function handleReset() {
  query.businessNo = "";
  query.customerName = "";
  query.businessType = "";
  query.dataStatus = "";
  query.pageNum = 1;
  void loadRiskData();
}

function openCreateDialog() {
  formMode.value = "create";
  resetFormState();
  syncFormSnapshot();
  dialogVisible.value = true;
}

async function openEditDialog(row: RiskDataVO) {
  try {
    const data = ensureSuccess(await riskDataService.getRiskDataDetail(row.id));
    formMode.value = "edit";
    currentEditId.value = row.id;
    form.businessNo = data.businessNo;
    form.customerName = data.customerName;
    form.businessType = data.businessType;
    form.riskDesc = data.riskDesc;
    dialogRecordStatus.value = data.dataStatus;
    form.indexValues = enabledIndexes.value.map((item) => {
      const current = data.indexValues.find((entry) => entry.indexId === item.id);
      return {
        indexId: item.id,
        indexName: item.indexName,
        indexCode: item.indexCode,
        weightValue: item.weightValue,
        indexValue: current?.indexValue ?? null,
      };
    });
    syncFormSnapshot();
    dialogVisible.value = true;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  }
}

async function openDetailDialog(row: RiskDataVO) {
  try {
    detail.value = ensureSuccess(await riskDataService.getRiskDataDetail(row.id));
    detailVisible.value = true;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  }
}

async function submitForm() {
  const payload = toPayload();
  if (!payload) {
    return;
  }

  submitting.value = true;
  try {
    let savedData: RiskDataDetailVO;
    if (formMode.value === "create") {
      savedData = ensureSuccess(await riskDataService.createRiskData(payload));
      ElMessage.success("风险数据已保存");
    } else if (currentEditId.value !== null) {
      savedData = ensureSuccess(await riskDataService.updateRiskData(currentEditId.value, payload));
      ElMessage.success(savedData.dataStatus === 2 ? "风险数据已更新，当前状态变为待重评" : "风险数据已更新");
    } else {
      return;
    }

    skipNextDialogGuard.value = true;
    dialogVisible.value = false;
    await loadRiskData();
    await promptNextStep(savedData, formMode.value);
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    submitting.value = false;
  }
}

async function removeRiskData(row: RiskDataVO) {
  try {
    await ElMessageBox.confirm(
      `确认删除业务 ${row.businessNo} 吗？已有评估或预警历史的数据将被拒绝删除。`,
      "删除确认",
      {
        type: "warning",
        confirmButtonText: "确认删除",
        cancelButtonText: "取消",
      },
    );

    await riskDataService.deleteRiskData(row.id).then(ensureSuccess);
    ElMessage.success("删除成功");
    await loadRiskData();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(getErrorMessage(error));
    }
  }
}

function goToAssess(row: RiskDataVO) {
  void router.push({
    path: "/risk/assessments",
    query: {
      riskDataId: `${row.id}`,
      businessNo: row.businessNo,
    },
  });
}

onMounted(() => {
  void bootstrap();
});
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">风险数据</h1>
        <p class="page-subtitle">
          风控人员在这里维护业务主记录，并为每条业务填写所有启用指标值。
        </p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增风险数据</el-button>
    </div>

    <el-card class="section-card" shadow="never">
      <div class="filter-grid">
        <el-input v-model="query.businessNo" placeholder="业务编号" clearable />
        <el-input v-model="query.customerName" placeholder="客户名称" clearable />
        <el-input v-model="query.businessType" placeholder="业务类型" clearable />
        <el-select v-model="query.dataStatus" placeholder="数据状态" clearable>
          <el-option label="待评估" :value="0" />
          <el-option label="已评估" :value="1" />
          <el-option label="待重评" :value="2" />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="records" style="width: 100%; margin-top: 18px;">
        <template #empty>
          <el-empty :description="hasFilters ? '当前筛选条件下没有匹配的风险数据' : '还没有风险数据，先创建第一条业务记录吧'">
            <el-button v-if="hasFilters" @click="handleReset">清空筛选</el-button>
            <el-button v-else type="primary" @click="openCreateDialog">新增风险数据</el-button>
          </el-empty>
        </template>
        <el-table-column prop="businessNo" label="业务编号" min-width="150" />
        <el-table-column prop="customerName" label="客户名称" min-width="160" />
        <el-table-column prop="businessType" label="业务类型" min-width="140" />
        <el-table-column prop="riskDesc" label="风险说明" min-width="220" show-overflow-tooltip />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="dataStatusTagType(row.dataStatus)">{{ dataStatusLabel(row.dataStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createByName" label="录入人" min-width="110" />
        <el-table-column label="录入时间" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="270" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetailDialog(row)">详情</el-button>
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button link type="warning" @click="goToAssess(row)">{{ buildGoAssessLabel(row.dataStatus) }}</el-button>
              <el-button link type="danger" @click="removeRiskData(row)">删除</el-button>
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
          @current-change="loadRiskData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="900px"
      destroy-on-close
      :before-close="handleDialogBeforeClose"
      @closed="handleDialogClosed"
    >
      <el-form label-position="top">
        <el-alert
          v-if="showReassessmentHint"
          type="warning"
          :closable="false"
          class="dialog-alert"
          title="当前业务已有有效评估记录。保存修改后，这条业务会自动进入“待重评”状态。"
        />
        <el-alert
          v-else-if="formMode === 'create'"
          type="info"
          :closable="false"
          class="dialog-alert"
          title="保存后可以直接跳转到评估页，完成这条业务的下一步处理。"
        />
        <el-row :gutter="16">
          <el-col :md="12" :xs="24">
            <el-form-item label="业务编号">
              <el-input v-model="form.businessNo" :disabled="formMode === 'edit'" placeholder="例如 FRC-202603-006" />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="客户名称">
              <el-input v-model="form.customerName" placeholder="请输入客户名称" />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="业务类型">
              <el-input v-model="form.businessType" placeholder="请输入业务类型" />
            </el-form-item>
          </el-col>
          <el-col :md="12" :xs="24">
            <el-form-item label="风险说明">
              <el-input v-model="form.riskDesc" placeholder="简要描述风险背景" />
            </el-form-item>
          </el-col>
        </el-row>

        <div class="index-panel">
          <div class="index-panel__header">
            <strong>指标值明细</strong>
            <span>当前启用 {{ enabledIndexes.length }} 个指标，保存时必须全部填写。</span>
          </div>
          <div class="index-grid">
            <div v-for="item in form.indexValues" :key="item.indexId" class="index-item">
              <div class="index-item__meta">
                <strong>{{ item.indexName }}</strong>
                <span>{{ item.indexCode }} · 权重 {{ item.weightValue }}%</span>
              </div>
              <el-input-number
                v-model="item.indexValue"
                :precision="2"
                :step="1"
                :min="0"
                controls-position="right"
                placeholder="请输入指标值"
                style="width: 100%;"
              />
            </div>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="closeDialog">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="风险数据详情" width="820px" destroy-on-close>
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
            <span>业务类型</span>
            <strong>{{ detail.businessType }}</strong>
          </div>
          <div class="detail-item">
            <span>数据状态</span>
            <strong>{{ dataStatusLabel(detail.dataStatus) }}</strong>
          </div>
          <div class="detail-item">
            <span>录入人</span>
            <strong>{{ detail.createByName }}</strong>
          </div>
          <div class="detail-item">
            <span>更新时间</span>
            <strong>{{ formatDateTime(detail.updateTime) }}</strong>
          </div>
          <div class="detail-item detail-item--full">
            <span>风险说明</span>
            <strong>{{ detail.riskDesc }}</strong>
          </div>
        </div>

        <el-alert
          v-if="detail.dataStatus === 2"
          type="warning"
          :closable="false"
          class="dialog-alert"
          title="这条业务已处于待重评状态，建议从列表点击“立即重评”继续处理。"
        />

        <el-table :data="detail.indexValues" style="width: 100%; margin-top: 18px;">
          <el-table-column prop="indexName" label="指标名称" min-width="160" />
          <el-table-column prop="indexCode" label="指标编码" min-width="170" />
          <el-table-column prop="weightValue" label="权重(%)" min-width="110" />
          <el-table-column prop="indexValue" label="指标值" min-width="110" />
        </el-table>

        <div class="detail-actions">
          <el-button type="primary" @click="goToAssess(detail)">去评估</el-button>
          <el-button @click="detailVisible = false">关闭</el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.dialog-alert {
  margin-bottom: 16px;
}

.filter-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr)) auto;
  gap: 12px;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.row-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.pagination-shell {
  display: flex;
  justify-content: flex-end;
  margin-top: 18px;
}

.index-panel {
  display: grid;
  gap: 16px;
  padding-top: 8px;
}

.index-panel__header {
  display: grid;
  gap: 4px;
  color: #58738f;
}

.index-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.index-item {
  display: grid;
  gap: 10px;
  padding: 16px;
  border: 1px solid rgba(15, 39, 66, 0.08);
  border-radius: 18px;
  background: rgba(247, 250, 252, 0.95);
}

.index-item__meta {
  display: grid;
  gap: 4px;
}

.index-item__meta span {
  color: #58738f;
  font-size: 13px;
}

.detail-shell {
  display: grid;
  gap: 16px;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.detail-item {
  display: grid;
  gap: 6px;
  padding: 14px 16px;
  border-radius: 16px;
  background: rgba(247, 250, 252, 0.96);
}

.detail-item span {
  color: #58738f;
  font-size: 13px;
}

.detail-item--full {
  grid-column: 1 / -1;
}

.detail-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 1080px) {
  .filter-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .index-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
