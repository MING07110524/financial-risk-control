<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, reactive, ref } from "vue";
import { useUserStore } from "@/stores/user";
import { warningService } from "@/services";
import type {
  WarningDetailVO,
  WarningHandleDTO,
  WarningHandleRecordVO,
  WarningQuery,
  WarningVO,
} from "@/types/risk";
import {
  formatDateTime,
  formatScore,
  riskLevelLabel,
  riskLevelTagType,
  warningStatusLabel,
  warningStatusTagType,
} from "@/utils/format";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

const userStore = useUserStore();

const loading = ref(false);
const submitting = ref(false);
const detailVisible = ref(false);
const total = ref(0);
const records = ref<WarningVO[]>([]);
const detail = ref<WarningDetailVO | null>(null);
const handleRecords = ref<WarningHandleRecordVO[]>([]);

const query = reactive<WarningQuery>({
  warningCode: "",
  warningLevel: "",
  warningStatus: "",
  startTime: "",
  endTime: "",
  pageNum: 1,
  pageSize: 10,
});

const handleForm = reactive<WarningHandleDTO>({
  handleOpinion: "",
  handleResult: "",
  nextStatus: 1,
});

// Keep the permission rule visible in the page state so read-only users still
// see the workflow but cannot submit it. / 把权限规则直接反映到页面状态里，
// 让只读角色依然能看完整流程，但不能真正提交处理动作。
const canHandle = computed(() => userStore.roleCode === "RISK_USER" && detail.value?.warningStatus !== 2);
const isManagerReadonly = computed(() => userStore.roleCode === "MANAGER");
const hasFilters = computed(() =>
  Boolean(query.warningCode || query.warningLevel || query.warningStatus !== "" || query.startTime || query.endTime),
);

function warningFlowText(status: WarningVO["warningStatus"]): string {
  if (status === 0) {
    return "当前预警还未开始处理，可以提交“处理中”或直接提交“已处理”。";
  }
  if (status === 1) {
    return "当前预警正在处理中，本次提交可以继续保持处理中，或将其流转为已处理。";
  }
  return "当前预警已经处理完成，只保留历史记录，不允许再次提交处理。";
}

function resetHandleForm() {
  handleForm.handleOpinion = "";
  handleForm.handleResult = "";
  handleForm.nextStatus = 1;
}

