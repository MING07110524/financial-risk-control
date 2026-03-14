import type { LogService, SystemService, UserService } from "@/services/contracts";
import { getMockDb, resetMockDb } from "@/mock/db";
import { success, withDelay } from "@/services/mock/helpers";

export const mockUserService: UserService = {
  async pageUsers() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持用户管理", data: undefined as never });
  },
  async getUserById() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持用户管理", data: undefined as never });
  },
  async createUser() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持用户管理", data: undefined as never });
  },
  async updateUser() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持用户管理", data: undefined as never });
  },
  async updateUserStatus() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持用户管理", data: undefined as never });
  },
  async deleteUser() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持用户管理", data: undefined as never });
  },
  async listRoles() {
    return withDelay({ code: 50100, message: "Mock 模式暂未支持用户管理", data: undefined as never });
  },
};

export const mockSystemService: SystemService = {
  async resetDemoData() {
    resetMockDb();
    return withDelay(success(undefined, "本地 Mock 数据已重置"));
  },
};

export const mockLogService: LogService = {
  async pageLogs(query, pageNum, pageSize) {
    const db = getMockDb();
    const normalizedModuleName = query.moduleName?.trim().toLowerCase() ?? "";
    const normalizedOperationType = query.operationType?.trim().toLowerCase() ?? "";
    const normalizedOperator = query.operator?.trim().toLowerCase() ?? "";
    const normalizedStartTime = query.startTime?.trim() ?? "";
    const normalizedEndTime = query.endTime?.trim() ?? "";

    const filtered = db.logs.filter((item) => {
      if (normalizedModuleName && !item.moduleName.toLowerCase().includes(normalizedModuleName)) {
        return false;
      }
      if (normalizedOperationType && item.operationType.toLowerCase() !== normalizedOperationType) {
        return false;
      }
      if (normalizedOperator && !(item.operator ?? "").toLowerCase().includes(normalizedOperator)) {
        return false;
      }
      if (normalizedStartTime && item.operationTime < normalizedStartTime) {
        return false;
      }
      if (normalizedEndTime && item.operationTime > normalizedEndTime) {
        return false;
      }
      return true;
    });

    const safePageNum = Math.max(pageNum, 1);
    const safePageSize = Math.max(pageSize, 1);
    const start = (safePageNum - 1) * safePageSize;
    const records = filtered.slice(start, start + safePageSize);

    return withDelay(success({ total: filtered.length, records }));
  },
};
