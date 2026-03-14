<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, reactive, ref } from "vue";
import { userService } from "@/services";
import { useUserStore } from "@/stores/user";
import type { FormInstance, FormRules } from "element-plus";
import type { PageResult } from "@/types/common";
import type { RoleVO, UserVO } from "@/types/system";
import { ensureSuccess, getErrorMessage } from "@/utils/result";

const userStore = useUserStore();
const loading = ref(false);
const total = ref(0);
const records = ref<UserVO[]>([]);
const roles = ref<RoleVO[]>([]);
const currentUserId = computed(() => userStore.currentUser?.userId ?? null);

const query = reactive({
  username: "",
  realName: "",
  roleCode: "",
  status: "" as number | "",
  pageNum: 1,
  pageSize: 10,
});

const dialogVisible = ref(false);
const dialogMode = ref<"create" | "edit">("create");
const editingId = ref<number | null>(null);
const saving = ref(false);
const formRef = ref<FormInstance>();

const form = reactive({
  username: "",
  password: "",
  realName: "",
  phone: "",
  roleIds: [] as number[],
});

const selectedRoleId = computed<number | null>({
  get: () => form.roleIds[0] ?? null,
  set: (value) => {
    form.roleIds = value === null ? [] : [value];
  },
});

const rulesConfig: FormRules = {
  username: [{ required: true, message: "请输入用户名", trigger: "blur" }],
  realName: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  roleIds: [{ required: true, message: "请选择角色", trigger: "change" }],
};

const passwordRules = computed(() =>
  dialogMode.value === "create"
    ? [{ required: true, message: "请输入密码", trigger: "blur" }]
    : [{ min: 6, message: "新密码长度不能少于 6 位", trigger: "blur" }],
);

async function loadRoles() {
  const result = ensureSuccess<RoleVO[]>(await userService.listRoles());
  roles.value = result;
}

async function loadUsers() {
  loading.value = true;
  try {
    const result = ensureSuccess<PageResult<UserVO>>(
      await userService.pageUsers(
        {
          username: query.username || undefined,
          realName: query.realName || undefined,
          roleCode: query.roleCode || undefined,
          status: query.status === "" ? undefined : query.status,
        },
        query.pageNum,
        query.pageSize
      )
    );
    total.value = result.total;
    records.value = result.records;
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    loading.value = false;
  }
}

function resetForm() {
  editingId.value = null;
  form.username = "";
  form.password = "";
  form.realName = "";
  form.phone = "";
  form.roleIds = [];
  formRef.value?.clearValidate();
}

function openCreateDialog() {
  resetForm();
  dialogMode.value = "create";
  dialogVisible.value = true;
}

function openEditDialog(row: UserVO) {
  editingId.value = row.id;
  form.username = row.username;
  form.password = "";
  form.realName = row.realName;
  form.phone = row.phone || "";
  form.roleIds = row.roleIds || [];
  dialogMode.value = "edit";
  dialogVisible.value = true;
}

async function submitForm() {
  await formRef.value?.validate();
  saving.value = true;
  try {
    if (dialogMode.value === "create") {
      ensureSuccess(
        await userService.createUser({
          username: form.username,
          password: form.password,
          realName: form.realName,
          phone: form.phone || undefined,
          roleIds: form.roleIds,
        })
      );
      ElMessage.success("用户新增成功");
    } else {
      const payload: any = {
        username: form.username,
        realName: form.realName,
        phone: form.phone || undefined,
        roleIds: form.roleIds,
      };
      if (form.password) {
        payload.password = form.password;
      }
      ensureSuccess(await userService.updateUser(editingId.value!, payload));
      ElMessage.success("用户更新成功");
    }
    dialogVisible.value = false;
    await loadUsers();
  } catch (error) {
    ElMessage.error(getErrorMessage(error));
  } finally {
    saving.value = false;
  }
}

async function handleToggleStatus(row: UserVO) {
  if (isCurrentUserRow(row)) {
    ElMessage.warning("不能停用当前登录用户");
    return;
  }
  const nextStatus = row.status === 1 ? 0 : 1;
  const actionText = nextStatus === 1 ? "启用" : "停用";
  try {
    await ElMessageBox.confirm(`确定要${actionText}用户"${row.username}"吗？`, `${actionText}用户`, {
      type: "warning",
    });
    ensureSuccess(await userService.updateUserStatus(row.id, nextStatus));
    ElMessage.success(`用户已${actionText}`);
    await loadUsers();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(getErrorMessage(error));
    }
  }
}