async function loadWarnings() {
  loading.value = true;
  try {
    const page = ensureSuccess(await warningService.pageWarnings(query));
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
  void loadWarnings();
}

function handleReset() {
  query.warningCode = "";
  query.warningLevel = "";
  query.warningStatus = "";
  query.startTime = "";
  query.endTime = "";
  query.pageNum = 1;
  void loadWarnings();
}

async function openDetail(id: number) {
  try {
    detail.value = ensureSuccess(await warningService.getWarningDetail(id));
    handleRecords.value = ensureSuccess(await warningService.listWarningRecords(id));
    resetHandleForm();
    detailVisible.value = true;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  }
}

async function submitHandle() {
  if (!detail.value) {
    return;
  }
  if (!handleForm.handleOpinion.trim() || !handleForm.handleResult.trim()) {
    ElMessage.warning("请填写处理意见和处理结果");
    return;
  }

  submitting.value = true;
  try {
    // Ask for an explicit confirmation before changing the warning status so
    // the user understands this action will be written into the timeline.
    // / 在真正更新预警状态前先二次确认，让用户明确这次提交会写入处置时间线。
    await ElMessageBox.confirm(
      handleForm.nextStatus === 2
        ? "本次提交会把预警状态更新为“已处理”，并写入一条新的处理记录。确认继续吗？"
        : "本次提交会写入一条新的处理记录，并保持预警为“处理中”。确认继续吗？",
      "提交处理确认",
      {
        type: "warning",
        confirmButtonText: "确认提交",
        cancelButtonText: "取消",
      },
    );

    await warningService.handleWarning(detail.value.id, handleForm).then(ensureSuccess);
    ElMessage.success(handleForm.nextStatus === 2 ? "预警已处理完成" : "预警处理记录已保存");
    detail.value = ensureSuccess(await warningService.getWarningDetail(detail.value.id));
    handleRecords.value = ensureSuccess(await warningService.listWarningRecords(detail.value.id));
    await loadWarnings();
    resetHandleForm();
  } catch (error) {
    if (error === "cancel") {
      submitting.value = false;
      return;
    }
    ElMessage.error(getErrorMessage(error));
  } finally {
    submitting.value = false;
  }
}

function actionLabel(row: WarningVO): string {
  if (userStore.roleCode === "MANAGER") {
    return "只读查看";
  }
  if (row.warningStatus === 2) {
    return "已处理";
  }
  return "处理";
}

function timelineTagType(status: WarningHandleRecordVO["nextStatus"]) {
  return warningStatusTagType(status);
}

onMounted(() => {
  void loadWarnings();
});
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">预警管理</h1>
        <p class="page-subtitle">风控人员可处理预警，管理人员只读查看预警详情和处置记录。</p>
      </div>
    </div>

    <el-alert
      v-if="isManagerReadonly"
      type="info"
      :closable="false"
      title="当前为管理人员视角：你可以查看预警详情和处理时间线，但不能提交处理。"
    />
    <el-alert
      v-else
      type="success"
      :closable="false"
      title="当前为风控人员视角：可对待处理/处理中预警提交处置意见，并推动状态流转。"
    />

    <el-card class="section-card" shadow="never">
      <div class="filter-grid">
        <el-input v-model="query.warningCode" placeholder="预警编号" clearable />
        <el-select v-model="query.warningLevel" placeholder="预警等级" clearable>
          <el-option label="中风险" value="MEDIUM" />
          <el-option label="高风险" value="HIGH" />
        </el-select>
        <el-select v-model="query.warningStatus" placeholder="预警状态" clearable>
          <el-option label="待处理" :value="0" />
          <el-option label="处理中" :value="1" />
          <el-option label="已处理" :value="2" />
        </el-select>
        <div class="filter-actions">
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </div>
      </div>

      <el-table v-loading="loading" :data="records" style="width: 100%; margin-top: 18px;">
        <template #empty>
          <el-empty :description="hasFilters ? '当前筛选条件下没有匹配的预警记录' : '当前没有预警记录，可先去风险评估页执行一次评估'">
            <el-button v-if="hasFilters" @click="handleReset">清空筛选</el-button>
          </el-empty>
        </template>
        <el-table-column prop="warningCode" label="预警编号" min-width="170" />
        <el-table-column label="预警等级" min-width="110">
          <template #default="{ row }">
            <el-tag :type="riskLevelTagType(row.warningLevel)">{{ riskLevelLabel(row.warningLevel) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="warningContent" label="预警内容" min-width="260" show-overflow-tooltip />
        <el-table-column prop="businessNo" label="业务编号" min-width="150" />
        <el-table-column label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="warningStatusTagType(row.warningStatus)">{{ warningStatusLabel(row.warningStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态说明" min-width="210" show-overflow-tooltip>
          <template #default="{ row }">
            {{ warningFlowText(row.warningStatus) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="170" fixed="right">
          <template #default="{ row }">
            <div class="row-actions">
              <el-button link type="primary" @click="openDetail(row.id)">详情</el-button>
              <el-button
                :disabled="userStore.roleCode === 'MANAGER' || row.warningStatus === 2"
                link
                type="warning"
                @click="openDetail(row.id)"
              >
                {{ actionLabel(row) }}
              </el-button>
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
          @current-change="loadWarnings"
        />
      </div>
    </el-card>

    <el-dialog v-model="detailVisible" title="预警详情" width="980px" destroy-on-close>
      <div v-if="detail" class="detail-shell">
        <div class="detail-grid">
          <div class="detail-item">
            <span>预警编号</span>
            <strong>{{ detail.warningCode }}</strong>
          </div>
          <div class="detail-item">
            <span>预警状态</span>
            <strong>{{ warningStatusLabel(detail.warningStatus) }}</strong>
          </div>
          <div class="detail-item">
            <span>风险等级</span>
            <strong>{{ riskLevelLabel(detail.riskLevel) }}</strong>
          </div>
          <div class="detail-item">
            <span>评估总分</span>
            <strong>{{ formatScore(detail.totalScore) }}</strong>
          </div>
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
            <span>创建时间</span>
            <strong>{{ formatDateTime(detail.createTime) }}</strong>
          </div>
          <div class="detail-item detail-item--full">
            <span>预警内容</span>
            <strong>{{ detail.warningContent }}</strong>
          </div>
          <div class="detail-item detail-item--full">
            <span>风险说明</span>
            <strong>{{ detail.riskDesc }}</strong>
          </div>
        </div>

        <el-alert
          v-if="isManagerReadonly"
          type="info"
          :closable="false"
          title="当前账号为管理人员，只可查看处理过程，不能提交新的处理记录。"
        />
        <el-alert
          v-else-if="detail.warningStatus === 2"
          type="success"
          :closable="false"
          title="这条预警已经处理完成，当前页面仅保留历史记录查看。"
        />
        <el-alert
          v-else
          type="warning"
          :closable="false"
          :title="warningFlowText(detail.warningStatus)"
        />

        <el-card class="sub-card" shadow="never">
          <template #header>处理记录</template>
          <el-empty v-if="handleRecords.length === 0" description="暂无处理记录" />
          <el-timeline v-else>
            <el-timeline-item
              v-for="item in handleRecords"
              :key="item.id"
              :timestamp="formatDateTime(item.handleTime)"
              placement="top"
            >
              <div class="timeline-item">
                <div class="timeline-item__header">
                  <strong>{{ item.handleUserName }}</strong>
                  <el-tag :type="timelineTagType(item.nextStatus)">{{ warningStatusLabel(item.nextStatus) }}</el-tag>
                </div>
                <p>处理意见：{{ item.handleOpinion }}</p>
                <p>处理结果：{{ item.handleResult }}</p>
                <p>状态流转：提交后将预警更新为“{{ warningStatusLabel(item.nextStatus) }}”</p>
              </div>
            </el-timeline-item>
          </el-timeline>
        </el-card>

        <el-card v-if="canHandle" class="sub-card" shadow="never">
          <template #header>提交处理</template>
          <el-form label-position="top">
            <el-alert
              class="handle-alert"
              type="info"
              :closable="false"
              :title="`你当前可以把这条预警更新为“处理中”或“已处理”。${warningFlowText(detail.warningStatus)}`"
            />
            <el-form-item label="处理意见">
              <el-input v-model="handleForm.handleOpinion" type="textarea" :rows="3" placeholder="请输入处理意见" />
            </el-form-item>
            <el-form-item label="处理结果">
              <el-input v-model="handleForm.handleResult" type="textarea" :rows="3" placeholder="请输入处理结果" />
            </el-form-item>
            <el-form-item label="下一状态">
              <el-radio-group v-model="handleForm.nextStatus">
                <el-radio :value="1">处理中</el-radio>
                <el-radio :value="2">已处理</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-button type="primary" :loading="submitting" @click="submitHandle">提交处理</el-button>
          </el-form>
        </el-card>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.filter-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr)) auto;
  gap: 12px;
}

.filter-actions,
.row-actions,
.pagination-shell {
  display: flex;
}

.filter-actions,
.row-actions {
  gap: 10px;
}

.pagination-shell {
  justify-content: flex-end;
  margin-top: 18px;
}

.detail-shell {
  display: grid;
  gap: 18px;
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

.sub-card {
  border-radius: 18px;
}

.timeline-item {
  display: grid;
  gap: 6px;
}

.timeline-item__header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.timeline-item p {
  margin: 0;
  color: #58738f;
}

.handle-alert {
  margin-bottom: 16px;
}

@media (max-width: 960px) {
  .filter-grid,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>
