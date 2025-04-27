<template>
  <div class="col-md-4 offset-md-4">
    <h2>登录</h2>
    <form @submit.prevent="doLogin">
      <div class="mb-3">
        <label class="form-label">用户名</label>
        <input v-model="form.username" type="text" class="form-control" required minlength="5" maxlength="20">
      </div>
      <div class="mb-3">
        <label class="form-label">密码</label>
        <input v-model="form.password" type="password" class="form-control" required pattern="[A-Za-z0-9]{5,20}">
      </div>
      <button class="btn btn-primary">登录</button>
    </form>
  </div>
</template>

<script>
import {login} from '@/utils/auth';

export default {
  data() {
    return {
      form: {username: '', password: ''}
    };
  },
  methods: {
    async doLogin() {
      try {
        await login(this.form);
        //alert('登录成功');
        //刷新页面，让 App.vue 重新执行 created()，拿到当前用户
        window.location.href = '/';
        this.$router.push('/');
      } catch (e) {
        alert('登录失败：' + e.response.data || e.message);
      }
    }
  }
};
</script>