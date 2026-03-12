<script setup lang="ts">
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { isAuthMockMode, isCoreWorkflowMockMode, isMockMode } from "@/services";
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
  { label: "管理员", username: "admin-demo", description: "查看指标规则与系统入口" },
  { label: "风控人员", username: "risk-demo", description: "完整演示录入、评估、预警处理闭环" },
  { label: "管理人员", username: "manager-demo", description: "只读查看预警与统计结果" },
];

async function submit() {
  loading.value = true;
  try {
    await userStore.login(form);
    ElMessage.success("登录成功");
    await router.push("/dashboard");
  } catch (error) {
    const message = getErrorMessage(error);
    if (!isAuthMockMode && /Network Error|timeout|fetch/i.test(message)) {
      ElMessage.error("真实认证后端暂未启动，请先运行 backend，再重新登录。");
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
        当前系统已经支持真实后端认证，并会按阶段把业务链逐步切到真实接口。
        现在这套演示账号可直接用于体验完整风控闭环。
      </p>
      <div class="login-copy__panel">
        <strong>演示说明</strong>
        <span v-if="isCoreWorkflowMockMode">支持三种角色登录。认证已走真实后端，部分业务链路仍保留 mock 过渡。</span>
        <span v-else>支持三种角色登录。认证与核心风控业务链都已切到真实后端。</span>
      </div>
      <div class="login-copy__panel login-copy__panel--guide">
        <strong>推荐体验顺序</strong>
        <span>risk-demo：风险数据 -> 风险评估 -> 预警处理 -> 统计回流</span>
        <span>manager-demo：预警详情 -> 统计分析 -> 首页洞察</span>
        <span>admin-demo：指标规则 -> 用户管理占位 -> 日志占位</span>
      </div>
      <div v-if="isMockMode" class="demo-accounts">
        <div class="demo-accounts__header">
          <strong>演示账号快捷入口</strong>
          <span>
            {{
              isAuthMockMode
                ? "点击即可直接建立 mock 会话"
                : isCoreWorkflowMockMode
                  ? "点击后会调用真实后端登录，并进入当前阶段已接通的业务页面"
                  : "点击后会调用真实后端登录，并进入完整真实业务链"
            }}
          </span>
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
          <p class="login-card__subtitle">可以手动输入，也可以直接使用左侧演示账号</p>
        </div>
      </template>

      <el-form label-position="top" @submit.prevent="submit">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名，例如 risk-demo" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="演示模式下输入任意非空密码即可" />
        </el-form-item>
        <el-button type="primary" class="login-card__action" :loading="loading" @click="submit">
          登录
        </el-button>
        <p v-if="isMockMode && isAuthMockMode" class="login-card__hint">当前为全 Mock 模式，账号密码仅用于演示交互，不对应真实后端账号。</p>
        <p v-else-if="isCoreWorkflowMockMode" class="login-card__hint">当前为“认证真实 / 部分业务 Mock”模式。三类演示账号统一使用密码：demo。</p>
        <p v-else class="login-card__hint">当前为“核心链真实后端”模式。三类演示账号统一使用密码：demo。</p>
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
