<template>
    <div class="movie-detail-container" v-loading="loading">
      <!-- 错误提示 -->
      <el-alert
        v-if="error"
        title="加载失败"
        type="error"
        :closable="false"
        class="error-alert"
      >
        {{ errorMessage }}
      </el-alert>
  
      <!-- 电影详情内容 -->
      <div v-if="movie" class="movie-content">
        <!-- 头部信息 -->
        <div class="movie-header">
          <!-- 封面图片 -->
          <el-image
            class="movie-cover"
            :src="movie.cover"
            fit="cover"
            :preview-src-list="[movie.cover]"
          >
            <template #error>
              <div class="image-error">
                <el-icon><Picture /></el-icon>
                <span>封面加载失败</span>
              </div>
            </template>
          </el-image>
  
          <!-- 基本信息 -->
          <div class="movie-meta">
            <h1 class="title">{{ movie.name }}</h1>
            <div class="alias" v-if="movie.alias">{{ movie.alias }}</div>
            
            <!-- 评分信息 -->
            <div class="rating-section">
              <div class="douban-rating">
                <span class="label">豆瓣评分</span>
                <div class="score">{{ movie.doubanScore.toFixed(1) }}</div>
                <div class="votes">{{ formatNumber(movie.doubanVotes) }}人评价</div>
              </div>
            </div>
  
            <!-- 基本信息列表 -->
            <div class="meta-list">
              <div class="meta-item">
                <span class="label">导演：</span>
                <div class="directors">
                  <router-link
                    v-for="director in parsedDirectors"
                    :key="director.id"
                    :to="`/person/${director.id}`"
                  >
                    {{ director.name }}
                  </router-link>
                </div>
              </div>
              <div class="meta-item">
                <span class="label">主演：</span>
                <div class="actors">
                  <router-link
                    v-for="actor in parsedActors"
                    :key="actor.id"
                    :to="`/person/${actor.id}`"
                  >
                    {{ actor.name }}
                  </router-link>
                </div>
              </div>
              <div class="meta-item">
                <span class="label">类型：</span>
                <div class="genres">
                  <el-tag
                    v-for="genre in movie.genres.split('/')"
                    :key="genre"
                    size="small"
                    class="genre-tag"
                  >
                    {{ genre }}
                  </el-tag>
                </div>
              </div>
              <div class="meta-item">
                <span class="label">上映日期：</span>
                {{ movie.releaseDate }}
              </div>
              <div class="meta-item">
                <span class="label">片长：</span>
                {{ formatDuration(movie.mins) }}
              </div>
              <div class="meta-item">
                <span class="label">地区：</span>
                {{ movie.regions }}
              </div>
              <div class="meta-item">
                <span class="label">语言：</span>
                {{ movie.languages }}
              </div>
            </div>
          </div>
        </div>
  
        <!-- 剧情简介 -->
        <div class="storyline-section">
          <h2 class="section-title">剧情简介</h2>
          <div class="storyline-content">
            {{ movie.storyline || '暂无剧情简介' }}
          </div>
        </div>
  
        <!-- 演职员信息 -->
        <div class="credits-section">
          <h2 class="section-title">演职员</h2>
          <div class="credits-list">
            <!-- 导演 -->
            <div class="credit-category">
              <h3>导演</h3>
              <div class="persons">
                <PersonCard
                  v-for="director in directorsInfo"
                  :key="director.id"
                  :person="director"
                />
              </div>
            </div>
  
            <!-- 演员 -->
            <div class="credit-category">
              <h3>主演</h3>
              <div class="persons">
                <PersonCard
                  v-for="actor in actorsInfo"
                  :key="actor.id"
                  :person="actor"
                />
              </div>
            </div>
          </div>
        </div>
  
        <!-- 其他信息 -->
        <div class="external-links">
          <el-link
            v-if="movie.imdbId"
            :href="`https://www.imdb.com/title/${movie.imdbId}/`"
            target="_blank"
            type="primary"
          >
            IMDb链接
          </el-link>
          <el-link
            v-if="movie.officialSite"
            :href="movie.officialSite"
            target="_blank"
            type="primary"
          >
            官方网站
          </el-link>
        </div>
      </div>
    </div>
</template>
  
