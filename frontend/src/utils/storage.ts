const TOKEN_KEY = "frc_access_token";
const USER_KEY = "frc_current_user";

export function getToken(): string {
  return localStorage.getItem(TOKEN_KEY) ?? "";
}

export function setToken(token: string): void {
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearToken(): void {
  localStorage.removeItem(TOKEN_KEY);
}

export function getUser(): string {
  return localStorage.getItem(USER_KEY) ?? "";
}

export function setUser(user: string): void {
  localStorage.setItem(USER_KEY, user);
}

export function clearUser(): void {
  localStorage.removeItem(USER_KEY);
}
