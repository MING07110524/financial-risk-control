import type { AuthService } from "@/services/contracts";
import type { LoginRequest, LoginUser } from "@/types/auth";
import { getCurrentMockUser } from "@/mock/db";
import { mockUsers } from "@/mock/seed";
import { failure, normalizeKeyword, success, withDelay } from "@/services/mock/helpers";

export const mockAuthService: AuthService = {
  async login(payload: LoginRequest) {
    const username = normalizeKeyword(payload.username);
    const password = payload.password.trim();
    const user = mockUsers.find((item) => item.username.toLowerCase() === username);

    if (!user || !password) {
      return withDelay(failure(41001, "用户名不存在或密码为空，请使用演示账号登录", undefined as never));
    }

    if (user.status === 0) {
      return withDelay(failure(41002, "当前账号已停用", undefined as never));
    }

    const loginUser: LoginUser = {
      userId: user.id,
      username: user.username,
      realName: user.realName,
      roleCode: user.roleCode,
      roleName: user.roleName,
      token: `mock-token-${user.roleCode}-${Date.now()}`,
    };

    return withDelay(success(loginUser));
  },
  async logout() {
    return withDelay(success(undefined));
  },
  async getCurrentUser() {
    const currentUser = getCurrentMockUser();
    if (!currentUser) {
      return withDelay(failure(40100, "当前未登录", undefined as never));
    }
    return withDelay(success(currentUser));
  },
};
