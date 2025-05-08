<template>
    <div class="rated-movies-container">
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="is-loading" color="#409EFC" :size="40">
          <Loading />
        </el-icon>
        <span class="loading-text">正在加载您的评分记录...</span>
      </div>
  
      <!-- 错误提示 -->
      <el-alert 
        v-else-if="error" 
        title="加载失败" 
        type="error" 
        :closable="false"
        class="error-alert"
      >
        {{ errorMessage }}
      </el-alert>
  
      <!-- 内容区域 -->
      <template v-else>
        <h2 v-if="ratedMovies.length > 0" class="section-title">已评分电影（{{ ratedMovies.length }}部）</h2>
        
        <!-- 电影列表 -->
        <el-row :gutter="20" class="movie-grid">
          <el-col 
            v-for="movie in ratedMovies" 
            :key="movie.movieId"
            :xs="24" 
            :sm="12" 
            :md="8" 
            :lg="6" 
            :xl="4"
          >
            <div class="rated-movie-card">
              <!-- 电影封面 -->
              <div class="movie-cover">
                <img :src="movie.cover || '/default-cover.jpg'" :alt="movie.title">
              </div>
  
              <!-- 电影信息 -->
              <div class="movie-info">
                <h3 class="movie-title">{{ movie.title }}</h3>
                
                <div class="meta-info">
                  <span class="year">{{ movie.year }}</span>
                  <el-rate
                    v-model="movie.score"
                    disabled
                    :max="10"
                    :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
                    allow-half
                    class="rating-stars"
                  />
                  <span class="rating-value">您的评分：{{ movie.score.toFixed(1) }}</span>
                </div>
  
                <!-- 类型标签 -->
                <div class="genre-tags">
                  <el-tag
                    v-for="genre in movie.genres.split('/')"
                    :key="genre"
                    type="info"
                    size="small"
                    class="genre-tag"
                  >
                    {{ genre }}
                  </el-tag>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
  
        <!-- 空状态 -->
        <div v-if="ratedMovies.length === 0" class="empty-state">
          <el-empty description="您还没有评分过任何电影">
            <el-button type="primary" @click="goToMovies">去发现好电影</el-button>
          </el-empty>
        </div>
      </template>
    </div>
</template>
  
<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage } from 'element-plus'
  import axios from 'axios'
  import { useUserStore } from '@/stores/user'
  
  interface RatedMovie {
    movieId: number
    title: string
    cover: string
    genres: string
    year: number
    score: number
  }
  const userId = ref('');
  const router = useRouter()
  const userStore = useUserStore()
  
  // 状态管理
  const ratedMovies = ref<RatedMovie[]>([])
  const loading = ref(true)
  const error = ref(false)
  const errorMessage = ref('')
  
  // 获取用户评分
  const fetchRatedMovies = async () => {
    try {
      userId.value = JSON.parse(userStore.userInfos || "").userId;
      if (!userId) {
        throw new Error('用户未登录')
      }

      const response = await axios.get(`/api/users/${userId.value}/ratings`)
  
      if (response.data.code === 200) {
        ratedMovies.value = response.data.data.map((item: any) => ({
          movieId: item.movieId,
          title: item.title,
          cover: item.cover,
          genres: item.genres,
          year: item.year,
          score: item.score
        }))
      }
    } catch (err: any) {
      error.value = true
      errorMessage.value = err.response?.data?.message || err.message
      if (err.response?.status === 401) {
        ElMessage.warning('请先登录')
        router.push('/login')
      }
    } finally {
      loading.value = false
    }
  }
  
  // 跳转到电影列表
  const goToMovies = () => {
    router.push('/movies')
  }
  
  onMounted(() => {
    if (!userStore.isLoggedIn) {
      ElMessage.warning('请先登录查看评分记录')
      router.push('/login')
      return
    }
    fetchRatedMovies()
  })
</script>
  
<style lang="scss" scoped>
  .rated-movies-container {
    padding: 20px;
    max-width: 1200px;
    margin: 0 auto;
  
    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      padding: 40px 0;
  
      .loading-text {
        margin-top: 15px;
        color: #606266;
      }
    }
  
    .error-alert {
      margin: 20px 0;
    }
  
    .section-title {
      color: #303133;
      font-size: 1.5rem;
      margin: 20px 0;
      padding-bottom: 10px;
      border-bottom: 2px solid #409EFF;
    }
  
    .movie-grid {
      margin: -10px;
  
      .el-col {
        padding: 10px;
      }
    }
  
    .rated-movie-card {
      background: #fff;
      border-radius: 8px;
      overflow: hidden;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
      transition: transform 0.3s ease;
  
      &:hover {
        transform: translateY(-5px);
      }
  
      .movie-cover {
        position: relative;
        padding-top: 150%;
        background: #f5f7fa;
  
        img {
          position: absolute;
          top: 0;
          left: 0;
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
      }
  
      .movie-info {
        padding: 15px;
  
        .movie-title {
          font-size: 1rem;
          margin: 0 0 10px;
          color: #303133;
          line-height: 1.4;
          display: -webkit-box;
          -webkit-line-clamp: 2;
          line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
  
        .meta-info {
          display: flex;
          flex-direction: column;
          gap: 8px;
          margin-bottom: 12px;
  
          .year {
            font-size: 0.9rem;
            color: #909399;
          }
  
          .rating-stars {
            :deep(.el-rate__icon) {
              font-size: 16px;
            }
          }
  
          .rating-value {
            font-size: 0.9rem;
            color: #FF9900;
            font-weight: 500;
          }
        }
  
        .genre-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 6px;
  
          .genre-tag {
            margin-right: 6px;
            background: rgba(64, 158, 255, 0.1);
            color: #409EFF;
            border: none;
          }
        }
      }
    }
  
    .empty-state {
      margin: 40px 0;
    }
  }
  
  @media (max-width: 768px) {
    .rated-movies-container {
      padding: 10px;
  
      .section-title {
        font-size: 1.2rem;
        margin: 15px 0;
      }
  
      .rated-movie-card {
        .movie-info {
          padding: 12px;
  
          .movie-title {
            font-size: 0.9rem;
          }
        }
      }
    }
  }
</style>