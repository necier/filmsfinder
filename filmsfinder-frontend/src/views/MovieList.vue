<!-- 文件路径: src/views/MovieList.vue -->
<template>
  <div class="container movie-list">
    <div class="row row-cols-1 row-cols-md-3 g-4">
      <div v-for="movie in movies" :key="movie.id" class="col">
        <div
            class="card shadow h-100 card-3d"
            :style="{ transform: cardTransforms[movie.id] }"
            @mousemove="handleMouseMove(movie.id, $event)"
            @mouseleave="handleMouseLeave(movie.id)"
            @click="goDetail(movie.id)"
            style="cursor: pointer;"
        >
          <div class="ratio-2x3">
            <img
                :src="movie.posterUrl || placeholder"
                referrerpolicy="no-referrer"
                alt="电影海报"
            />
          </div>
          <div class="card-body">
            <h5 class="card-title">
              {{ movie.chineseName ? movie.chineseName + ' - ' + movie.name : movie.name }}
            </h5>
          </div>
        </div>
      </div>
    </div>

    <nav v-if="totalPages > 1" aria-label="Page navigation">
      <ul class="pagination justify-content-center">
        <li class="page-item" :class="{ disabled: page <= 1 }">
          <a class="page-link" href="#" @click.prevent="changePage(page - 1)">上一页</a>
        </li>
        <li
            v-for="p in pageList"
            :key="p"
            class="page-item"
            :class="{ active: page === p, disabled: p === '...' }"
        >
          <span v-if="p === '...'" class="page-link">…</span>
          <a v-else class="page-link" href="#" @click.prevent="changePage(p)">{{ p }}</a>
        </li>
        <li class="page-item" :class="{ disabled: page >= totalPages }">
          <a class="page-link" href="#" @click.prevent="changePage(page + 1)">下一页</a>
        </li>
      </ul>
    </nav>
  </div>
</template>

<script>
import http from '@/utils/http';
import placeholder from '@/assets/image/placeholder.jpg';

function getCssVarNumber(name) {
  const val = getComputedStyle(document.documentElement)
      .getPropertyValue(name)
      .trim();
  return parseFloat(val);
}

export default {
  data() {
    return {
      movies: [],
      placeholder,
      total: 0,
      page: 1,
      size: 24,
      keyword: '',
      cardTransforms: {}
    };
  },
  computed: {
    totalPages() {
      return Math.ceil(this.total / this.size);
    },
    pageList() {
      const pages = [];
      const total = this.totalPages;
      const current = this.page;
      if (total <= 7) {
        for (let i = 1; i <= total; i++) pages.push(i);
      } else {
        pages.push(1);
        if (current > 4) pages.push('...');
        const start = Math.max(2, current - 2);
        const end = Math.min(total - 1, current + 2);
        for (let i = start; i <= end; i++) pages.push(i);
        if (current < total - 3) pages.push('...');
        pages.push(total);
      }
      return pages;
    }
  },
  watch: {
    '$route.query': {
      handler(newQ) {
        this.keyword = newQ.keyword || '';
        const p = parseInt(newQ.page, 10);
        this.page = isNaN(p) || p < 1 ? 1 : p;
        this.fetchMovies();
      },
      immediate: true,
      deep: true
    }
  },
  methods: {
    async fetchMovies() {
      try {
        const res = await http.get('/movies', {
          params: { page: this.page, keyword: this.keyword }
        });
        this.movies = res.data.movies;
        this.total = res.data.total;
        this.size = res.data.size;
      } catch (e) {
        alert('加载电影列表失败：' + (e.response?.data || e.message));
      }
    },
    changePage(p) {
      if (p < 1 || p > this.totalPages || p === this.page) return;
      this.$router.push({
        query: { page: p, ...(this.keyword ? { keyword: this.keyword } : {}) }
      });
    },
    goDetail(id) {
      this.$router.push(`/movie/${id}`);
    },
    calculateTransform(event) {
      const rect = event.currentTarget.getBoundingClientRect();
      const xRatio = (event.clientX - rect.left) / rect.width;
      const yRatio = (event.clientY - rect.top) / rect.height;
      const hoverScale = getCssVarNumber('--card-hover-scale');
      const maxTilt = getCssVarNumber('--card-max-tilt-angle');
      const rotateY = -(xRatio - 0.5) * maxTilt;
      const rotateX = -(yRatio - 0.5) * maxTilt * -1;
      return `perspective(600px) scale(${hoverScale}) rotateX(${rotateX}deg) rotateY(${rotateY}deg)`;
    },
    handleMouseMove(id, event) {
      this.cardTransforms = {
        ...this.cardTransforms,
        [id]: this.calculateTransform(event)
      };
    },
    handleMouseLeave(id) {
      const defaultScale = getCssVarNumber('--card-default-scale');
      this.cardTransforms = {
        ...this.cardTransforms,
        [id]: `perspective(600px) scale(${defaultScale}) rotateX(0deg) rotateY(0deg)`
      };
    }
  }
};
</script>
