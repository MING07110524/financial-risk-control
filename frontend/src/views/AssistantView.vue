<script setup lang="ts">
import { ElMessage } from "element-plus";
import { reactive, ref } from "vue";
import { assistantService } from "@/services";
import { getErrorMessage } from "@/utils/result";

const loading = ref(false);
const form = reactive({
  prompt: "",
});
const latestMessage = ref("当前版本未启用 AI 助手，只保留占位接口与页面入口。");

async function submitQuery() {
  loading.value = true;
  try {
    const result = await assistantService.query({ prompt: form.prompt.trim() });
    latestMessage.value = result.message;
    ElMessage.warning(result.message);
  } catch (error) {
    latestMessage.value = getErrorMessage(error);
    ElMessage.error(latestMessage.value);
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="page-shell assistant-page">
    <div class="page-header">
      <div>
        <h1 class="page-title">AI 助手</h1>
        <p class="page-subtitle">当前页面只用于演示占位入口与禁用提示，不提供真实问答或自动执行能力。</p>
      </div>
    </div>

    <el-card class="section-card" shadow="never">
      <template #header>
        <div class="assistant-header">
          <span>V1 状态说明</span>
          <el-tag type="warning" effect="plain">未启用</el-tag>
        </div>
      </template>
      <p class="assistant-text">后端 `/api/assistant/query` 与 `/api/assistant/action` 已预留，当前统一返回禁用提示，便于演示、联调和后续扩展。</p>
      <el-input
        v-model="form.prompt"
        type="textarea"
        :rows="4"
        placeholder="例如：请总结当前待处理预警的关注点"
      />
      <div class="assistant-actions">
        <el-button type="primary" :loading="loading" @click="submitQuery">发送占位查询</el-button>
      </div>
      <el-alert :title="latestMessage" type="info" :closable="false" show-icon />
    </el-card>
  </div>
</template>

<style scoped>
.assistant-page {
  display: grid;
  gap: 20px;
}

.assistant-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.assistant-text {
  margin: 0 0 16px;
  color: #58738f;
  line-height: 1.7;
}

.assistant-actions {
  display: flex;
  justify-content: flex-end;
  margin: 16px 0;
}
</style>
