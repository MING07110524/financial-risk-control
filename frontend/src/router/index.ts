import { createRouter, createWebHistory, type RouteRecordRaw } from "vue-router";
import AppLayout from "@/layout/AppLayout.vue";
import { useUserStore } from "@/stores/user";

const routes: RouteRecordRaw[] = [
  {
    path: "/login",
    name: "login",
    component: () => import("@/views/LoginView.vue"),
    meta: {
      public: true,
      title: "登录",
    },
  },
  {
    path: "/",
    component: AppLayout,
    redirect: "/dashboard",
    children: [
      {
        path: "dashboard",
        name: "dashboard",
        component: () => import("@/views/DashboardView.vue"),
        meta: {
          title: "仪表盘",
          roles: ["ADMIN", "RISK_USER", "MANAGER"],
          showInMenu: true,
        },
      },
      {
        path: "system/users",
        name: "system-users",
        component: () => import("@/views/system/UsersView.vue"),
        meta: {
          title: "用户管理",
          roles: ["ADMIN"],
          showInMenu: true,
        },
      },
      {
        path: "system/logs",
        name: "system-logs",
        component: () => import("@/views/system/LogsView.vue"),
        meta: {
          title: "操作日志",
          roles: ["ADMIN"],
          showInMenu: true,
        },
      },
      {
        path: "risk/data",
        name: "risk-data",
        component: () => import("@/views/risk/RiskDataView.vue"),
        meta: {
          title: "风险数据",
          roles: ["RISK_USER"],
          showInMenu: true,
        },
      },
      {
        path: "risk/indexes",
        name: "risk-indexes",
        component: () => import("@/views/risk/RiskIndexesView.vue"),
        meta: {
          title: "指标规则",
          roles: ["ADMIN"],
          showInMenu: true,
        },
      },
      {
        path: "risk/assessments",
        name: "risk-assessments",
        component: () => import("@/views/risk/AssessmentsView.vue"),
        meta: {
          title: "风险评估",
          roles: ["RISK_USER"],
          showInMenu: true,
        },
      },
      {
        path: "risk/warnings",
        name: "risk-warnings",
        component: () => import("@/views/risk/WarningsView.vue"),
        meta: {
          title: "预警管理",
          roles: ["RISK_USER", "MANAGER"],
          showInMenu: true,
        },
      },
      {
        path: "analysis/statistics",
        name: "analysis-statistics",
        component: () => import("@/views/analysis/StatisticsView.vue"),
        meta: {
          title: "统计分析",
          roles: ["RISK_USER", "MANAGER"],
          showInMenu: true,
        },
      },
    ],
  },
  {
    path: "/:pathMatch(.*)*",
    name: "not-found",
    component: () => import("@/views/NotFoundView.vue"),
    meta: {
      public: true,
      title: "页面不存在",
    },
  },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

router.beforeEach(async (to) => {
  const userStore = useUserStore();
  const isPublic = Boolean(to.meta.public);

  if (isPublic && userStore.isAuthenticated && to.path === "/login") {
    return "/dashboard";
  }

  if (!isPublic && !userStore.isAuthenticated) {
    return "/login";
  }

  if (!isPublic && userStore.isAuthenticated && !userStore.currentUser) {
    try {
      await userStore.fetchCurrentUser();
    } catch {
      userStore.clearSession();
      return "/login";
    }
  }

  const allowedRoles = to.meta.roles as string[] | undefined;
  if (allowedRoles && userStore.roleCode && !allowedRoles.includes(userStore.roleCode)) {
    return "/dashboard";
  }

  return true;
});

export default router;
