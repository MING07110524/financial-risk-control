<script setup lang="ts">
import { computed } from "vue";
import type { RoleCode } from "@/types/auth";

const visible = defineModel<boolean>({ required: true });

const props = defineProps<{
  roleCode: RoleCode | "";
  showResetTip: boolean;
}>();

const commonSteps = computed(() => [
  "登录页直接选择演示账号，优先从 risk-demo 开始体验完整闭环。",
  "每次处理完预警后，记得回到首页或统计页点“刷新数据”，观察数字和图表回流。",
  ...(props.showResetTip ? ["如果中途想回到初始状态，可在右上角点击“重置演示数据”。"] : []),
]);

// Build role-specific guidance so the same drawer can guide both product demos
// and technical walkthroughs. / 用角色维度组织导览步骤，让同一个抽屉既能用于
// 答辩演示，也能用于日常自测，不需要额外再解释“不同角色该看什么”。
const roleGuide = computed(() => {
  if (props.roleCode === "ADMIN") {
    return {
      title: "管理员演示路径",
      purpose: "说明系统依据来自哪些指标与规则，并展示后台能力未来会落在哪些入口。",
      steps: [
        "从仪表盘查看首页总览，确认管理员不会误入业务处理链。",
        "进入“指标规则”，查看 4 个预置指标、权重分布和评分区间。",
        "进入“用户管理”与“操作日志”占位页，确认这些入口是后续真实后台能力预留位置。",
      ],
    };
  }

  if (props.roleCode === "MANAGER") {
    return {
      title: "管理人员演示路径",
      purpose: "说明管理视角重点关注的是风险态势和处置进度，而不是直接改业务数据。",
      steps: [
        "从仪表盘看角色关注点、风险等级分布和预警趋势。",
        "进入“预警管理”，打开一条预警详情，查看时间线但不能提交处理。",
        "进入“统计分析”，通过筛选验证图表与洞察文案会同步变化。",
      ],
    };
  }

  return {
    title: "风控人员演示路径",
    purpose: "这是当前最完整的最小闭环：从录入业务到处理预警，再回看统计结果。",
    steps: [
      "进入“风险数据”，新增或编辑一条业务并填写全部指标值。",
      "从列表点击“去评估 / 重新评估”，在评估页执行一次评估并查看评分详情。",
      "进入“预警管理”，打开新生成的预警并提交处理意见与处理结果。",
      "回到“仪表盘”或“统计分析”，刷新页面查看预警和统计数据是否回流。",
    ],
  };
});
</script>

<template>
  <el-drawer v-model="visible" title="演示路径导览" size="420px">
    <div class="guide-shell">
      <el-card class="guide-card" shadow="never">
        <template #header>
          <strong>{{ roleGuide.title }}</strong>
        </template>
        <p class="guide-card__text">{{ roleGuide.purpose }}</p>
      </el-card>

      <el-card class="guide-card" shadow="never">
        <template #header>
          <strong>当前角色推荐步骤</strong>
        </template>
        <ol class="guide-list">
          <li v-for="step in roleGuide.steps" :key="step">{{ step }}</li>
        </ol>
      </el-card>

      <el-card class="guide-card" shadow="never">
        <template #header>
          <strong>通用体验提醒</strong>
        </template>
        <ul class="guide-list guide-list--unordered">
          <li v-for="tip in commonSteps" :key="tip">{{ tip }}</li>
        </ul>
      </el-card>
    </div>
  </el-drawer>
</template>

<style scoped>
.guide-shell {
  display: grid;
  gap: 16px;
}

.guide-card {
  border-radius: 18px;
}

.guide-card__text {
  margin: 0;
  color: #58738f;
}

.guide-list {
  margin: 0;
  padding-left: 20px;
  display: grid;
  gap: 10px;
  color: #17324d;
}

.guide-list--unordered {
  list-style: disc;
}
</style>
