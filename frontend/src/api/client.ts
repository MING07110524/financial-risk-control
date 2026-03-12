import axios from "axios";
import { AppServiceError } from "@/utils/result";
import { clearToken, clearUser, getToken } from "@/utils/storage";

const client = axios.create({
  baseURL: "/api",
  timeout: 10000,
});

client.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearToken();
      clearUser();
      window.location.href = "/login";
    }

    const code = error.response?.data?.code;
    const message = error.response?.data?.message;
    if (typeof code === "number" && typeof message === "string" && message.trim()) {
      return Promise.reject(new AppServiceError(code, message));
    }

    return Promise.reject(error);
  },
);

export default client;
