<template>
  <div class="login-wrap">
    <el-form class="login-card" :model="form" :rules="rules" ref="formRef" @submit.prevent>
      <h2>PetCare 管理后台</h2>
      <p class="sub">宠物上门护理 · 运营控制台</p>
      <el-form-item prop="username"><el-input v-model="form.username" placeholder="用户名" size="large" :prefix-icon="User" /></el-form-item>
      <el-form-item prop="password"><el-input v-model="form.password" type="password" show-password placeholder="密码" size="large" :prefix-icon="Lock" /></el-form-item>
      <el-button type="primary" size="large" style="width:100%;" :loading="loading" @click="doLogin">登录</el-button>
      <p style="color:#999;font-size:12px;margin-top:16px;text-align:center;">默认账号 admin / 123456</p>
    </el-form>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { login } from '@/api'

const router = useRouter()
const route = useRoute()
const formRef = ref()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const doLogin = async () => {
  await formRef.value.validate()
  loading.value = true
  try {
    const data = await login(form.username, form.password)
    localStorage.setItem('admin_token', data.token)
    localStorage.setItem('admin_name', form.username)
    ElMessage.success('登录成功')
    router.replace(route.query.redirect || '/dashboard')
  } finally {
    loading.value = false
  }
}
</script>
