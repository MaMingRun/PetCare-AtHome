<template>
  <div>
    <div class="stat-grid" v-loading="loading">
      <div class="stat-card accent-1">
        <div class="label">累计订单</div>
        <div class="value">{{ d.totalOrders || 0 }}</div>
      </div>
      <div class="stat-card accent-2">
        <div class="label">今日新增</div>
        <div class="value">{{ d.todayOrders || 0 }}</div>
      </div>
      <div class="stat-card accent-3">
        <div class="label">已支付 GMV</div>
        <div class="money value">{{ yuan(d.totalGmv) }}</div>
      </div>
      <div class="stat-card">
        <div class="label">已评价</div>
        <div class="value">{{ d.reviewedOrders || 0 }}</div>
      </div>
      <div class="stat-card accent-1">
        <div class="label">待接派单</div>
        <div class="value">{{ d.pendingAccept || 0 }}</div>
      </div>
      <div class="stat-card accent-2">
        <div class="label">待结算</div>
        <div class="value">{{ d.pendingSettlement || 0 }}</div>
      </div>
      <div class="stat-card accent-3">
        <div class="label">待审核提现</div>
        <div class="value">{{ d.pendingWithdrawal || 0 }}</div>
      </div>
      <div class="stat-card accent-4">
        <div class="label">活跃服务师</div>
        <div class="value">{{ d.totalProviders || 0 }}</div>
      </div>
    </div>

    <div class="panel">
      <div class="panel-header">
        <h3>快捷操作</h3>
        <el-button type="primary" plain @click="refresh"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>
      <div style="display:flex;gap:12px;flex-wrap:wrap;">
        <el-button type="success" :loading="settling" @click="doSettle">手动结算（待结算订单入库）</el-button>
        <el-tag type="info" effect="plain">共 {{ d.paidOrders || 0 }} 笔已支付订单</el-tag>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchDashboard, markSettleAll } from '@/api'

const d = reactive({})
const loading = ref(false)
const settling = ref(false)
const yuan = f => ((f || 0) / 100).toFixed(2)

const refresh = async () => {
  loading.value = true
  try { Object.assign(d, await fetchDashboard()) } finally { loading.value = false }
}
const doSettle = async () => {
  await ElMessageBox.confirm('确认手动触发结算？', '提示', { type: 'warning' })
  settling.value = true
  try {
    const n = await markSettleAll()
    ElMessage.success(`结算完成：${n} 笔`)
    refresh()
  } finally { settling.value = false }
}
onMounted(refresh)
</script>
