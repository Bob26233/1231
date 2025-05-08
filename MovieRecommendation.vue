<template>
  <div class="container">
    <h1>电影推荐</h1>

    <div v-if="loading" class="loading-container">
      <el-icon class="loading-icon" :size="40"><Loading /></el-icon>
    </div>
    <div v-else-if="error" class="error-message">
      {{ error }}
    </div>
    <div v-else>
      <h2>猜你喜欢</h2>
      <div class="movie-grid">
        <div 
          v-for="recommendation in recommendations" 
          :key="recommendation.movieId"
          class="movie-item"
        >
          <MovieCard 
            :movie="{
              id: recommendation.movieId,
              title: recommendation.title,
              poster: recommendation.cover,
              genres: recommendation.genres,
              year: recommendation.year,
              rating: recommendation.score
            }" 
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 2rem 1rem;
}

// 新增卡片尺寸调整样式
.movie-item {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

h1 {
  font-size: 2rem;
  font-weight: 600;
  color: var(--el-color-primary);
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
    background: linear-gradient(90deg, var(--el-color-primary), #7c4dff);
    border-radius: 2px;
  }
}

h2 {
  font-size: 1.5rem;
  font-weight: 500;
  color: #2c3e50;
  margin-bottom: 2.5rem;
  text-align: center;
  position: relative;
  
  &::before {
    content: "🎬";
    margin-right: 12px;
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

.error-message {
  padding: 1.5rem;
  background: var(--el-color-danger-light-9);
  color: var(--el-color-danger);
  border-radius: 8px;
  text-align: center;
  max-width: 600px;
  margin: 2rem auto;
  font-weight: 500;
}

.movie-grid {
  display: grid;
  gap: 1rem;
  padding: 1.5rem 0;

  // 保持卡片宽高比
  grid-auto-rows: minmax(300px, auto);

  @media (min-width: 640px) {
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  }

  @media (min-width: 1024px) {
    grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
  }

  .movie-item {
    position: relative;
    transition: transform 0.3s ease;
    
    &:hover {
      z-index: 10;
      transform: translateY(-5px);
      
      // 防止相邻卡片重叠
      ~ .movie-item {
        transform: translateY(5px);
      }
    }
  }
}

@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
  
<script lang="ts" setup>
  import MovieCard from "@/components/MovieCard.vue"
  import { onMounted, ref, watch } from 'vue';
  import { useUserStore, type UserInfo } from '@/stores/user';
  import { useRouter } from 'vue-router';
  import { ElMessage } from 'element-plus';
  const userStore = useUserStore()
  const router = useRouter();
  // 定义响应式变量
  const userId = ref('');
  const recommendations = ref<{
    movieId: number;
    title: string;
    cover: string;
    genres: string;
    year: number;
    score: number }[]>([]);
  const loading = ref(false);
  const error = ref('');
  
  const isLoggedIn = ref(userStore.isLoggedIn || false) // 根据实际登录状态修改
  // 定义获取推荐的函数
  const fetchRecommendations = async () => {
    loading.value = true;
    error.value = '';
    try {
      const response = await fetch(`http://localhost:8080/api/recommend?userId=${userId.value}`);
      if (!response.ok) {
        throw new Error('请求失败');
      }
      const data = await response.json();
      if (data.code === 200) {
        recommendations.value = data.data;
      } else {
        error.value = data.message;
      }
    } catch (err) {
      if (err instanceof Error) {
        error.value = err.message;
      } else {
        error.value = '发生未知错误';
      }
    } finally {
      loading.value = false;
    }
  };
  onMounted(()=>{
    userId.value = userStore.userInfo?.userId || "";
    isLoggedIn.value = userStore.isLoggedIn;
    if(isLoggedIn.value){
      const userData = JSON.parse(userStore.userInfos || "");
      userId.value = userData.userId;
      const username = userData.username;
      fetchRecommendations();
    }
    else{
      ElMessage.warning("您还未登录");
      router.back();
    }
  })

</script>
  
