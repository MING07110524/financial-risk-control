import type { SystemService } from "@/services/contracts";
import { resetMockDb } from "@/mock/db";
import { success, withDelay } from "@/services/mock/helpers";

export const mockSystemService: SystemService = {
  async resetDemoData() {
    resetMockDb();
    return withDelay(success(undefined, "演示数据已重置"));
  },
};