<script setup lang="ts">
  import { ref, onMounted, computed } from 'vue'
  import { useRoute } from 'vue-router'
  import axios from 'axios'
  import { ElMessage } from 'element-plus'
  import PersonCard from '@/components/PersonCard.vue'
  const formatNumber = (value: number) => value.toLocaleString()
  const formatDuration = (mins: number) => {
    const hours = Math.floor(mins / 60)
    const minutes = mins % 60
    return hours > 0 ? `${hours}小时${minutes}分钟` : `${minutes}分钟`
  }
  interface MovieDetail {
    movieId: number
    name: string
    alias: string
    cover: string
    directors: string
    actors: string
    genres: string
    languages: string
    mins: number
    officialSite: string
    regions: string
    releaseDate: string
    doubanScore: number
    doubanVotes: number
    storyline: string
    imdbId: string
    directorIds: string
    actorIds: string
  }
  
  interface PersonInfo {
    id: number
    name: string
    photo?: string
    profession?: string
  }
  
  const route = useRoute()
  const movie = ref<MovieDetail | null>(null)
  const directorsInfo = ref<PersonInfo[]>([])
  const actorsInfo = ref<PersonInfo[]>([])
  const loading = ref(true)
  const error = ref(false)
  const errorMessage = ref('')
  
  // 解析ID字符串（如："陈凯歌:1023040"）
  const parseIds = (idsStr: string) => {
    return idsStr.split('|').map(item => {
      const [name, id] = item.split(':')
      return { name, id: parseInt(id) }
    })
  }
  // 获取电影详情
  const fetchMovieDetail = async (movieId: string) => {
    try {
      const response = await axios.get(`/api/movies/${movieId}`)
      if (response.data.code === 200) {
        movie.value = response.data.data
        // 获取导演信息
        const directorIds = parseIds(movie.value?.directorIds||"")
        directorsInfo.value = await Promise.all(
          directorIds.map(({ id }) => fetchPersonInfo(id))
        )
        // 获取演员信息
        const actorIds = parseIds(movie.value?.actorIds||"")
        actorsInfo.value = await Promise.all(
          actorIds.map(({ id }) => fetchPersonInfo(id))
        )
      }
    } catch (err) {
      error.value = true
      errorMessage.value = err.response?.data?.message || '加载失败'
      ElMessage.error('获取电影详情失败')
    } finally {
      loading.value = false
    }
  }
  
  // 获取人物信息
  const fetchPersonInfo = async (personId: number) => {
    try {
      const response = await axios.get(`/api/person/${personId}`)
      return response.data.data
    } catch (err) {
      console.error('获取人物信息失败:', err)
      return { id: personId, name: '未知' }
    }
  }
  
  // 初始化加载
  onMounted(() => {
    const movieId = route.params.id as string
    if (!movieId) {
      error.value = true
      errorMessage.value = '无效的电影ID'
      return
    }
    fetchMovieDetail(movieId)
  })
  
  // 过滤器
  const filters = {
    formatNumber(value: number) {
      return value.toLocaleString()
    },
    formatDuration(mins: number) {
      const hours = Math.floor(mins / 60)
      const minutes = mins % 60
      return hours > 0 ? `${hours}小时${minutes}分钟` : `${minutes}分钟`
    }
  }
  
  // 计算属性
  const parsedDirectors = computed(() => {
    return movie.value?.directorIds.split('|').map(item => {
      const [name, id] = item.split(':')
      return { name, id: parseInt(id) }
    }) || []
  })
  
  const parsedActors = computed(() => {
    return movie.value?.actorIds.split('|').map(item => {
      const [name, id] = item.split(':')
      return { name, id: parseInt(id) }
    }) || []
  })
</script>
  
<style lang="scss" scoped>
  .movie-detail-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 20px;
  
    .movie-header {
      display: flex;
      gap: 30px;
      margin-bottom: 40px;
  
      .movie-cover {
        width: 300px;
        height: 450px;
        border-radius: 8px;
        overflow: hidden;
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  
        .image-error {
          height: 100%;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          background: #f5f7fa;
          color: #909399;
        }
      }
  
      .movie-meta {
        flex: 1;
  
        .title {
          font-size: 2.2rem;
          margin: 0 0 10px;
          color: #303133;
        }
  
        .alias {
          font-size: 1.2rem;
          color: #606266;
          margin-bottom: 20px;
        }
  
        .rating-section {
          margin-bottom: 30px;
  
          .douban-rating {
            display: flex;
            align-items: baseline;
            gap: 15px;
  
            .label {
              font-size: 1.1rem;
              color: #606266;
            }
  
            .score {
              font-size: 2.4rem;
              color: #ff9900;
              font-weight: 600;
            }
  
            .votes {
              color: #909399;
            }
          }
        }
  
        .meta-list {
          .meta-item {
            margin-bottom: 12px;
            font-size: 0.95rem;
  
            .label {
              color: #606266;
              min-width: 70px;
              display: inline-block;
            }
  
            .genres {
              display: inline-flex;
              gap: 8px;
  
              .genre-tag {
                background: rgba(64, 158, 255, 0.1);
                color: #409EFF;
                border: none;
              }
            }
  
            a {
              color: #409EFF;
              margin-right: 10px;
  
              &:hover {
                color: #79bbff;
              }
            }
          }
        }
      }
    }
  
    .section-title {
      color: #303133;
      font-size: 1.4rem;
      border-left: 4px solid #409EFF;
      padding-left: 12px;
      margin: 30px 0 20px;
    }
  
    .storyline-content {
      line-height: 1.8;
      color: #606266;
      font-size: 0.95rem;
      white-space: pre-wrap;
    }
  
    .credits-list {
      .credit-category {
        margin-bottom: 30px;
  
        h3 {
          font-size: 1.2rem;
          color: #303133;
          margin: 0 0 15px;
        }
  
        .persons {
          display: grid;
          grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
          gap: 20px;
        }
      }
    }
  
    .external-links {
      margin-top: 30px;
      display: flex;
      gap: 20px;
    }
  }
  
  @media (max-width: 768px) {
    .movie-detail-container {
      padding: 15px;
  
      .movie-header {
        flex-direction: column;
  
        .movie-cover {
          width: 100%;
          height: auto;
          aspect-ratio: 2/3;
        }
      }
  
      .title {
        font-size: 1.8rem !important;
      }
  
      .meta-item {
        font-size: 0.9rem !important;
      }
    }
  }
  </style>