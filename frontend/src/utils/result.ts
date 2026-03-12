import type { Result } from "@/types/common";

export class AppServiceError extends Error {
  code: number;

  constructor(code: number, message: string) {
    super(message);
    this.name = "AppServiceError";
    this.code = code;
  }
}

export function ensureSuccess<T>(result: Result<T>): T {
  if (result.code !== 0) {
    throw new AppServiceError(result.code, result.message);
  }
  return result.data;
}

export function getErrorMessage(error: unknown, fallback = "操作失败，请稍后重试"): string {
  if (error instanceof AppServiceError) {
    return error.message;
  }
  if (error instanceof Error && error.message) {
    return error.message;
  }
  return fallback;
}
