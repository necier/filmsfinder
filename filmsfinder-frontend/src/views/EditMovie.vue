<template>
  <div class="container">
    <h1 class="my-4">编辑电影</h1>
    <form @submit.prevent="submitForm" class="col-md-6">
      <div class="mb-3">
        <label class="form-label">电影原名</label>
        <input v-model="form.name" type="text" class="form-control" required />
      </div>
      <div class="mb-3">
        <label class="form-label">中文译名</label>
        <input v-model="form.chineseName" type="text" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">上映年份</label>
        <input v-model.number="form.releaseYear" type="number" class="form-control" placeholder="如 2023" />
      </div>
      <div class="mb-3">
        <label class="form-label">类型</label>
        <select v-model="form.type" class="form-select">
          <option value="">请选择</option>
          <option value="FILM">电影</option>
          <option value="DOCUMENTARY">纪录片</option>
          <option value="TV_SERIES">电视剧</option>
          <option value="SHORT">短片</option>
        </select>
      </div>
      <div class="mb-3">
        <label class="form-label">语言</label>
        <input v-model="form.language" type="text" class="form-control" placeholder="如 英语" />
      </div>
      <div class="mb-3">
        <label class="form-label">片长（分钟）</label>
        <input v-model.number="form.duration" type="number" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">海报 URL</label>
        <input v-model="form.posterUrl" type="url" class="form-control" />
      </div>
      <div class="mb-3">
        <label class="form-label">简介</label>
        <textarea v-model="form.description" class="form-control" rows="3"></textarea>
      </div>
      <div class="mb-3">
        <label class="form-label">IMDB 链接</label>
        <input v-model="form.imdbLink" type="url" class="form-control" />
      </div>
      <button type="submit" class="btn btn-primary">保存</button>
    </form>
  </div>
</template>

<script>
import http from '@/utils/http';
export default {
  data() {
    return {
      movieId: null,
      form: {
        name: '',
        chineseName: '',
        releaseYear: null,
        type: '',
        language: '',
        duration: null,
        posterUrl: '',
        description: '',
        imdbLink: ''
      }
    };
  },
  async created() {
    this.movieId = this.$route.params.id;
    try {
      const res = await http.get(`/movies/${this.movieId}`);
      this.form = { ...res.data };
    } catch (e) {
      alert('加载失败：' + (e.response?.data || e.message));
      this.$router.push('/');
    }
  },
  methods: {
    async submitForm() {
      try {
        await http.put(`/movies/${this.movieId}`, this.form);
        this.$router.push(`/movie/${this.movieId}`);
        alert('保存成功');
      } catch (e) {
        alert('保存失败：' + (e.response?.data || e.message));
      }
    }
  }
};
</script>