<template>
  <div v-if="movie" class="container movie-detail">
    <!-- 电影详情 -->
    <div class="row mb-5">
      <div class="col-md-4">
        <img
            :src="movie.posterUrl || placeholder"
            referrerpolicy="no-referrer"
            class="img-fluid rounded-3 shadow"
            alt="电影海报"
        />
      </div>
      <div class="col-md-8">
        <h1 class="mb-3">{{ movie.name }}</h1>
        <div class="detail-item">
          <span class="label">中文译名：</span>
          <span class="value">{{ movie.chineseName }}</span>
        </div>
        <div class="detail-item">
          <span class="label">上映年份：</span>
          <span class="value">{{ movie.releaseYear }}</span>
        </div>
        <div class="detail-item">
          <span class="label">类型：</span>
          <span class="value">{{ movie.type }}</span>
        </div>
        <div class="detail-item">
          <span class="label">语言：</span>
          <span class="value">{{ movie.language }}</span>
        </div>
        <div class="detail-item">
          <span class="label">时长：</span>
          <span class="value">{{ movie.duration }} 分钟</span>
        </div>
        <div class="detail-item">
          <span class="label">IMDB：</span>
          <a :href="movie.imdbLink" target="_blank">{{ movie.imdbLink }}</a>
        </div>
        <div class="detail-item">
          <span class="label">简介：</span>
          <div class="value description">{{ movie.description }}</div>
        </div>
      </div>
    </div>

    <!-- 管理员修改信息区域 -->
    <div v-if="isAdmin" class="edit-section card mb-4">
      <div class="card-body p-3">
        <h4>修改信息</h4>
        <router-link
            :to="`/edit-movie/${movie.id}`"
            class="btn btn-outline-primary btn-sm mt-2"
        >前往编辑</router-link>
      </div>
    </div>

    <!-- 下载链接模块 -->
    <div class="download-links mb-5">
      <h3 class="mb-3">下载链接</h3>
      <div class="row g-3">
        <div class="col-md-4" v-for="link in filteredLinks" :key="link.id">
          <div class="card download-card">
            <div class="card-body">
              <!-- 以文件名为链接文字 -->
              <a
                  :href="link.url"
                  target="_blank"
                  class="download-file-link"
              >{{ link.fileName || link.url }}</a>

              <div class="download-info">
                <small>大小: {{ formatFileSize(link.fileSize) }}</small>
                <small>分辨率: {{ link.resolution }}</small>
              </div>

              <small class="text-muted d-block mt-1">
                权限: {{ link.accessLevel }}
              </small>
            </div>
          </div>
        </div>
      </div>
      <div v-if="isAdmin" class="mt-3">
        <button
            class="btn btn-sm btn-outline-primary"
            @click="showAddLink = !showAddLink"
        >
          {{ showAddLink ? '取消添加' : '添加下载链接' }}
        </button>
        <div v-if="showAddLink" class="mt-3">
          <input
              v-model="newLink.url"
              placeholder="链接 URL"
              class="form-control mb-2"
          />
          <select v-model="newLink.accessLevel" class="form-select mb-2">
            <option value="VISITOR">访客</option>
            <option value="USER">登录用户</option>
            <option value="ADMIN">管理员</option>
          </select>
          <button class="btn btn-primary btn-sm" @click="addLink">
            确认添加
          </button>
        </div>
      </div>
    </div>

    <!-- 评论模块 -->
    <div class="comments-section">
      <h3 class="mb-4">观众评论 ({{ comments.length }})</h3>
      <div v-if="comments.length > 0">
        <div
            v-for="comment in comments"
            :key="comment.id"
            class="comment-card card mb-3"
        >
          <div class="card-body">
            <div class="comment-header">
              <div class="rating">
                <span
                    v-for="star in 5"
                    :key="star"
                    class="star"
                    :class="{ filled: star <= comment.rating }"
                >★</span>
              </div>
              <small class="text-muted">
                {{ formatDateTime(comment.createdAt) }}
              </small>
            </div>
            <p class="comment-content">{{ comment.content }}</p>
          </div>
        </div>
      </div>
      <div v-else class="empty-comments">
        还没有评论，快来发表你的看法吧～
      </div>

      <div class="comment-form card">
        <div class="card-body">
          <h5 class="card-title">发表你的评论</h5>
          <form @submit.prevent="submitComment">
            <div class="mb-3">
              <label class="form-label">评分</label>
              <select
                  v-model.number="newComment.rating"
                  class="form-select"
                  required
              >
                <option disabled :value="null">请选择评分</option>
                <option v-for="i in 5" :value="i">{{ i }} 星</option>
              </select>
            </div>
            <div class="mb-3">
              <label class="form-label">评论内容</label>
              <textarea
                  v-model="newComment.content"
                  class="form-control"
                  rows="3"
                  placeholder="写下你的观后感..."
                  required
              ></textarea>
            </div>
            <button type="submit" class="btn btn-outline-primary">
              提交评论
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import http from '@/utils/http';
import { getCurrentUser } from '@/utils/auth';
import placeholder from '@/assets/image/placeholder.jpg';

export default {
  data() {
    return {
      movie: null,
      comments: [],
      links: [],
      user: null,
      showAddLink: false,
      newLink: { url: '', accessLevel: 'VISITOR' },
      newComment: { rating: null, content: '' },
      placeholder
    };
  },
  computed: {
    isAdmin() {
      return this.user && this.user.userType === 'ADMIN';
    },
    filteredLinks() {
      if (!this.user) {
        return this.links.filter(l => l.accessLevel === 'VISITOR');
      }
      if (this.user.userType === 'USER') {
        return this.links.filter(l => ['VISITOR','USER'].includes(l.accessLevel));
      }
      return this.links;
    }
  },
  async created() {
    const id = this.$route.params.id;
    this.user = await getCurrentUser().catch(() => null);
    await this.loadData(id);
  },
  methods: {
    async loadData(id) {
      try {
        const [mRes, cRes, lRes] = await Promise.all([
          http.get(`/movies/${id}`),
          http.get(`/movies/${id}/comments`),
          http.get(`/movies/${id}/downloads`)
        ]);
        this.movie = mRes.data;
        this.comments = cRes.data;
        this.links = lRes.data;
      } catch (e) {
        alert('加载失败：' + (e.response?.data || e.message));
      }
    },
    formatDateTime(dateTimeStr) {
      const options = {
        year: 'numeric', month: '2-digit', day: '2-digit',
        hour: '2-digit', minute: '2-digit'
      };
      return new Date(dateTimeStr).toLocaleString(undefined, options);
    },
    formatFileSize(bytes) {
      if (!bytes && bytes !== 0) return '未知';
      const units = ['B','KB','MB','GB','TB'];
      let i = 0, size = bytes;
      while (size >= 1024 && i < units.length - 1) {
        size /= 1024;
        i++;
      }
      return `${size.toFixed(2)} ${units[i]}`;
    },
    async submitComment() {
      if (!this.newComment.rating || !this.newComment.content) {
        alert('请填写完整信息');
        return;
      }
      await http.post(`/movies/${this.movie.id}/comments`, { ...this.newComment });
      this.newComment = { rating: null, content: '' };
      await this.loadData(this.movie.id);
    },
    async addLink() {
      await http.post(`/movies/${this.movie.id}/downloads`, this.newLink,{ timeout: 30000 });
      this.newLink = { url: '', accessLevel: 'VISITOR' };
      this.showAddLink = false;
      await this.loadData(this.movie.id);
    }
  }
};
</script>
