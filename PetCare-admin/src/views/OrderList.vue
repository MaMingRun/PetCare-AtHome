<template>
  <div class="panel">
    <!-- 顶部：搜索 + 操作 -->
    <div class="panel-header">
      <h3>订单列表</h3>
      <div style="display:flex;gap:10px;">
        <el-button type="success" plain :loading="settling" @click="doSettle">
          <el-icon><Money /></el-icon>手动结算
        </el-button>
        <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>
    </div>

    <!-- 搜索条 -->
    <div style="display:flex;gap:10px;margin-bottom:14px;flex-wrap:wrap;">
      <el-select v-model="q.status" placeholder="全部状态" clearable style="width:160px;">
        <el-option v-for="s in statusOpt" :key="s.v" :label="s.l" :value="s.v" />
      </el-select>
      <el-input v-model="q.keyword" placeholder="订单号/手机号" clearable style="width:240px;" @keyup.enter="go(1)">
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" @click="go(1)">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="list" v-loading="loading" stripe style="width:100%;">
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="serviceName" label="服务" width="120" />
      <el-table-column label="状态" width="120">
        <template #default="{ row }">
          <el-tag :type="statusType(row.status)">{{ statusLabel(row.status) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="startDate" label="服务日期" width="110" />
      <el-table-column prop="timeSlot" label="时段" width="110" />
      <el-table-column prop="contactName" label="联系人" width="160" />
      <el-table-column prop="contactPhone" label="电话" width="130" />
      <el-table-column label="金额(元)" width="100">
        <template #default="{ row }"><span class="money">{{ (row.payAmount/100).toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column prop="entryMode" label="入户方式" width="100" />
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="$router.push('/orders/' + row.id)">详情</el-button>
          <el-button
            v-if="row.status === 'PENDING_ACCEPT'"
            type="success"
            link
            @click="openDispatch(row)">派单</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div style="display:flex;justify-content:flex-end;margin-top:14px;">
      <el-pagination
        v-model:current-page="q.page"
        v-model:page-size="q.size"
        :page-sizes="[5,10,20,50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="go(1)"
        @current-change="go" />
    </div>

    <!-- 派单 Dialog -->
    <el-dialog v-model="dispatchVisible" :title="'派单给订单 ' + (dispatchTarget?.orderNo || '')" width="480px">
      <el-form label-width="90px">
        <el-form-item label="服务师">
          <el-select v-model="dispatchForm.providerId" placeholder="选择活跃服务师" style="width:100%;" filterable>
            <el-option
              v-for="p in providers"
              :key="p.id"
              :value="p.id"
              :label="`${p.realName} · ${p.city || '未填城市'} · 评分 ${(p.ratingAvg||0).toFixed(1)}`"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="dispatchForm.remark" type="textarea" :rows="2" placeholder="给服务师的备注（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatching" @click="doDispatch">确认派单</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchOrders, fetchProviders, dispatchOrder, markSettleAll } from '@/api'

const q = reactive({ page: 1, size: 10, status: '', keyword: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const providers = ref([])

// 派单 Dialog
const dispatchVisible = ref(false)
const dispatchTarget = ref(null)
const dispatchForm = reactive({ providerId: null, remark: '' })
const dispatching = ref(false)
const settling = ref(false)

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
const statusType = s => ({
  PENDING_ACCEPT:'warning', ACCEPTED:'primary', DEPARTED:'primary', ARRIVED:'primary',
  SERVING:'success', PENDING_CONFIRM:'warning', SETTLING:'warning',
  REVIEWED:'success', FINISHED:'info', CANCELLED:'info', REFUNDING:'danger'
}[s] || '')

const load = async () => {
  loading.value = true
  try {
    const d = await fetchOrders({
      page: q.page, size: q.size,
      status: q.status || undefined,
      keyword: q.keyword || undefined
    })
    list.value = d.list
    total.value = d.total
  } finally { loading.value = false }
}
const go = p => { q.page = p; load() }

const openDispatch = async row => {
  dispatchTarget.value = row
  dispatchForm.providerId = null
  dispatchForm.remark = ''
  dispatchVisible.value = true
  if (!providers.value.length) {
    providers.value = await fetchProviders()
    if (!providers.value.length) {
      ElMessage.warning('暂无活跃服务师，无法派单')
      dispatchVisible.value = false
    }
  }
}
const doDispatch = async () => {
  if (!dispatchForm.providerId) { ElMessage.warning('请选择服务师'); return }
  dispatching.value = true
  try {
    await dispatchOrder(dispatchTarget.value.id, dispatchForm.providerId, dispatchForm.remark)
    ElMessage.success('派单成功')
    dispatchVisible.value = false
    load()
  } finally { dispatching.value = false }
}

const doSettle = async () => {
  await ElMessageBox.confirm('将所有已确认待结算的订单手动结算入库？', '手动结算', { type: 'warning' })
  settling.value = true
  try {
    const n = await markSettleAll()
    ElMessage.success(`结算完成：${n} 笔订单`)
  } finally { settling.value = false }
}

onMounted(load)
</script>
