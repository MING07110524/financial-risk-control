<script setup lang="ts">
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from "vue";
import * as echarts from "echarts";
import type { ECharts, EChartsOption } from "echarts";

const props = withDefaults(defineProps<{
  option: EChartsOption;
  height?: string;
  autoresize?: boolean;
}>(), {
  height: "260px",
  autoresize: true,
});

const chartRef = ref<HTMLDivElement | null>(null);
let chartInstance: ECharts | null = null;
let resizeObserver: ResizeObserver | null = null;

function renderChart() {
  if (!chartRef.value) {
    return;
  }

  if (!chartInstance) {
    chartInstance = echarts.init(chartRef.value);
  }

  chartInstance.setOption(props.option, true);
}

function resizeChart() {
  chartInstance?.resize();
}

onMounted(async () => {
  await nextTick();
  renderChart();

  // Resize with the container instead of only listening to the window so the
  // chart stays correct inside responsive cards. / 跟随容器尺寸变化而不是只监听
  // window，可以让图表在响应式卡片内也保持正确布局。
  if (props.autoresize && chartRef.value && typeof ResizeObserver !== "undefined") {
    resizeObserver = new ResizeObserver(() => resizeChart());
    resizeObserver.observe(chartRef.value);
  }

  window.addEventListener("resize", resizeChart);
});

watch(
  () => props.option,
  async () => {
    await nextTick();
    renderChart();
  },
  { deep: true },
);

onBeforeUnmount(() => {
  resizeObserver?.disconnect();
  window.removeEventListener("resize", resizeChart);
  chartInstance?.dispose();
  chartInstance = null;
});
</script>

<template>
  <div ref="chartRef" class="chart-shell" :style="{ height }" />
</template>

<style scoped>
.chart-shell {
  width: 100%;
}
</style>
