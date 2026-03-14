import client from "@/api/client";
import { compactParams } from "@/api/params";
import type { LogQuery } from "@/services/contracts";
import type { PageResult, Result } from "@/types/common";
import type { LogVO } from "@/types/system";

export async function pageLogsApi(
  query: LogQuery,
  pageNum: number,
  pageSize: number
): Promise<Result<PageResult<LogVO>>> {
  const { data } = await client.get<Result<PageResult<LogVO>>>("/logs", {
    params: compactParams({ ...query, pageNum, pageSize }),
  });
  return data;
}
