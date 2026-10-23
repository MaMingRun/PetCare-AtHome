<template>
  <div class="panel">
    <div class="panel-header">
      <h3>提现申请</h3>
      <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
    </div>

    <div style="display:flex;gap:10px;margin-bottom:14px;">
      <el-select v-model="q.status" placeholder="全部状态" clearable style="width:160px;" @change="go(1)">
        <el-option label="待审核" value="APPLIED" />
        <el-option label="已通过" value="APPROVED" />
        <el-option label="已驳回" value="REJECTED" />
        <el-option label="已打款" value="PAID" />
      </el-select>
    </div>

    <el-table :data="list" v-loading="loading" stripe style="width:100%;">
      <el-table-column prop="withdrawalNo" label="提现单号" width="200" />
      <el-table-column prop="providerName" label="服务师" width="120" />
      <el-table-column label="金额(元)" width="110">
        <template #default="{ row }"><span class="money">{{ (row.amount/100).toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column prop="bankCardMasked" label="银行卡" width="140" />
      <el-table-column label="状态" width="110">
        <template #default="{ row }"><el-tag :type="wStatusType(row.status)">{{ wStatusLabel(row.status) }}</el-tag></template>
      </el-table-column>
      <el-table-column prop="rejectReason" label="驳回原因" show-overflow-tooltip />
      <el-table-column prop="appliedAt" label="申请时间" width="170" />
      <el-table-column prop="paidAt" label="打款时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === 'APPLIED'">
            <el-button type="success" link @click="doApprove(row)">通过</el-button>
            <el-button type="danger" link @click="doReject(row)">驳回</el-button>
          </template>
          <template v-else-if="row.status === 'APPROVED'">
            <el-button type="primary" link @click="doMarkPaid(row)">标记已打款</el-button>
          </template>
          <template v-else>-</template>
        </template>
      </el-table-column>
    </el-table>

    <div style="display:flex;justify-content:flex-end;margin-top:14px;">
      <el-pagination
        v-model:current-page="q.page"
        v-model:page-size="q.size"
        :page-sizes="[10,20,50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="go(1)"
        @current-change="go" />
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, ElInput } from 'element-plus'
import { fetchWithdrawals, approveWithdrawal, rejectWithdrawal, markWithdrawalPaid } from '@/api'

const q = reactive({ page: 1, size: 10, status: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)

const wStatusLabel = s => ({ APPLIED:'待审核', APPROVED:'已通过', REJECTED:'已驳回', PAID:'已打款' }[s] || s)
const wStatusType = s => ({ APPLIED:'warning', APPROVED:'primary', REJECTED:'danger', PAID:'success' }[s] || '')

const load = async () => {
  loading.value = true
  try {
    const d = await fetchWithdrawals({ page: q.page, size: q.size, status: q.status || undefined })
    list.value = d.list
    total.value = d.total
  } finally { loading.value = false }
}
const go = p => { q.page = p; load() }

const doApprove = async row => {
  await ElMessageBox.confirm(`通过 ${row.providerName} 的 ${(row.amount/100).toFixed(2)} 元提现？`, '审批', { type: 'warning' })
  await approveWithdrawal(row.id)
  ElMessage.success('已通过')
  load()
}
const doReject = async row => {
  const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回', { inputPattern: /.+/, inputErrorMessage: '请填写原因' })
  await rejectWithdrawal(row.id, value)
  ElMessage.success('已驳回（金额退回服务师余额）')
  load()
}
const doMarkPaid = async row => {
  const { value } = await ElMessageBox.prompt('请输入转账流水号', '标记已打款')
  await markWithdrawalPaid(row.id, value || '')
  ElMessage.success('已标记打款')
  load()
}
onMounted(load)
</script>
