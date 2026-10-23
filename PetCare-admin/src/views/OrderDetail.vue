<template>
  <div v-loading="loading">
    <el-page-header @back="$router.back()" :content="'订单详情 ' + detail.orderNo" style="margin-bottom:16px;" />

    <div v-if="detail.id" class="panel" style="margin-bottom:16px;">
      <div class="panel-header"><h3>基本信息</h3></div>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="statusType(detail.status)">{{ statusLabel(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="服务项目">{{ detail.serviceName }}</el-descriptions-item>
        <el-descriptions-item label="服务日期">{{ detail.startDate }}（{{ detail.days }}天 · {{ detail.timeSlot }}）</el-descriptions-item>
        <el-descriptions-item label="联系人">{{ detail.contactName }}</el-descriptions-item>
        <el-descriptions-item label="联系电话">{{ detail.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="服务地址" :span="2">{{ detail.address }}</el-descriptions-item>
        <el-descriptions-item label="入户方式">{{ detail.entryMode }}</el-descriptions-item>
        <el-descriptions-item label="金额"><span class="money">{{ (detail.payAmount/100).toFixed(2) }}</span></el-descriptions-item>
        <el-descriptions-item label="宠物" :span="2">
          <el-tag v-for="p in detail.petNames" :key="p" style="margin-right:6px;">{{ p }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ detail.remark || '-' }}</el-descriptions-item>
        <el-descriptions-item label="下单时间" :span="2">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </div>

    <!-- 状态流转日志 + 入户凭证日志 -->
    <div v-if="detail.id" class="panel">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="状态流转日志" name="status">
          <el-timeline v-if="statusLogs.length" style="padding-left:6px;">
            <el-timeline-item
              v-for="log in statusLogs"
              :key="log.id"
              :timestamp="log.createdAt"
              :type="opTypeTag(log.operatorType)">
              <div style="display:flex;gap:10px;align-items:center;flex-wrap:wrap;">
                <el-tag size="small">{{ log.fromStatus || '(起始)' }}</el-tag>
                <el-icon><Right /></el-icon>
                <el-tag size="small" type="success">{{ log.toStatus }}</el-tag>
                <el-tag size="small" :type="opTypeTag(log.operatorType)">{{ opTypeLabel(log.operatorType) }}</el-tag>
                <span v-if="log.operatorId" style="color:#6b7280;font-size:12px;">#{{ log.operatorId }}</span>
              </div>
              <div v-if="log.remark" style="color:#6b7280;font-size:13px;margin-top:4px;">{{ log.remark }}</div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无状态日志" />
        </el-tab-pane>

        <el-tab-pane label="入户凭证日志" name="credential">
          <el-table v-if="credentialLogs.length" :data="credentialLogs" stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="addressId" label="地址ID" width="90" />
            <el-table-column prop="providerId" label="服务师ID" width="100" />
            <el-table-column label="操作" width="120">
              <template #default="{ row }">
                <el-tag :type="row.action === 'VIEW' ? 'info' : 'warning'">
                  {{ row.action === 'VIEW' ? '查看凭证' : '解锁凭证' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="ip" label="IP" width="140" />
            <el-table-column prop="createdAt" label="时间" />
          </el-table>
          <el-empty v-else description="暂无凭证日志" />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { fetchOrderDetail, fetchOrderStatusLogs, fetchOrderCredentialLogs } from '@/api'

const route = useRoute()
const detail = ref({})
const loading = ref(false)
const activeTab = ref('status')
const statusLogs = ref([])
const credentialLogs = ref([])

const statusOpt = [
  { v: 'PENDING_PAY',      l: '待支付' },
  { v: 'PENDING_ACCEPT',   l: '待接单' },
  { v: 'ACCEPTED',         l: '已接单' },
  { v: 'DEPARTED',         l: '已出发' },
  { v: 'ARRIVED',          l: '已入户' },
  { v: 'SERVING',          l: '服务中' },
  { v: 'PENDING_CONFIRM',  l: '待用户确认' },
  { v: 'SETTLING',         l: '待评价' },
  { v: 'REVIEWED',         l: '已评价' },
  { v: 'FINISHED',         l: '已完成' },
  { v: 'CANCELLED',        l: '已取消' }
]
const statusLabel = s => statusOpt.find(x => x.v === s)?.l || s
const statusType = s => ({ PENDING:'warning', ACCEPTED:'primary', SERVING:'success', PENDING_CONFIRM:'warning', SETTLING:'warning', REVIEWED:'success', FINISHED:'info', CANCELLED:'info', REFUNDING:'danger' }[s] || '')

const opTypeLabel = t => ({
  USER: '用户', PROVIDER: '服务师', ADMIN: '管理员', SYSTEM: '系统'
}[t] || t)
const opTypeTag = t => ({
  USER: 'primary', PROVIDER: 'success', ADMIN: 'warning', SYSTEM: 'info'
}[t] || '')

onMounted(async () => {
  loading.value = true
  try {
    const orderId = route.params.id
    detail.value = await fetchOrderDetail(orderId)
    // 并行加载日志
    Promise.all([
      fetchOrderStatusLogs(orderId).then(d => statusLogs.value = d || []),
      fetchOrderCredentialLogs(orderId).then(d => credentialLogs.value = d || [])
    ]).catch(() => {})
  } finally { loading.value = false }
})
</script>
