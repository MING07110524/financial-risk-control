import type { RiskDataStatus, RiskLevel, WarningStatus } from "@/types/risk";

function toDate(input: string): Date {
  return new Date(input.replace(" ", "T"));
}

export function formatDateTime(input: string): string {
  const date = toDate(input);
  if (Number.isNaN(date.getTime())) {
    return input;
  }

  const year = date.getFullYear();
  const month = `${date.getMonth() + 1}`.padStart(2, "0");
  const day = `${date.getDate()}`.padStart(2, "0");
  const hour = `${date.getHours()}`.padStart(2, "0");
  const minute = `${date.getMinutes()}`.padStart(2, "0");

  return `${year}-${month}-${day} ${hour}:${minute}`;
}

export function formatScore(value: number): string {
  return value.toFixed(2);
}

export function riskLevelLabel(level: RiskLevel): string {
  return {
    LOW: "低风险",
    MEDIUM: "中风险",
    HIGH: "高风险",
  }[level];
}

export function riskLevelTagType(level: RiskLevel): "success" | "warning" | "danger" {
  return ({
    LOW: "success",
    MEDIUM: "warning",
    HIGH: "danger",
  } as const)[level];
}

export function warningStatusLabel(status: WarningStatus): string {
  return {
    0: "待处理",
    1: "处理中",
    2: "已处理",
  }[status];
}

export function warningStatusTagType(status: WarningStatus): "danger" | "warning" | "success" {
  return ({
    0: "danger",
    1: "warning",
    2: "success",
  } as const)[status];
}

export function dataStatusLabel(status: RiskDataStatus): string {
  return {
    0: "待评估",
    1: "已评估",
    2: "待重评",
    3: "待补录",
  }[status];
}

export function dataStatusTagType(status: RiskDataStatus): "info" | "success" | "warning" | "danger" {
  return ({
    0: "info",
    1: "success",
    2: "warning",
    3: "danger",
  } as const)[status];
}

export function yesNoLabel(value: boolean): string {
  return value ? "是" : "否";
}
