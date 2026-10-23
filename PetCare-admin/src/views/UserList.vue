<template>
  <div class="panel">
    <div class="panel-header">
      <h3>用户列表</h3>
      <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
    </div>

    <div style="display:flex;gap:10px;margin-bottom:14px;flex-wrap:wrap;">
      <el-select v-model="q.status" placeholder="全部状态" clearable style="width:160px;">
        <el-option label="正常" value="NORMAL" />
        <el-option label="已封禁" value="BANNED" />
        <el-option label="已注销" value="CANCELLED" />
      </el-select>
      <el-input v-model="q.keyword" placeholder="昵称/手机号" clearable style="width:240px;" @keyup.enter="go(1)" />
      <el-button type="primary" @click="go(1)">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="黑名单" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.blacklisted === 1" type="danger">已拉黑</el-tag>
          <span v-else>正常</span>
        </template>
      </el-table-column>
      <el-table-column prop="blacklistReason" label="拉黑原因" min-width="140" show-overflow-tooltip />
      <el-table-column prop="lastLoginAt" label="最后登录" width="170" />
      <el-table-column prop="createdAt" label="注册时间" width="170" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.blacklisted !== 1" type="danger" link @click="blacklist(row, 1)">拉黑</el-button>
          <el-button v-else type="success" link @click="blacklist(row, 0)">取消拉黑</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div style="display:flex;justify-content:flex-end;margin-top:14px;">
      <el-pagination
        v-model:current-page="q.page"
        v-model:page-size="q.size"
        :page-sizes="[10,20,50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="go(1)"
        @current-change="go" />
    </div>

    <el-drawer v-model="detailVisible" :title="'用户详情 · ' + (detail?.nickname || '')" size="480px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="openid">{{ detail.openid }}</el-descriptions-item>
        <el-descriptions-item label="昵称">{{ detail.nickname }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ statusLabel(detail.status) }}</el-descriptions-item>
        <el-descriptions-item label="黑名单">{{ detail.blacklisted === 1 ? '已拉黑' : '正常' }}</el-descriptions-item>
        <el-descriptions-item label="拉黑原因">{{ detail.blacklistReason || '-' }}</el-descriptions-item>
        <el-descriptions-item label="投诉标记">{{ detail.complaintMarked === 1 ? '有' : '无' }}</el-descriptions-item>
        <el-descriptions-item label="最后登录">{{ detail.lastLoginAt }}</el-descriptions-item>
        <el-descriptions-item label="注册时间">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchUsers, fetchUserDetail, updateUserBlacklist } from '@/api'

const q = reactive({ page: 1, size: 10, status: '', keyword: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const detail = ref(null)
const detailVisible = ref(false)

const statusLabel = s => ({ NORMAL: '正常', BANNED: '已封禁', CANCELLED: '已注销' }[s] || s)
const statusType = s => ({ NORMAL: 'success', BANNED: 'danger', CANCELLED: 'info' }[s] || '')

const load = async () => {
  loading.value = true
  try {
    const d = await fetchUsers({
      page: q.page, size: q.size,
      status: q.status || undefined,
      keyword: q.keyword || undefined
    })
    list.value = d.list
    total.value = d.total
  } finally { loading.value = false }
}
const go = p => { q.page = p; load() }

const openDetail = async row => {
  detail.value = await fetchUserDetail(row.id)
  detailVisible.value = true
}

const blacklist = async (row, val) => {
  let reason = ''
  if (val === 1) {
    const { value } = await ElMessageBox.prompt('请输入拉黑原因', `拉黑用户「${row.nickname}」`, {
      inputPlaceholder: '例如：恶意投诉 / 多次取消订单',
      inputValidator: v => !!v || '原因必填'
    })
    reason = value
  } else {
    await ElMessageBox.confirm(`确定取消拉黑「${row.nickname}」？`, '取消拉黑', { type: 'warning' })
  }
  await updateUserBlacklist(row.id, val, reason)
  ElMessage.success(val === 1 ? '已拉黑' : '已取消拉黑')
  load()
}

onMounted(load)
</script>
