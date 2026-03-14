import client from "@/api/client";
import type { Result } from "@/types/common";

export async function queryAssistantApi(payload: Record<string, unknown>): Promise<Result<void>> {
  const { data } = await client.post<Result<void>>("/assistant/query", payload);
  return data;
}

export async function actionAssistantApi(payload: Record<string, unknown>): Promise<Result<void>> {
  const { data } = await client.post<Result<void>>("/assistant/action", payload);
  return data;
}
