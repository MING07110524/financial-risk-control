export function compactParams<T extends object>(params: T): Partial<T> {
  return Object.fromEntries(
    Object.entries(params as Record<string, unknown>).filter(([, value]) => value !== "" && value !== null && value !== undefined),
  ) as Partial<T>;
}
