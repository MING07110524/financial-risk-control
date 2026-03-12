import client from "@/api/client";
import type { CurrentUser, LoginRequest, LoginUser } from "@/types/auth";
import type { Result } from "@/types/common";

export async function loginApi(payload: LoginRequest): Promise<Result<LoginUser>> {
  const { data } = await client.post<Result<LoginUser>>("/auth/login", payload);
  return data;
}

export async function logoutApi(): Promise<Result<void>> {
  const { data } = await client.post<Result<void>>("/auth/logout");
  return data;
}

export async function fetchCurrentUserApi(): Promise<Result<CurrentUser>> {
  const { data } = await client.get<Result<CurrentUser>>("/auth/me");
  return data;
}
