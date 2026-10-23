<template>
  <div>
    <!-- 顶部统计卡 -->
    <div class="stat-row" v-if="stat">
      <div class="stat-card"><div class="num">{{ stat.totalCount || 0 }}</div><div class="lbl">评价总数</div></div>
      <div class="stat-card"><div class="num">{{ stat.avgOverall || 0 }}</div><div class="lbl">综合均分</div></div>
      <div class="stat-card"><div class="num">{{ stat.avgProfessional || 0 }}</div><div class="lbl">专业</div></div>
      <div class="stat-card"><div class="num">{{ stat.avgPunctual || 0 }}</div><div class="lbl">准时</div></div>
      <div class="stat-card"><div class="num">{{ stat.avgCommunication || 0 }}</div><div class="lbl">沟通</div></div>
      <div class="stat-card"><div class="num">{{ stat.avgCare || 0 }}</div><div class="lbl">照料</div></div>
      <div class="stat-card star"><div class="num">5★ {{ stat.star5 || 0 }}</div><div class="lbl">4.5 分以上</div></div>
      <div class="stat-card star"><div class="num">4★ {{ stat.star4 || 0 }}</div><div class="lbl">3.5-4.5</div></div>
      <div class="stat-card star"><div class="num">3★ {{ stat.star3 || 0 }}</div><div class="lbl">2.5-3.5</div></div>
      <div class="stat-card star"><div class="num">2★ {{ stat.star2 || 0 }}</div><div class="lbl">1.5-2.5</div></div>
      <div class="stat-card star"><div class="num">1★ {{ stat.star1 || 0 }}</div><div class="lbl">1.5 分以下</div></div>
    </div>

    <div class="panel">
      <div class="panel-header">
        <h3>评价列表</h3>
        <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>

      <div style="display:flex;gap:10px;margin-bottom:14px;flex-wrap:wrap;">
        <el-select v-model="q.providerId" placeholder="全部服务师" clearable filterable style="width:220px;" @change="onProviderChange">
          <el-option v-for="p in providers" :key="p.id" :label="p.realName" :value="p.id" />
        </el-select>
        <el-select v-model="q.minScore" placeholder="全部评分" clearable style="width:160px;">
          <el-option label="≥ 4.5 分" :value="4.5" />
          <el-option label="≥ 3.5 分" :value="3.5" />
          <el-option label="≥ 2.5 分" :value="2.5" />
          <el-option label="≥ 1.5 分" :value="1.5" />
        </el-select>
        <el-button type="primary" @click="go(1)">搜索</el-button>
      </div>

      <el-table :data="list" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="orderId" label="订单ID" width="90" />
        <el-table-column prop="userId" label="用户ID" width="90" />
        <el-table-column prop="providerId" label="服务师ID" width="100" />
        <el-table-column label="综合评分" width="100">
          <template #default="{ row }">
            <span style="color:#f59e0b;font-weight:600;">
              {{ ((row.scoreProfessional + row.scorePunctual + row.scoreCommunication + row.scoreCare) / 4).toFixed(1) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="scoreProfessional" label="专业" width="70" />
        <el-table-column prop="scorePunctual" label="准时" width="70" />
        <el-table-column prop="scoreCommunication" label="沟通" width="70" />
        <el-table-column prop="scoreCare" label="照料" width="70" />
        <el-table-column prop="content" label="评价内容" min-width="200" show-overflow-tooltip />
        <el-table-column label="匿名" width="70">
          <template #default="{ row }">
            <el-tag v-if="row.anonymous === 1" type="info" size="small">匿名</el-tag>
            <span v-else>实名</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评价时间" width="170" />
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
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { fetchReviews, fetchReviewStat, fetchProviders } from '@/api'

const q = reactive({ page: 1, size: 10, providerId: null, minScore: null })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const stat = ref(null)
const providers = ref([])

const load = async () => {
  loading.value = true
  try {
    const d = await fetchReviews({
      page: q.page, size: q.size,
      providerId: q.providerId || undefined,
      minScore: q.minScore || undefined
    })
    list.value = d.list
    total.value = d.total
  } finally { loading.value = false }
}
const loadStat = async () => {
  stat.value = await fetchReviewStat(q.providerId || undefined)
}
const onProviderChange = () => { loadStat(); go(1) }
const go = p => { q.page = p; load() }

onMounted(async () => {
  providers.value = await fetchProviders()
  await loadStat()
  load()
})
</script>

<style scoped>
.stat-row { display: grid; grid-template-columns: repeat(auto-fit, minmax(140px, 1fr)); gap: 12px; margin-bottom: 16px; }
.stat-card { background: #fff; padding: 14px; border-radius: 8px; box-shadow: 0 1px 3px rgba(0,0,0,.06); }
.stat-card .num { font-size: 20px; font-weight: 700; color: #1f2937; }
.stat-card .lbl { font-size: 12px; color: #6b7280; margin-top: 4px; }
.stat-card.star .num { color: #f59e0b; }
</style>
