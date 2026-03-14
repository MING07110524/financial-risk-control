import { fileURLToPath, URL } from "node:url";
import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes("node_modules")) {
            return;
          }

          if (id.includes("echarts")) {
            return "vendor-echarts";
          }

          if (id.includes("element-plus") || id.includes("@element-plus")) {
            return "vendor-element-plus";
          }

          if (id.includes("vue-router") || id.includes("pinia") || id.includes("/vue/")) {
            return "vendor-vue";
          }
        },
      },
    },
  },
});
