import client from "@/api/client";
import { compactParams } from "@/api/params";
import type { PageResult, Result } from "@/types/common";
import type { UserVO, RoleVO } from "@/types/system";

export interface UserQuery {
  username?: string;
  realName?: string;
  roleCode?: string;
  status?: number | "";
}

export async function pageUsersApi(
  query: UserQuery,
  pageNum: number,
  pageSize: number
): Promise<Result<PageResult<UserVO>>> {
  const { data } = await client.get<Result<PageResult<UserVO>>>("/users", {
    params: compactParams({ ...query, pageNum, pageSize }),
  });
  return data;
}

export async function getUserByIdApi(id: number): Promise<Result<UserVO>> {
  const { data } = await client.get<Result<UserVO>>(`/users/${id}`);
  return data;
}

export async function createUserApi(payload: {
  username: string;
  password: string;
  realName: string;
  phone?: string;
  roleIds: number[];
}): Promise<Result<UserVO>> {
  const { data } = await client.post<Result<UserVO>>("/users", payload);
  return data;
}

export async function updateUserApi(
  id: number,
  payload: {
    username?: string;
    realName?: string;
    phone?: string;
    roleIds?: number[];
  }
): Promise<Result<UserVO>> {
  const { data } = await client.put<Result<UserVO>>(`/users/${id}`, payload);
  return data;
}

export async function updateUserStatusApi(
  id: number,
  status: number
): Promise<Result<UserVO>> {
  const { data } = await client.put<Result<UserVO>>(`/users/${id}/status`, {
    status,
  });
  return data;
}

export async function deleteUserApi(id: number): Promise<Result<void>> {
  const { data } = await client.delete<Result<void>>(`/users/${id}`);
  return data;
}

export async function listRolesApi(): Promise<Result<RoleVO[]>> {
  const { data } = await client.get<Result<RoleVO[]>>("/roles");
  return data;
}
