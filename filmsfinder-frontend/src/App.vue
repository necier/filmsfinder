<template>
  <div>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark sticky-top">
      <div class="container">
        <!-- 品牌和折叠按钮 -->
        <router-link to="/" class="navbar-brand">FilmsFinder</router-link>
        <button
            class="navbar-toggler"
            type="button"
            data-bs-toggle="collapse"
            data-bs-target="#navbarSupportedContent"
            aria-controls="navbarSupportedContent"
            aria-expanded="false"
            aria-label="Toggle navigation"
        >
          <span class="navbar-toggler-icon"></span>
        </button>

        <!-- 折叠内容 -->
        <div class="collapse navbar-collapse" id="navbarSupportedContent">
          <!-- 左侧菜单 + 搜索 -->
          <ul class="navbar-nav me-auto mb-2 mb-lg-0 align-items-center">
            <li class="nav-item">
              <router-link to="/" class="nav-link">首页</router-link>
            </li>
            <li class="nav-item" v-if="isAdmin">
              <router-link to="/add-movie" class="nav-link">添加电影</router-link>
            </li>
            <li class="nav-item">
              <!-- 苹果风格内嵌搜索框 -->
              <form class="ms-3" @submit.prevent="onSearch">
                <div class="apple-search-group">
                  <input
                      v-model="keyword"
                      class="apple-search-input"
                      type="search"
                      placeholder="搜索电影"
                      aria-label="Search"
                  />
                  <button class="apple-search-btn" type="submit">
                    <!-- 内嵌放大镜 SVG -->
                    <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 16 16" fill="currentColor">
                      <path
                          d="M11.742 10.344a6.5 6.5 0 1 0-1.397 1.398h-.001l3.85 3.85a1 1 0 0 0 1.415-1.414l-3.85-3.85zm-5.242.656a5 5 0 1 1 0-10 5 5 0 0 1 0 10z"
                      />
                    </svg>
                  </button>
                </div>
              </form>
            </li>
          </ul>

          <!-- 右侧登录/用户名 -->
          <ul class="navbar-nav mb-2 mb-lg-0 align-items-center">
            <template v-if="!user">
              <li class="nav-item">
                <router-link to="/login" class="nav-link">登录</router-link>
              </li>
              <li class="nav-item">
                <router-link to="/register" class="nav-link">注册</router-link>
              </li>
            </template>
            <template v-else>
              <li class="nav-item">
                <span class="nav-link">{{ user.username }}</span>
              </li>
              <li class="nav-item">
                <a href="#" class="nav-link" @click.prevent="doLogout">退出</a>
              </li>
            </template>
          </ul>
        </div>
      </div>
    </nav>

    <div class="container mt-4">
      <router-view />
    </div>
  </div>
</template>

<script>
import { getCurrentUser, logout } from '@/utils/auth';

export default {
  data() {
    return {
      user: null,
      keyword: ''
    };
  },
  computed: {
    isAdmin() {
      return this.user && this.user.userType === 'ADMIN';
    }
  },
  async created() {
    try {
      this.user = await getCurrentUser();
    } catch {
      this.user = null;
    }
    this.keyword = this.$route.query.keyword || '';
  },
  methods: {
    async doLogout() {
      await logout();
      this.user = null;
      this.$router.push('/');
    },
    onSearch() {
      this.$router.push({ path: '/', query: { keyword: this.keyword || undefined } });
    }
  }
};
</script>
