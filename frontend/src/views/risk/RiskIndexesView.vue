<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, reactive, ref } from "vue";
import { isMockMode, riskIndexService } from "@/services";
import type { FormInstance, FormRules } from "element-plus";
import type {
  RiskIndexCreateDTO,
  RiskIndexUpdateDTO,
  RiskIndexVO,
  RiskRuleCreateDTO,
  RiskRuleUpdateDTO,
  RiskRuleVO,
} from "@/types/risk";
import { riskLevelLabel, riskLevelTagType } from "@/utils/format";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

const loading = ref(false);
const indexes = ref<RiskIndexVO[]>([]);
const selectedIndexId = ref<number | null>(null);
const rules = ref<RiskRuleVO[]>([]);
const saving = ref(false);
const dialogVisible = ref(false);
const editingIndexId = ref<number | null>(null);
const formRef = ref<FormInstance>();
const form = reactive<RiskIndexCreateDTO>({
  indexName: "",
  indexCode: "",
  weightValue: null,
  indexDesc: "",
  status: 0,
});

const ruleSaving = ref(false);
const ruleDialogVisible = ref(false);
const editingRuleId = ref<number | null>(null);
const ruleFormRef = ref<FormInstance>();
const ruleForm = reactive<RiskRuleCreateDTO>({
  indexId: 0,
  scoreMin: null,
  scoreMax: null,
  scoreValue: null,
  warningLevel: "LOW",
});

const enabledIndexes = computed(() => indexes.value.filter((item) => item.status === 1));
const enabledWeightTotal = computed(() =>
  enabledIndexes.value.reduce((sum, item) => sum + Number(item.weightValue ?? 0), 0),
);
const selectedIndex = computed(() => indexes.value.find((item) => item.id === selectedIndexId.value) ?? null);

const rulesConfig: FormRules<RiskIndexCreateDTO> = {
  indexName: [{ required: true, message: "请输入指标名称", trigger: "blur" }],
  indexCode: [{ required: true, message: "请输入指标编码", trigger: "blur" }],
  weightValue: [{ required: true, message: "请输入权重", trigger: "change" }],
  indexDesc: [{ required: true, message: "请输入指标说明", trigger: "blur" }],
};

const ruleRulesConfig: FormRules<RiskRuleCreateDTO> = {
  scoreMin: [{ required: true, message: "请输入最小值", trigger: "change" }],
  scoreMax: [{ required: true, message: "请输入最大值", trigger: "change" }],
  scoreValue: [{ required: true, message: "请输入原始得分", trigger: "change" }],
  warningLevel: [{ required: true, message: "请选择预警等级", trigger: "change" }],
};

async function loadRules(indexId: number) {
  rules.value = ensureSuccess(await riskIndexService.listRiskRules(indexId));
}

