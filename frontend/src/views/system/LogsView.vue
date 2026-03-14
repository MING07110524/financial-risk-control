<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import { logService } from "@/services";
import type { LogVO } from "@/types/system";
import { ensureSuccess, getErrorMessage } from "@/utils/result";
import { ElMessage } from "element-plus";

const loading = ref(false);
const total = ref(0);
const records = ref<LogVO[]>([]);

const query = reactive({
  moduleName: "",
  operationType: "",
  operator: "",
  startTime: "",
  endTime: "",
  pageNum: 1,
  pageSize: 10,
});

const moduleOptions = [
  { label: "系统", value: "系统" },
  { label: "风险数据", value: "风险数据" },
  { label: "风险评估", value: "风险评估" },
  { label: "指标规则", value: "指标规则" },
  { label: "预警", value: "预警" },
  { label: "用户", value: "用户" },
];

const operationOptions = [
  { label: "登录", value: "登录" },
  { label: "登录失败", value: "登录失败" },
  { label: "退出", value: "退出" },
  { label: "新增", value: "新增" },
  { label: "编辑", value: "编辑" },
  { label: "删除", value: "删除" },
  { label: "启用", value: "启用" },
  { label: "停用", value: "停用" },
  { label: "执行", value: "执行" },
  { label: "处理", value: "处理" },
];

async function loadLogs() {
  loading.value = true;
  try {
    const result = ensureSuccess(
      await logService.pageLogs(
        {
          moduleName: query.moduleName || undefined,
          operationType: query.operationType || undefined,
          operator: query.operator || undefined,
          startTime: query.startTime || undefined,
          endTime: query.endTime || undefined,
        },
        query.pageNum,
        query.pageSize
      )
    );
    total.value = result.total;
    records.value = result.records;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function handleSearch() {
  query.pageNum = 1;
  loadLogs();
}

function handleReset() {
  query.moduleName = "";
  query.operationType = "";
  query.operator = "";
  query.startTime = "";
  query.endTime = "";
  query.pageNum = 1;
  loadLogs();
}

function handlePageChange(page: number) {
  query.pageNum = page;
  loadLogs();
}

function handlePageSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  loadLogs();
}

onMounted(() => {
  loadLogs();
});

function hasFilters() {
  return Boolean(query.moduleName || query.operationType || query.operator || query.startTime || query.endTime);
}
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">操作日志</h1>
        <p class="page-subtitle">管理员可以查看系统操作日志记录。</p>
      </div>
    </div>

    <el-card class="section-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="模块">
          <el-select v-model="query.moduleName" placeholder="请选择模块" clearable>
            <el-option v-for="item in moduleOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="query.operationType" placeholder="请选择操作类型" clearable>
            <el-option v-for="item in operationOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作人">
          <el-input v-model="query.operator" placeholder="请输入操作人" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker v-model="query.startTime" type="datetime" placeholder="选择开始时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker v-model="query.endTime" type="datetime" placeholder="选择结束时间" value-format="YYYY-MM-DD HH:mm:ss" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="records" style="width: 100%">
        <template #empty>
          <el-empty :description="hasFilters() ? '当前筛选条件下没有匹配的日志记录' : '当前没有可展示的日志记录'">
            <el-button v-if="hasFilters()" @click="handleReset">清空筛选</el-button>
          </el-empty>
        </template>
        <el-table-column prop="operationTime" label="操作时间" min-width="170" />
        <el-table-column prop="moduleName" label="模块" min-width="120" />
        <el-table-column prop="operationType" label="操作类型" min-width="100" />
        <el-table-column prop="operator" label="操作人" min-width="120" />
        <el-table-column prop="operationDesc" label="操作描述" min-width="300" />
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.search-form {
  margin-bottom: 16px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}
</style>
