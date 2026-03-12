<script setup lang="ts">
import { ElMessage } from "element-plus";
import { onMounted, ref } from "vue";
import { isRiskIndexHttpMode, riskIndexService } from "@/services";
import type { RiskIndexVO, RiskRuleVO } from "@/types/risk";
import { riskLevelLabel, riskLevelTagType } from "@/utils/format";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

const loading = ref(false);
const indexes = ref<RiskIndexVO[]>([]);
const selectedIndexId = ref<number | null>(null);
const rules = ref<RiskRuleVO[]>([]);

async function loadIndexes() {
  loading.value = true;
  try {
    indexes.value = ensureSuccess(await riskIndexService.listRiskIndexes());
    const firstIndex = indexes.value[0];
    if (!selectedIndexId.value && firstIndex) {
      selectedIndexId.value = firstIndex.id;
    }
    if (selectedIndexId.value !== null) {
      rules.value = ensureSuccess(await riskIndexService.listRiskRules(selectedIndexId.value));
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
    rules.value = ensureSuccess(await riskIndexService.listRiskRules(indexId));
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
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
        <p class="page-subtitle">管理员在第一轮只读查看预置指标与评分规则，这些规则会直接影响风控闭环。</p>
      </div>
      <el-tag :type="isRiskIndexHttpMode ? 'success' : 'info'">{{ isRiskIndexHttpMode ? "真实接口" : "只读模式" }}</el-tag>
    </div>

    <el-alert
      type="info"
      :closable="false"
      :title="isRiskIndexHttpMode
        ? '当前页面已切到真实后端读取：指标、权重和评分区间都来自 backend。'
        : '当前页面用于说明评估依据：风控主线里的总分、风险等级和预警触发，都来自这里的指标权重与评分区间。'"
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
              <strong>{{ indexes.length }}</strong>
            </div>
            <div class="summary-item">
              <span>当前查看指标</span>
              <strong>{{ indexes.find((item) => item.id === selectedIndexId)?.indexName ?? "未选择" }}</strong>
            </div>
            <div class="summary-item">
              <span>规则用途</span>
              <strong>决定原始得分与建议预警等级</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>风险指标</template>
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
            <el-table-column prop="indexDesc" label="说明" min-width="220" show-overflow-tooltip />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :xl="12">
        <el-card class="section-card" shadow="never">
          <template #header>评分规则</template>
          <div v-if="selectedIndexId === null" class="empty-hint">请选择左侧指标查看规则。</div>
          <el-table v-else :data="rules">
            <el-table-column prop="scoreMin" label="最小值" min-width="90" />
            <el-table-column prop="scoreMax" label="最大值" min-width="90" />
            <el-table-column prop="scoreValue" label="原始得分" min-width="100" />
            <el-table-column label="建议预警等级" min-width="130">
              <template #default="{ row }">
                <el-tag :type="riskLevelTagType(row.warningLevel)">{{ riskLevelLabel(row.warningLevel) }}</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
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

@media (max-width: 960px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
