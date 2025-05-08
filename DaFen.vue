<template>
    <div class="movie-container">
      <h1>电影打分</h1>
      <!-- 刷新按钮 -->
      <div class="controls">
        <button @click="fetchMovies" :disabled="loading">
          {{ loading ? '资源家在中...' : '刷新' }}
        </button>
      </div>
  
      <!-- 加载状态 -->
      <div v-if="loading" class="loading-container">
        <el-icon class="loading-icon" :size="40"><Loading /></el-icon>
      </div>
      
      <!-- 错误提示 -->
      <div v-if="error" class="error">{{ error }}</div>
  
      <!-- 电影列表 -->
      <div v-if="!loading && !error" class="movie-list">
        <MovieCard
          v-for="recommendation in recommendations"
          :key="recommendation.movieId"
          :movie="{
            id: recommendation.movieId,
            title: recommendation.title,
            poster: recommendation.cover,
            genres: recommendation.genres,
            year: recommendation.year,
            rating: recommendation.score,
            userRating:1
          }"
        />
      </div>
    </div>
</template>
  
<script setup lang="ts">
  import { ref, onMounted } from 'vue'
  import type { Recommendation } from '@/types/movie'
  import MovieCard from '@/components/MovieCard.vue'
  import axios from 'axios'
  import { ElMessage } from 'element-plus'
  import { useUserStore } from '@/stores/user'
  import { useRouter } from 'vue-router'
  const router = useRouter();
  const userId = ref('');
  const userStore = useUserStore()
  const recommendations = ref<Recommendation[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)
  const isLoggedIn = ref(userStore.isLoggedIn || false)
  const fetchMovies = async () => {
    try {
      loading.value = true
      error.value = null
      
      const response = await axios.get('/api/movies/recommendations')
      recommendations.value = response.data.data
    } catch (err) {
      error.value = '电影资源加载失败，请您重试！'
      ElMessage.error("电影资源加载失败，请您重试!");
    } finally {
      loading.value = false
    }
  }
  
  // 组件挂载时自动加载数据
  onMounted(() => {
    userId.value = userStore.userInfo?.userId || "";
    isLoggedIn.value = userStore.isLoggedIn;
    if(isLoggedIn.value){
      const userData = JSON.parse(userStore.userInfos || "aaa");
      userId.value = userData.userId;
      const username = userData.username;
      fetchMovies();
    }
    else{
      ElMessage.warning("您还未登录");
      router.push("/LoginRegister");
    }

  })
</script>
  
<style scoped lang="scss">
  h1 {
    font-size: 2rem;
    font-weight: 600;
    color: #42b983;
    margin-bottom: 2.5rem;
    text-align: center;
    position: relative;
    padding-bottom: 1rem;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 50%;
      transform: translateX(-50%);
      width: 80px;
      height: 2px;
      background: linear-gradient(90deg, #42b983, #7c4dff);
      border-radius: 2px;
    }
  }
  .loading-container {
    display: flex;
    justify-content: center;
    padding: 4rem 0;
    
    .loading-icon {
      animation: rotate 1.5s linear infinite;
      color: var(--el-color-primary);
    }
  }
  .movie-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  }
  
  .controls {
    margin-bottom: 20px;
    text-align: right;
  }
  
  button {
    padding: 8px 16px;
    background-color: #42b983;
    color: white;
    border: none;
    border-radius: 4px;
    cursor: pointer;
  }
  
  button:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
  
  .movie-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
    gap: 20px;
  }
  
  .loading, .error {
    padding: 20px;
    text-align: center;
    font-size: 1.2em;
  }
  
  .error {
    color: #ff4444;
  }
</style>