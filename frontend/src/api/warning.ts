import client from "@/api/client";
import { compactParams } from "@/api/params";
import type { PageResult, Result } from "@/types/common";
import type {
  WarningDetailVO,
  WarningHandleDTO,
  WarningHandleRecordVO,
  WarningQuery,
  WarningVO,
} from "@/types/risk";

export async function pageWarningsApi(query: WarningQuery): Promise<Result<PageResult<WarningVO>>> {
  const { data } = await client.get<Result<PageResult<WarningVO>>>("/warnings", {
    params: compactParams(query),
  });
  return data;
}

export async function getWarningDetailApi(id: number): Promise<Result<WarningDetailVO>> {
  const { data } = await client.get<Result<WarningDetailVO>>(`/warnings/${id}`);
  return data;
}

export async function listWarningRecordsApi(id: number): Promise<Result<WarningHandleRecordVO[]>> {
  const { data } = await client.get<Result<WarningHandleRecordVO[]>>(`/warnings/${id}/records`);
  return data;
}

export async function handleWarningApi(id: number, payload: WarningHandleDTO): Promise<Result<void>> {
  const { data } = await client.post<Result<void>>(`/warnings/${id}/handle`, payload);
  return data;
}
