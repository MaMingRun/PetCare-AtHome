<template>
  <div class="panel">
    <div class="panel-header">
      <h3>钱包流水</h3>
      <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
    </div>

    <div style="display:flex;gap:10px;margin-bottom:14px;flex-wrap:wrap;">
      <el-select v-model="q.providerId" placeholder="全部服务师" clearable filterable style="width:220px;">
        <el-option v-for="p in providers" :key="p.id" :label="p.realName" :value="p.id" />
      </el-select>
      <el-select v-model="q.bizType" placeholder="全部类型" clearable style="width:200px;">
        <el-option label="入账待结算" value="INCOME_PENDING" />
        <el-option label="入账已结算" value="INCOME_SETTLED" />
        <el-option label="提现申请" value="WITHDRAW_APPLY" />
        <el-option label="提现驳回退回" value="WITHDRAW_REJECT" />
        <el-option label="提现已打款" value="WITHDRAW_PAID" />
      </el-select>
      <el-button type="primary" @click="go(1)">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="providerName" label="服务师" width="120" />
      <el-table-column label="类型" width="150">
        <template #default="{ row }">
          <el-tag :type="bizTypeTag(row.bizType)">{{ bizTypeLabel(row.bizType) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="金额(元)" width="120">
        <template #default="{ row }">
          <span :style="{ color: row.amount >= 0 ? '#16a34a' : '#dc2626', fontWeight: 600 }">
            {{ row.amount >= 0 ? '+' : '' }}{{ (row.amount / 100).toFixed(2) }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="余额(元)" width="120">
        <template #default="{ row }">{{ (row.balanceAfter / 100).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="bizId" label="关联单号" width="120" />
      <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
      <el-table-column prop="createdAt" label="时间" width="170" />
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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { fetchWalletFlows, fetchProviders } from '@/api'

const q = reactive({ page: 1, size: 10, providerId: null, bizType: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const providers = ref([])

const bizTypeLabel = t => ({
  INCOME_PENDING: '入账待结算',
  INCOME_SETTLED: '入账已结算',
  WITHDRAW_APPLY: '提现申请',
  WITHDRAW_REJECT: '提现驳回退回',
  WITHDRAW_PAID: '提现已打款'
}[t] || t)
const bizTypeTag = t => ({
  INCOME_PENDING: 'warning',
  INCOME_SETTLED: 'success',
  WITHDRAW_APPLY: 'info',
  WITHDRAW_REJECT: 'danger',
  WITHDRAW_PAID: 'success'
}[t] || '')

const load = async () => {
  loading.value = true
  try {
    const d = await fetchWalletFlows({
      page: q.page, size: q.size,
      providerId: q.providerId || undefined,
      bizType: q.bizType || undefined
    })
    list.value = d.list
    total.value = d.total
  } finally { loading.value = false }
}
const go = p => { q.page = p; load() }

onMounted(async () => {
  providers.value = await fetchProviders()
  load()
})
</script>