async function handleDelete(row: UserVO) {
  if (isCurrentUserRow(row)) {
    ElMessage.warning("不能删除当前登录用户");
    return;
  }
  try {
    await ElMessageBox.confirm(`确定要删除用户"${row.username}"吗？`, "删除用户", {
      type: "warning",
    });
    ensureSuccess(await userService.deleteUser(row.id));
    ElMessage.success("用户删除成功");
    await loadUsers();
  } catch (error) {
    if (error !== "cancel") {
      ElMessage.error(getErrorMessage(error));
    }
  }
}

function handlePageChange(page: number) {
  query.pageNum = page;
  loadUsers();
}

function handlePageSizeChange(size: number) {
  query.pageSize = size;
  query.pageNum = 1;
  loadUsers();
}

function handleSearch() {
  query.pageNum = 1;
  loadUsers();
}

function handleReset() {
  query.username = "";
  query.realName = "";
  query.roleCode = "";
  query.status = "";
  query.pageNum = 1;
  loadUsers();
}

function isCurrentUserRow(row: UserVO) {
  return currentUserId.value !== null && row.id === currentUserId.value;
}

onMounted(() => {
  loadRoles();
  loadUsers();
});
</script>

<template>
  <div class="page-shell">
    <div class="page-header">
      <div>
        <h1 class="page-title">用户管理</h1>
        <p class="page-subtitle">管理员可以维护系统用户账号与角色分配。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新增用户</el-button>
    </div>

    <el-card class="section-card" shadow="never">
      <el-form :inline="true" :model="query" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="query.username" placeholder="请输入用户名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="真实姓名">
          <el-input v-model="query.realName" placeholder="请输入真实姓名" clearable @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="query.roleCode" placeholder="请选择角色" clearable>
            <el-option label="系统管理员" value="ADMIN" />
            <el-option label="风控人员" value="RISK_USER" />
            <el-option label="管理人员" value="MANAGER" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="请选择状态" clearable>
            <el-option label="启用" :value="1" />
            <el-option label="停用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="records" style="width: 100%">
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column label="登录态" min-width="100">
          <template #default="{ row }">
            <el-tag v-if="isCurrentUserRow(row)" type="info" effect="plain">当前登录</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="realName" label="真实姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="roleName" label="角色" min-width="120" />
        <el-table-column label="状态" min-width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? "启用" : "停用" }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button link type="primary" @click="openEditDialog(row)">编辑</el-button>
              <el-button
                link
                :type="row.status === 1 ? 'warning' : 'success'"
                :disabled="isCurrentUserRow(row)"
                @click="handleToggleStatus(row)"
              >
                {{ row.status === 1 ? "停用" : "启用" }}
              </el-button>
              <el-button link type="danger" :disabled="isCurrentUserRow(row)" @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '新增用户' : '编辑用户'"
      width="520px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rulesConfig" label-width="96px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" maxlength="50" :disabled="dialogMode === 'edit'" />
        </el-form-item>
        <el-form-item :label="dialogMode === 'create' ? '密码' : '新密码'" prop="password" :rules="passwordRules">
          <el-input v-model="form.password" type="password" maxlength="20" show-password :placeholder="dialogMode === 'create' ? '请输入密码' : '如不修改请留空'" />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" maxlength="50" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" maxlength="20" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="selectedRoleId" placeholder="请选择角色" class="full-width" clearable>
            <el-option v-for="role in roles" :key="role.id" :label="role.roleName" :value="role.id" />
          </el-select>
          <div class="form-helper">当前运行口径为“一个用户一个角色”，更换角色会直接覆盖原角色。</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.search-form {
  margin-bottom: 16px;
}

.table-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pagination-wrapper {
  margin-top: 16px;
  display: flex;
  justify-content: flex-end;
}

.full-width {
  width: 100%;
}

.form-helper {
  margin-top: 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #6a8298;
}
</style>
