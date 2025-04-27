<template>
  <div class="col-md-4 offset-md-4">
    <h2>注册</h2>
    <form @submit.prevent="doRegister">
      <div class="mb-3">
        <label class="form-label">用户名</label>
        <input v-model="form.username" type="text" class="form-control" required minlength="5" maxlength="20">
      </div>
      <div class="mb-3">
        <label class="form-label">密码</label>
        <input v-model="form.password" type="password" class="form-control" required pattern="[A-Za-z0-9]{5,20}">
      </div>
      <button class="btn btn-primary">注册</button>
    </form>
  </div>
</template>

<script>
import { register } from '@/utils/auth';

export default {
  data() {
    return {
      form: { username: '', password: '' }
    };
  },
  methods: {
    async doRegister() {
      try {
        const msg = await register(this.form);
        alert(msg);
        this.$router.push('/login');
      } catch (e) {
        alert('注册失败：' + e.response.data || e.message);
      }
    }
  }
};
</script>