import { defineStore } from "pinia";
import { computed, ref } from "vue";
import { authService } from "@/services";
import type { CurrentUser, LoginRequest, RoleCode } from "@/types/auth";
import { clearToken, clearUser, getToken, getUser, setToken, setUser } from "@/utils/storage";
import { ensureSuccess } from "@/utils/result";

function readStoredUser(): CurrentUser | null {
  const raw = getUser();
  if (!raw) {
    return null;
  }

  try {
    return JSON.parse(raw) as CurrentUser;
  } catch {
    clearUser();
    return null;
  }
}

export const useUserStore = defineStore("user", () => {
  const token = ref<string>(getToken());
  const currentUser = ref<CurrentUser | null>(readStoredUser());

  const isAuthenticated = computed(() => Boolean(token.value));
  const roleCode = computed<RoleCode | "">(() => currentUser.value?.roleCode as RoleCode ?? "");

  async function login(payload: LoginRequest) {
    const result = await authService.login(payload);
    const loginUser = ensureSuccess(result);
    setToken(loginUser.token);
    token.value = loginUser.token;

    currentUser.value = {
      userId: loginUser.userId,
      username: loginUser.username,
      realName: loginUser.realName,
      roleCode: loginUser.roleCode,
      roleName: loginUser.roleName,
    };
    setUser(JSON.stringify(currentUser.value));
  }

  async function fetchCurrentUser() {
    if (!token.value) {
      return null;
    }

    const result = await authService.getCurrentUser();
    const user = ensureSuccess(result);
    currentUser.value = user;
    setUser(JSON.stringify(user));
    return user;
  }

  async function logout() {
    if (token.value) {
      await authService.logout().catch(() => undefined);
    }
    clearSession();
  }

  function clearSession() {
    token.value = "";
    currentUser.value = null;
    clearToken();
    clearUser();
  }

  return {
    token,
    currentUser,
    isAuthenticated,
    roleCode,
    login,
    fetchCurrentUser,
    logout,
    clearSession,
  };
});