async function loadIndexes() {
  loading.value = true;
  try {
    indexes.value = ensureSuccess(await riskIndexService.listRiskIndexes());
    const firstIndex = indexes.value[0];
    if (!selectedIndexId.value && firstIndex) {
      selectedIndexId.value = firstIndex.id;
    }
    if (selectedIndexId.value !== null) {
      await loadRules(selectedIndexId.value);
    }
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

async function handleSelectIndex(indexId: number) {
  selectedIndexId.value = indexId;
  try {
    await loadRules(indexId);
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  }
}

function resetForm() {
  editingIndexId.value = null;
  form.indexName = "";
  form.indexCode = "";
  form.weightValue = null;
  form.indexDesc = "";
  form.status = 0;
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(row: RiskIndexVO) {
  editingIndexId.value = row.id;
  form.indexName = row.indexName;
  form.indexCode = row.indexCode;
  form.weightValue = Number(row.weightValue);
  form.indexDesc = row.indexDesc;
  form.status = row.status;
  dialogVisible.value = true;
}

async function submitIndexForm() {
  await formRef.value?.validate();
  saving.value = true;
  try {
    if (editingIndexId.value === null) {
      ensureSuccess(await riskIndexService.createRiskIndex({ ...form }));
      ElMessage.success("指标新增成功");
    } else {
      const payload: RiskIndexUpdateDTO = {
        indexName: form.indexName,
        indexCode: form.indexCode,
        weightValue: form.weightValue,
        indexDesc: form.indexDesc,
      };
      ensureSuccess(await riskIndexService.updateRiskIndex(editingIndexId.value, payload));
      ElMessage.success("指标编辑成功");
    }
    dialogVisible.value = false;
    await loadIndexes();
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    saving.value = false;
  }
}

async function handleToggleStatus(row: RiskIndexVO) {
  const nextStatus = row.status === 1 ? 0 : 1;
  const actionText = nextStatus === 1 ? "启用" : "停用";
  try {
    await ElMessageBox.confirm(`确定要${actionText}指标“${row.indexName}”吗？`, `${actionText}指标`, {
      type: "warning",
    });
    ensureSuccess(await riskIndexService.updateRiskIndexStatus(row.id, { status: nextStatus }));
    ElMessage.success(
      nextStatus === 1
        ? `指标已启用。已有业务如果缺少该指标值，将自动进入“待补录”。`
        : `指标已停用。相关业务会自动重算状态，旧有效评估也会同步失效。`,
    );
    await loadIndexes();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(getErrorMessage(error));
    }
  }
}

function resetRuleForm() {
  editingRuleId.value = null;
  ruleForm.indexId = selectedIndexId.value ?? 0;
  ruleForm.scoreMin = null;
  ruleForm.scoreMax = null;
  ruleForm.scoreValue = null;
  ruleForm.warningLevel = "LOW";
  ruleFormRef.value?.clearValidate();
}

function openCreateRuleDialog() {
  if (selectedIndexId.value === null) {
    ElMessage.warning("请先选择左侧指标后再新增规则");
    return;
  }
  resetRuleForm();
  ruleForm.indexId = selectedIndexId.value;
  ruleDialogVisible.value = true;
}

function openEditRuleDialog(row: RiskRuleVO) {
  editingRuleId.value = row.id;
  ruleForm.indexId = row.indexId;
  ruleForm.scoreMin = Number(row.scoreMin);
  ruleForm.scoreMax = Number(row.scoreMax);
  ruleForm.scoreValue = Number(row.scoreValue);
  ruleForm.warningLevel = row.warningLevel;
  ruleDialogVisible.value = true;
}

async function submitRuleForm() {
  await ruleFormRef.value?.validate();
  if (selectedIndexId.value === null) {
    return;
  }

  ruleSaving.value = true;
  try {
    if (editingRuleId.value === null) {
      ensureSuccess(await riskIndexService.createRiskRule({ ...ruleForm, indexId: selectedIndexId.value }));
      ElMessage.success("规则新增成功");
    } else {
      const payload: RiskRuleUpdateDTO = {
        scoreMin: ruleForm.scoreMin,
        scoreMax: ruleForm.scoreMax,
        scoreValue: ruleForm.scoreValue,
        warningLevel: ruleForm.warningLevel,
      };
      ensureSuccess(await riskIndexService.updateRiskRule(editingRuleId.value, payload));
      ElMessage.success("规则编辑成功");
    }
    ruleDialogVisible.value = false;
    await loadRules(selectedIndexId.value);
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    ruleSaving.value = false;
  }
}

async function handleDeleteRule(row: RiskRuleVO) {
  if (selectedIndexId.value === null) {
    return;
  }

  try {
    await ElMessageBox.confirm(`确定要删除该条评分规则吗？`, "删除规则", {
      type: "warning",
    });
    ensureSuccess(await riskIndexService.deleteRiskRule(row.id));
    ElMessage.success("规则删除成功");
    await loadRules(selectedIndexId.value);
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(getErrorMessage(error));
    }
  }
}

onMounted(() => {
  void loadIndexes();
});
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">指标规则</h1>
        <p class="page-subtitle">管理员可以维护风险指标与评分规则，并影响评估总分与预警等级。</p>
      </div>
    </div>

    <el-alert
      type="info"
      :closable="false"
      :title="isMockMode
        ? '当前为 Mock 环境：指标与规则修改只影响本地 Mock 数据，可用于独立演示和回退验证。'
        : '当前为真实后端环境：指标与规则写操作会直接影响真实评估口径；规则区间不可重叠，且无规则指标不能启用。'
      "
       />

    <el-row :gutter="20">
      <el-col :xs="24" :xl="8">
        <el-card class="section-card" shadow="never">
          <template #header>本页怎么看</template>
          <div class="guide-list">
            <p>1. 先看左侧“风险指标”，确认有哪些指标参与评分。</p>
            <p>2. 再看右侧“评分规则”，确认每个指标值会映射成什么分值。</p>
            <p>3. 权重与规则共同决定风险评估页里的总分与预警等级。</p>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="16">
        <el-card class="section-card" shadow="never">
          <template #header>当前规则说明</template>
          <div class="summary-grid">
            <div class="summary-item">
              <span>启用指标数</span>
              <strong>{{ enabledIndexes.length }}</strong>
            </div>
            <div class="summary-item">
              <span>当前查看指标</span>
              <strong>{{ selectedIndex?.indexName ?? "未选择" }}</strong>
            </div>
            <div class="summary-item">
              <span>启用权重合计</span>
              <strong>{{ enabledWeightTotal.toFixed(2) }}%</strong>
            </div>
          </div>
          <el-alert
            class="summary-alert"
            :type="enabledWeightTotal > 100 ? 'error' : enabledWeightTotal === 100 ? 'success' : 'warning'"
            :closable="false"
            :title="enabledWeightTotal === 100
              ? '当前启用权重合计正好为 100%，适合继续进行风险录入与评估。'
              : enabledWeightTotal > 100
                ? '当前启用权重已经超过 100%，请先调整指标配置。'
                : '当前启用权重未满 100%。请继续调整指标配置后再进行完整业务验证。 '"
          />
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header-actions">
              <span>风险指标</span>
              <el-button type="primary" @click="openCreateDialog">新增指标</el-button>
            </div>
          </template>
          <el-table
            v-loading="loading"
            :data="indexes"
            highlight-current-row
            row-key="id"
            @current-change="(row: RiskIndexVO | undefined) => row && handleSelectIndex(row.id)"
          >
            <el-table-column prop="indexName" label="指标名称" min-width="150" />
            <el-table-column prop="indexCode" label="指标编码" min-width="160" />
            <el-table-column prop="weightValue" label="权重(%)" min-width="100" />
            <el-table-column label="状态" min-width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "启用" : "停用" }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="indexDesc" label="说明" min-width="220" show-overflow-tooltip />
            <el-table-column label="操作" min-width="180" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
                  <el-button link type="warning" @click="handleToggleStatus(row)">
                    {{ row.status === 1 ? "停用" : "启用" }}
                  </el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>
            <div class="card-header-actions">
              <span>评分规则</span>
              <el-button type="primary" :disabled="selectedIndexId === null" @click="openCreateRuleDialog">新增规则</el-button>
            </div>
          </template>
          <div v-if="selectedIndexId === null" class="empty-hint">请选择左侧指标查看规则。</div>
          <el-table v-else :data="rules">
            <template #empty>
              <el-empty description="当前指标还没有评分规则，可先新增第一条规则" />
            </template>
            <el-table-column prop="scoreMin" label="最小值" min-width="90" />
            <el-table-column prop="scoreMax" label="最大值" min-width="90" />
            <el-table-column prop="scoreValue" label="原始得分" min-width="100" />
            <el-table-column label="建议预警等级" min-width="130">
              <template #default="{ row }">
                <el-tag :type="riskLevelTagType(row.warningLevel)">{{ riskLevelLabel(row.warningLevel) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="140" fixed="right">
              <template #default="{ row }">
                <div class="table-actions">
                  <el-button link type="primary" @click="openEditRuleDialog(row)">编辑</el-button>
                  <el-button link type="danger" @click="handleDeleteRule(row)">删除</el-button>
                </div>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog
      v-model="dialogVisible"
      :title="editingIndexId === null ? '新增指标' : '编辑指标'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rulesConfig" label-width="96px">
        <el-form-item label="指标名称" prop="indexName">
          <el-input v-model="form.indexName" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="指标编码" prop="indexCode">
          <el-input v-model="form.indexCode" maxlength="50" placeholder="建议使用英文大写下划线" />
        </el-form-item>
        <el-form-item label="权重(%)" prop="weightValue">
          <el-input-number v-model="form.weightValue" :min="0.01" :max="100" :precision="2" class="full-width" />
        </el-form-item>
        <el-form-item v-if="editingIndexId === null" label="初始状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="指标说明" prop="indexDesc">
          <el-input v-model="form.indexDesc" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitIndexForm">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="ruleDialogVisible"
      :title="editingRuleId === null ? '新增规则' : '编辑规则'"
      width="520px"
      destroy-on-close
      @closed="resetRuleForm"
    >
      <el-form ref="ruleFormRef" :model="ruleForm" :rules="ruleRulesConfig" label-width="96px">
        <el-form-item label="最小值" prop="scoreMin">
          <el-input-number v-model="ruleForm.scoreMin" :precision="2" :step="0.01" class="full-width" />
        </el-form-item>
        <el-form-item label="最大值" prop="scoreMax">
          <el-input-number v-model="ruleForm.scoreMax" :precision="2" :step="0.01" class="full-width" />
        </el-form-item>
        <el-form-item label="原始得分" prop="scoreValue">
          <el-input-number v-model="ruleForm.scoreValue" :min="0" :precision="2" :step="0.01" class="full-width" />
        </el-form-item>
        <el-form-item label="预警等级" prop="warningLevel">
          <el-select v-model="ruleForm.warningLevel" class="full-width">
            <el-option label="低风险" value="LOW" />
            <el-option label="中风险" value="MEDIUM" />
            <el-option label="高风险" value="HIGH" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="ruleDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="ruleSaving" @click="submitRuleForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.card-header-actions,
.table-actions {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.guide-list,
.summary-grid {
  display: grid;
  gap: 12px;
}

.guide-list p {
  margin: 0;
  color: #58738f;
}

.summary-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.summary-item {
  display: grid;
  gap: 6px;
  padding: 16px;
  border-radius: 16px;
  background: rgba(247, 250, 252, 0.96);
}

.summary-item span {
  color: #58738f;
  font-size: 13px;
}

.summary-alert {
  margin-top: 16px;
}

.full-width {
  width: 100%;
}

@media (max-width: 960px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
