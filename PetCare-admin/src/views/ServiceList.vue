<template>
  <div class="panel">
    <div class="panel-header">
      <h3>服务项目</h3>
      <div style="display:flex;gap:10px;">
        <el-button type="primary" @click="openEdit(null)"><el-icon><Plus /></el-icon>新增服务</el-button>
        <el-button :loading="loading" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>
    </div>

    <div style="display:flex;gap:10px;margin-bottom:14px;flex-wrap:wrap;">
      <el-select v-model="q.status" placeholder="全部状态" clearable style="width:140px;">
        <el-option label="上架中" value="ON" />
        <el-option label="已下架" value="OFF" />
      </el-select>
      <el-select v-model="q.category" placeholder="全部类目" clearable style="width:160px;">
        <el-option label="喂养" value="FEEDING" />
        <el-option label="洗护" value="GROOMING" />
        <el-option label="医护" value="MEDICAL" />
      </el-select>
      <el-input v-model="q.keyword" placeholder="服务名" clearable style="width:220px;" @keyup.enter="go(1)" />
      <el-button type="primary" @click="go(1)">搜索</el-button>
    </div>

    <el-table :data="list" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="封面" width="90">
        <template #default="{ row }">
          <el-image v-if="row.coverUrl" :src="row.coverUrl" style="width:56px;height:40px;border-radius:4px;" fit="cover" />
          <span v-else style="color:#9ca3af;">无图</span>
        </template>
      </el-table-column>
      <el-table-column prop="name" label="服务名" width="140" />
      <el-table-column label="类目" width="100">
        <template #default="{ row }">
          <el-tag size="small">{{ categoryLabel(row.category) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="起步价" width="100">
        <template #default="{ row }"><span class="money">¥{{ (row.priceFrom / 100).toFixed(2) }}</span></template>
      </el-table-column>
      <el-table-column prop="durationMinutes" label="时长(分)" width="90" />
      <el-table-column label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ON' ? 'success' : 'info'">
            {{ row.status === 'ON' ? '上架中' : '已下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column prop="updatedAt" label="更新时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="openEdit(row)">编辑</el-button>
          <el-button v-if="row.status === 'OFF'" type="success" link @click="setStatus(row, 'ON')">上架</el-button>
          <el-button v-else type="warning" link @click="setStatus(row, 'OFF')">下架</el-button>
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

    <!-- 新增/编辑 Dialog -->
    <el-dialog v-model="editVisible" :title="editForm.id ? '编辑服务' : '新增服务'" width="640px">
      <el-form :model="editForm" label-width="110px">
        <el-form-item label="服务名" required>
          <el-input v-model="editForm.name" placeholder="如：上门喂养" />
        </el-form-item>
        <el-form-item label="类目" required>
          <el-select v-model="editForm.category" style="width:100%;">
            <el-option label="喂养" value="FEEDING" />
            <el-option label="洗护" value="GROOMING" />
            <el-option label="医护" value="MEDICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面 URL">
          <el-input v-model="editForm.coverUrl" placeholder="https://..." />
        </el-form-item>
        <el-form-item label="起步价（元）" required>
          <el-input-number v-model="editForm.priceFromYuan" :min="0" :precision="2" :step="1" style="width:100%;" />
        </el-form-item>
        <el-form-item label="时长（分钟）">
          <el-input-number v-model="editForm.durationMinutes" :min="0" :step="15" style="width:100%;" />
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="editForm.tags" placeholder='JSON 数组，如：["适合猫咪","新手友好"]' />
        </el-form-item>
        <el-form-item label="服务内容">
          <el-input v-model="editForm.contentItems" type="textarea" :rows="2" placeholder='JSON 数组，如：["添粮换水","清理猫砂"]' />
        </el-form-item>
        <el-form-item label="服务须知">
          <el-input v-model="editForm.notice" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="退款规则">
          <el-input v-model="editForm.refundPolicy" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="排序权重">
          <el-input-number v-model="editForm.sort" :min="0" :step="1" style="width:100%;" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { fetchServices, createService, updateService, updateServiceStatus } from '@/api'

const q = reactive({ page: 1, size: 10, status: '', category: '', keyword: '' })
const list = ref([])
const total = ref(0)
const loading = ref(false)
const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
  id: null, name: '', category: 'FEEDING', coverUrl: '',
  priceFromYuan: 0, durationMinutes: 60,
  tags: '', contentItems: '', notice: '', refundPolicy: '', sort: 0
})

const categoryLabel = c => ({ FEEDING: '喂养', GROOMING: '洗护', MEDICAL: '医护' }[c] || c)

const load = async () => {
  loading.value = true
  try {
    const d = await fetchServices({
      page: q.page, size: q.size,
      status: q.status || undefined,
      category: q.category || undefined,
      keyword: q.keyword || undefined
    })
    list.value = d.list
    total.value = d.total
  } finally { loading.value = false }
}
const go = p => { q.page = p; load() }

const openEdit = row => {
  if (row) {
    Object.assign(editForm, {
      id: row.id, name: row.name, category: row.category,
      coverUrl: row.coverUrl, priceFromYuan: row.priceFrom / 100,
      durationMinutes: row.durationMinutes, tags: row.tags,
      contentItems: row.contentItems, notice: row.notice,
      refundPolicy: row.refundPolicy, sort: row.sort
    })
  } else {
    Object.assign(editForm, {
      id: null, name: '', category: 'FEEDING', coverUrl: '',
      priceFromYuan: 0, durationMinutes: 60,
      tags: '', contentItems: '', notice: '', refundPolicy: '', sort: 0
    })
  }
  editVisible.value = true
}

const save = async () => {
  if (!editForm.name) { ElMessage.warning('服务名必填'); return }
  saving.value = true
  try {
    const payload = {
      name: editForm.name,
      category: editForm.category,
      coverUrl: editForm.coverUrl,
      priceFrom: Math.round(editForm.priceFromYuan * 100),
      durationMinutes: editForm.durationMinutes,
      tags: editForm.tags,
      contentItems: editForm.contentItems,
      notice: editForm.notice,
      refundPolicy: editForm.refundPolicy,
      sort: editForm.sort,
      status: 'OFF'
    }
    if (editForm.id) {
      await updateService(editForm.id, payload)
      ElMessage.success('已更新')
    } else {
      await createService(payload)
      ElMessage.success('已创建（默认下架，需手动上架）')
    }
    editVisible.value = false
    load()
  } finally { saving.value = false }
}

const setStatus = async (row, status) => {
  await ElMessageBox.confirm(
    `确定${status === 'ON' ? '上架' : '下架'}「${row.name}」？`,
    '状态变更', { type: 'warning' }
  )
  await updateServiceStatus(row.id, status)
  ElMessage.success(status === 'ON' ? '已上架' : '已下架')
  load()
}

onMounted(load)
</script>

<style scoped>
.money { color: #dc2626; font-weight: 600; }
</style>
