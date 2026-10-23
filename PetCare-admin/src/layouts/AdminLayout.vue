<template>
  <!-- 登录页：全屏独立，不走 layout -->
  <router-view v-if="isPublic" />

  <!-- 业务布局：侧边栏 + 顶栏 + 内容区 -->
  <div v-else class="app-shell">
    <aside class="sidebar">
      <div class="logo">
        <span class="dot"></span>
        PetCare 管理后台
      </div>
      <el-menu
        :default-active="activeMenu"
        background-color="#001529"
        text-color="#c9d1d9"
        active-text-color="#ffffff"
        router>
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>
        <el-menu-item index="/orders">
          <el-icon><Tickets /></el-icon>
          <span>订单管理</span>
        </el-menu-item>
        <el-menu-item index="/providers">
          <el-icon><Avatar /></el-icon>
          <span>服务师管理</span>
        </el-menu-item>
        <el-menu-item index="/users">
          <el-icon><User /></el-icon>
          <span>用户管理</span>
        </el-menu-item>
        <el-menu-item index="/services">
          <el-icon><Grid /></el-icon>
          <span>服务项目</span>
        </el-menu-item>
        <el-menu-item index="/reviews">
          <el-icon><Star /></el-icon>
          <span>评价管理</span>
        </el-menu-item>
        <el-menu-item index="/wallet-flows">
          <el-icon><Coin /></el-icon>
          <span>钱包流水</span>
        </el-menu-item>
        <el-menu-item index="/withdrawals">
          <el-icon><Wallet /></el-icon>
          <span>提现管理</span>
        </el-menu-item>
      </el-menu>
    </aside>

    <main class="main">
      <header class="topbar">
        <div style="font-weight:600;">{{ pageTitle }}</div>
        <el-dropdown @command="onCmd">
          <span style="cursor:pointer;display:flex;align-items:center;gap:6px;">
            <el-icon><User /></el-icon>
            {{ adminName }}
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <section class="content"><router-view /></section>
    </main>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const adminName = ref(localStorage.getItem('admin_name') || 'admin')

const isPublic = computed(() => route.meta.public === true)
const activeMenu = computed(() => {
  if (route.path.startsWith('/orders')) return '/orders'
  return route.path
})
const pageTitle = computed(() => route.meta.title || 'PetCare 管理后台')

const onCmd = cmd => {
  if (cmd === 'logout') {
    localStorage.removeItem('admin_token')
    localStorage.removeItem('admin_name')
    ElMessage.success('已退出')
    router.push('/login')
  }
}
</script>
