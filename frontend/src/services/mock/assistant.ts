import type { AssistantService } from "@/services/contracts";
import { failure, withDelay } from "@/services/mock/helpers";

const disabledMessage = "AI assistant is disabled in V1";

export const mockAssistantService: AssistantService = {
  async query() {
    return withDelay(failure(50100, disabledMessage, undefined));
  },
  async action() {
    return withDelay(failure(50100, disabledMessage, undefined));
  },
};
