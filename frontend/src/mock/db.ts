import type { CurrentUser } from "@/types/auth";
import type { MockDb } from "@/mock/schema";
import {
  createDashboardStatistics,
  createHandleSummaryStatistics,
  createMockSeed,
  createRiskLevelStatistics,
  createWarningTrendStatistics,
} from "@/mock/seed";
import { getToken, getUser } from "@/utils/storage";

const MOCK_DB_KEY = "frc_mock_db";

function cloneDb<T>(value: T): T {
  return structuredClone(value);
}

function readDb(): MockDb {
  const raw = localStorage.getItem(MOCK_DB_KEY);
  if (!raw) {
    const seed = createMockSeed();
    localStorage.setItem(MOCK_DB_KEY, JSON.stringify(seed));
    return seed;
  }

  return JSON.parse(raw) as MockDb;
}

function writeDb(db: MockDb): void {
  localStorage.setItem(MOCK_DB_KEY, JSON.stringify(db));
}

export function getMockDb(): MockDb {
  return cloneDb(readDb());
}

export function updateMockDb(updater: (db: MockDb) => MockDb): MockDb {
  const nextDb = updater(getMockDb());
  writeDb(nextDb);
  return cloneDb(nextDb);
}

export function resetMockDb(): void {
  writeDb(createMockSeed());
}

export function nextId(db: MockDb, key: keyof MockDb["nextIds"]): number {
  const id = db.nextIds[key];
  if (typeof id !== "number") {
    throw new Error(`Unknown next id key: ${key}`);
  }
  db.nextIds[key] = id + 1;
  return id;
}

export function getCurrentMockUser(): CurrentUser | null {
  const token = getToken();
  const raw = getUser();

  if (!token || !raw) {
    return null;
  }

  return JSON.parse(raw) as CurrentUser;
}

export function buildDashboardBundle(db: MockDb) {
  const recentWarnings = [...db.warnings]
    .sort((left, right) => right.createTime.localeCompare(left.createTime))
    .slice(0, 5)
    .map((item) => ({
      id: item.id,
      assessmentId: item.assessmentId,
      riskDataId: item.riskDataId,
      warningCode: item.warningCode,
      warningLevel: item.warningLevel,
      warningContent: item.warningContent,
      businessNo: item.businessNo,
      customerName: item.customerName,
      warningStatus: item.warningStatus,
      createTime: item.createTime,
    }));

  return {
    dashboard: createDashboardStatistics(db),
    riskLevels: createRiskLevelStatistics(db),
    warningTrend: createWarningTrendStatistics(db),
    handleSummary: createHandleSummaryStatistics(db),
    recentWarnings,
  };
}
