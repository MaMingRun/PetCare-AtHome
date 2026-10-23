<template>
  <div class="panel">
    <div class="panel-header">
      <h3>服务师列表</h3>
      <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
    </div>

    <div style="display:flex;gap:10px;margin-bottom:14px;flex-wrap:wrap;">
      <el-select v-model="q.status" placeholder="全部状态" clearable style="width:160px;">
        <el-option label="待审核" value="PENDING" />
        <el-option label="正常接单" value="ACTIVE" />
        <el-option label="暂停接单" value="SUSPENDED" />
        <el-option label="已封禁" value="BANNED" />
        <el-option label="已离职" value="QUIT" />
      </el-select>
      <el-input v-model="q.keyword" placeholder="姓名/手机号" clearable style="width:240px;" @keyup.enter="go(1)" />
      <el-button type="primary" @click="go(1)">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="realName" label="姓名" width="110" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="city" label="城市" width="100" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="评分" width="100">
        <template #default="{ row }">{{ (row.ratingAvg || 0).toFixed(1) }}</template>
      </el-table-column>
      <el-table-column prop="serviceCount" label="完成单数" width="90" />
      <el-table-column prop="rejectCount7d" label="7日拒单" width="90" />
      <el-table-column prop="createdAt" label="入驻时间" width="170" />
      <el-table-column label="操作" width="240" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openDetail(row)">详情</el-button>
          <el-button v-if="row.status === 'PENDING'" type="success" link @click="setStatus(row, 'ACTIVE')">通过审核</el-button>
          <el-button v-if="row.status === 'ACTIVE'" type="warning" link @click="setStatus(row, 'SUSPENDED')">暂停</el-button>
          <el-button v-if="row.status === 'SUSPENDED'" type="success" link @click="setStatus(row, 'ACTIVE')">恢复</el-button>
          <el-button v-if="row.status !== 'BANNED'" type="danger" link @click="setStatus(row, 'BANNED')">封禁</el-button>
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

    <!-- 详情 Drawer -->
    <el-drawer v-model="detailVisible" :title="'服务师详情 · ' + (detail?.realName || '')" size="540px">
      <el-descriptions v-if="detail" :column="1" border>
        <el-descriptions-item label="ID">{{ detail.id }}</el-descriptions-item>
        <el-descriptions-item label="姓名">{{ detail.realName }}</el-descriptions-item>
        <el-descriptions-item label="手机号">{{ detail.phone }}</el-descriptions-item>
        <el-descriptions-item label="城市">{{ detail.city }}</el-descriptions-item>
        <el-descriptions-item label="服务区域">{{ detail.serviceAreas }}</el-descriptions-item>
        <el-descriptions-item label="服务类型">{{ detail.serviceTypes }}</el-descriptions-item>
        <el-descriptions-item label="从业年限">{{ detail.years }} 年</el-descriptions-item>
        <el-descriptions-item label="从业经历">{{ detail.experiences }}</el-descriptions-item>
        <el-descriptions-item label="专长">{{ detail.specialties }}</el-descriptions-item>
        <el-descriptions-item label="简介">{{ detail.intro }}</el-descriptions-item>
        <el-descriptions-item label="综合评分">{{ (detail.ratingAvg || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="专业">{{ (detail.ratingProfessional || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="准时">{{ (detail.ratingPunctual || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="沟通">{{ (detail.ratingCommunication || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="照料">{{ (detail.ratingCare || 0).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="完成单数">{{ detail.serviceCount }}</el-descriptions-item>
        <el-descriptions-item label="入驻时间">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-drawer>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchProvidersPage, fetchProviderDetail, updateProviderStatus } from '@/api'

const q = reactive({ page: 1, size: 10, status: '', keyword: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const detail = ref(null)
const detailVisible = ref(false)

const statusLabel = s => ({
  PENDING: '待审核', ACTIVE: '正常接单', SUSPENDED: '暂停接单',
  BANNED: '已封禁', QUIT: '已离职'
}[s] || s)
const statusType = s => ({
  PENDING: 'warning', ACTIVE: 'success', SUSPENDED: 'info',
  BANNED: 'danger', QUIT: 'info'
}[s] || '')

const load = async () => {
  loading.value = true
  try {
    const d = await fetchProvidersPage({
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
  detail.value = await fetchProviderDetail(row.id)
  detailVisible.value = true
}

const setStatus = async (row, status) => {
  const label = statusLabel(status)
  await ElMessageBox.confirm(`确定将「${row.realName}」状态改为「${label}」？`, '状态变更', { type: 'warning' })
  await updateProviderStatus(row.id, status)
  ElMessage.success(`已${label}`)
  load()
}

onMounted(load)
</script>
