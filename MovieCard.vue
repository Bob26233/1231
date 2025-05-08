<template>
  <div class="movie-card">
    <!-- 主封面 -->
    <div class="main-poster">
      <img :src="movie.poster" :alt="movie.title" />
    </div>

    <!-- 悬停内容 -->
    <div class="hover-content">
      <!-- 上半部分：缩略图 + 基本信息 -->
      <div class="top-section">
        <div class="thumbnail-wrapper">
          <img :src="movie.poster" :alt="movie.title" />
        </div>

        <div class="info-wrapper">
          <h3 class="title">{{ movie.title }}</h3>
          <div class="meta">
            <span class="year">{{ movie.year }}</span>
            <span class="rating" v-if="movie.rating > 0">
              {{ movie.rating.toFixed(1) }} 分
            </span>
            <span class="no-rating" v-else>暂无评分</span>
          </div>
          <div class="genres">
            <span 
              v-for="genre in movieGenres"
              :key="genre"
              class="genre"
            >
              {{ genre }}
            </span>
          </div>
        </div>
      </div>

      <!-- 下半部分：评分组件 -->
      <div class="rating-section">
        <div class="rating-control" v-if="localRating > 0">
          <el-rate
            v-model="localRating"
            :max="10"
            :colors="['#99A9BF', '#F7BA2A', '#FF9900']"
            allow-half
            @change="handleRatingChange"
          />
          <span class="rating-value">
            {{ localRating > 0 ? localRating : '点击评分' }}
          </span>
        </div>
        <el-button
          type="primary"
          size="small"
          :loading="submitting"
          @click="submitRating"
        >
          {{ localRating === 0 ? '查看详情': localRating === 1 ? '取消评分' : '保存评分' }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import axios from 'axios'
const userId = ref('');
interface Movie {
  id: number
  title: string
  poster: string
  genres: string
  year: number
  rating: number
  userRating?: number
}
const props = defineProps<{
  movie: Movie
}>()

const router = useRouter()
const userStore = useUserStore()
const submitting = ref(false)
const localRating = ref(props.movie.userRating || 0)
const isLoggedIn = ref(userStore.isLoggedIn || false)

// 处理评分变化
const handleRatingChange = (value: number) => {
  if (!isAuthenticated()) {
    ElMessage.warning('请先登录后再进行评分')
    return router.push('/LoginRegister')
  }
  localRating.value = value
}

// 提交评分
const submitRating = async () => {
  if(localRating.value > 0){
    try {
      submitting.value = true
      const response = await axios.post('/api/ratings', {
      userId: userId.value,
      movieId: props.movie.id,
      rating: localRating.value,
      timestamp: new Date().toISOString()
    })

      if (response.data.code === 200) {
        ElMessage.success('评分提交成功')
        props.movie.userRating = localRating.value
      }
    } catch (err) {
      ElMessage.error('评分提交失败')
      console.error('评分提交错误:', err)
    } finally {
      submitting.value = false
    }
  }
  else{
    router.push(`/movie/${props.movie.id}`)
  }
}

// 工具函数
const isAuthenticated = () => !!getToken()
const getToken = () => userStore.userInfos
const movieGenres = computed(() => props.movie.genres.split('/'))
onMounted(() => {
    userId.value = JSON.parse(userStore.userInfos || "").userId;
    isLoggedIn.value = userStore.isLoggedIn;
    if(isLoggedIn.value){
      const userData = JSON.parse(userStore.userInfos || "aaa");
      userId.value = userData.userId;
      const username = userData.username;
    }
    else{
      ElMessage.warning("您还未登录");
      router.push("/LoginRegister");
    }

  })
</script>

<style lang="scss" scoped>
.movie-card {
  $thumbnail-width: 100px;
  $transition-time: 0.4s;

  position: relative;
  width: 220px;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all $transition-time ease;

  &:hover {
    width: 300px;
    box-shadow: 0 8px 20px rgba(0,0,0,0.15);

    .main-poster {
      opacity: 0;
      transform: scale(1.1);
    }

    .hover-content {
      opacity: 1;
      transform: translateX(0);
    }
  }

  .main-poster {
    position: absolute;
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: all $transition-time ease;
    z-index: 2;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  }

  .hover-content {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 15px;
    padding: 20px;
    opacity: 0;
    transform: translateX(20px);
    transition: all $transition-time cubic-bezier(0.4, 0, 0.2, 1);
    background: rgba(255,255,255,0.95);
    z-index: 3;

    .top-section {
      flex: 1;
      display: flex;
      gap: 15px;
      overflow: hidden;
    }

    .thumbnail-wrapper {
      flex: 0 0 $thumbnail-width;
      height: 100%;
      border-radius: 6px;
      overflow: hidden;
      box-shadow: 0 4px 12px rgba(0,0,0,0.15);

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    .info-wrapper {
      flex: 1;
      display: flex;
      flex-direction: column;
      overflow: hidden;

      .title {
        font-size: 1.2rem;
        margin: 0 0 12px;
        color: #2c3e50;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .meta {
        display: flex;
        gap: 12px;
        font-size: 0.9rem;
        color: #666;
        margin-bottom: 15px;

        .rating {
          color: #ff9900;
          font-weight: 600;
        }
      }

      .genres {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
        margin-bottom: auto;

        .genre {
          background: rgba(73, 0, 189, 0.08);
          color: #4900bd;
          padding: 4px 12px;
          border-radius: 15px;
          font-size: 0.8rem;
        }
      }
    }

    .rating-section {
      flex-shrink: 0;
      display: flex;
      flex-direction: column;
      gap: 12px;
      padding-top: 15px;
      border-top: 1px solid #eee;

      .rating-control {
        display: flex;
        align-items: center;
        gap: 10px;

        :deep(.el-rate) {
          display: flex;
          align-items: center;
        }

        .rating-value {
          font-size: 14px;
          color: #ff9900;
          font-weight: 500;
        }
      }

      .el-button {
        align-self: flex-start;
        transition: all 0.3s ease;

        &:hover {
          transform: translateY(-2px);
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .movie-card {
    width: 100%;
    height: auto;
    aspect-ratio: 2/3;

    &:hover {
      width: 100%;
      height: auto;

      .main-poster {
        opacity: 1;
      }

      .hover-content {
        opacity: 0;
      }
    }

    .hover-content {
      display: none;
    }
  }
}
</style>