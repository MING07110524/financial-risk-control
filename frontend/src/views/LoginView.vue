<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { isMockMode } from "@/services";
import { useUserStore } from "@/stores/user";
import { getErrorMessage } from "@/utils/result";

const router = useRouter();
const userStore = useUserStore();

const form = reactive({
  username: "",
  password: "",
});

const loading = ref(false);
const demoAccounts = [
  { label: "管理员", username: "admin-demo", description: "维护指标规则、用户管理与操作日志" },
  { label: "风控人员", username: "risk-demo", description: "完整演示录入、评估、预警处理与统计回流" },
  { label: "管理人员", username: "manager-demo", description: "聚焦预警查看与统计分析" },
];

async function submit() {
  loading.value = true;
  try {
    await userStore.login(form);
    ElMessage.success("登录成功");
    await router.push("/dashboard");
  } catch (error) {
    const message = getErrorMessage(error);
    if (!isMockMode && /Network Error|timeout|fetch/i.test(message)) {
      ElMessage.error("真实后端暂未启动，请先运行 backend，再重新登录。");
    } else {
      ElMessage.error(message);
    }
  } finally {
    loading.value = false;
  }
}

async function submitDemoLogin(username: string) {
  form.username = username;
  form.password = "demo";
  await submit();
}
</script>

<template>
  <div class="login-shell">
    <div class="login-copy">
      <p class="login-copy__eyebrow">最小闭环原型</p>
      <h1 class="login-copy__title">金融风控管理系统</h1>
      <p class="login-copy__text">
        默认环境已切到真实后端联调，三类演示账号可以直接覆盖风控处理、管理查看与管理员配置审计三条路径。
        如需本地纯演示回退，请使用独立的 mock 环境启动前端。
      </p>
      <div class="login-copy__panel">
        <strong>环境说明</strong>
        <span v-if="isMockMode">当前为独立 Mock 环境，账号会创建本地演示会话，页面数据也全部来自本地 Mock。</span>
        <span v-else>当前为默认真实后端环境，三类演示账号都会走真实登录态与真实业务接口。</span>
      </div>
      <div class="login-copy__panel login-copy__panel--guide">
        <strong>推荐体验顺序</strong>
        <span>risk-demo：风险数据 -> 风险评估 -> 预警处理 -> 统计回流</span>
        <span>manager-demo：预警查看 -> 统计分析</span>
        <span>admin-demo：指标规则 -> 用户管理 -> 操作日志</span>
      </div>
      <div class="demo-accounts">
        <div class="demo-accounts__header">
          <strong>演示账号快捷入口</strong>
          <span>{{ isMockMode ? "点击即可建立本地 Mock 会话并进入本地数据链路" : "点击后会调用真实后端登录，并进入对应真实业务链" }}</span>
        </div>
        <button
          v-for="account in demoAccounts"
          :key="account.username"
          class="demo-account"
          type="button"
          @click="submitDemoLogin(account.username)"
        >
          <span class="demo-account__title">{{ account.label }}</span>
          <span class="demo-account__meta">{{ account.username }}</span>
          <span class="demo-account__desc">{{ account.description }}</span>
        </button>
      </div>
    </div>

    <el-card class="login-card" shadow="never">
      <template #header>
        <div>
          <h2 class="login-card__title">登录系统</h2>
          <p class="login-card__subtitle">可手动输入，也可直接使用左侧演示账号快速进入对应路径</p>
        </div>
      </template>

      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名，例如 risk-demo" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="三类演示账号统一使用密码 demo" />
        </el-form-item>
        <el-button type="primary" class="login-card__action" :loading="loading" @click="submit">
          登录
        </el-button>
        <p v-if="isMockMode" class="login-card__hint">当前为独立 Mock 环境，账号密码仅用于本地演示交互，不对应真实后端账号。</p>
        <p v-else class="login-card__hint">当前为默认真实后端环境。三类演示账号统一使用密码：demo。</p>
      </el-form>
    </el-card>
  </div>
</template>

<style scoped>
.login-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 1.2fr 420px;
  gap: 32px;
  padding: 40px;
  align-items: center;
}

.login-copy {
  padding: 36px;
}

.login-copy__eyebrow {
  margin: 0;
  color: #8b5e00;
  font-size: 12px;
  letter-spacing: 0.16em;
  text-transform: uppercase;
}

.login-copy__title {
  margin: 16px 0 12px;
  font-size: 52px;
  line-height: 1.05;
  color: #0f2742;
}

.login-copy__text {
  max-width: 680px;
  color: #47627d;
  font-size: 17px;
}

.login-copy__panel {
  display: grid;
  gap: 8px;
  margin-top: 28px;
  padding: 20px;
  max-width: 560px;
  border-radius: 22px;
  background: rgba(255, 255, 255, 0.86);
  border: 1px solid rgba(15, 39, 66, 0.08);
}

.login-copy__panel--guide span {
  font-size: 14px;
}

.demo-accounts {
  display: grid;
  gap: 12px;
  margin-top: 28px;
}

.demo-accounts__header {
  display: grid;
  gap: 4px;
  color: #47627d;
}

.demo-account {
  display: grid;
  gap: 4px;
  padding: 16px 18px;
  border: 1px solid rgba(15, 39, 66, 0.1);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.8);
  text-align: left;
  cursor: pointer;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.demo-account:hover {
  transform: translateY(-1px);
  box-shadow: 0 16px 28px rgba(15, 39, 66, 0.08);
}

.demo-account__title {
  font-size: 16px;
  font-weight: 700;
  color: #0f2742;
}

.demo-account__meta,
.demo-account__desc {
  color: #58738f;
  font-size: 13px;
}

.login-card {
  border-radius: 24px;
  border: 1px solid rgba(15, 39, 66, 0.08);
  background: rgba(255, 255, 255, 0.94);
}

.login-card__title {
  margin: 0;
  color: #0f2742;
}

.login-card__subtitle {
  margin: 8px 0 0;
  color: #58738f;
}

.login-card__action {
  width: 100%;
}

.login-card__hint {
  margin: 14px 0 0;
  font-size: 13px;
  color: #6a8298;
}

@media (max-width: 980px) {
  .login-shell {
    grid-template-columns: 1fr;
    padding: 24px;
  }

  .login-copy {
    padding: 0;
  }

  .login-copy__title {
    font-size: 36px;
  }
}
</style>
