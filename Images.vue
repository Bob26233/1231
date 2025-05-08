<template>
    <section class="hero-carousel">
      <el-carousel 
        :autoplay="true" 
        :interval="5000"
        height="600px"
        indicator-position="outside"
        arrow="always"
      >
        <el-carousel-item 
          v-for="slide in slides" 
          :key="slide.id"
          class="carousel-item"
        >
          <div 
            class="slide-content"
            :style="{ backgroundImage: `url(${slide.image})` }"
          >
            <div class="content-overlay">
              <div class="container">
                <h2 class="title">{{ slide.title }}</h2>
                <p class="description">{{ slide.description }}</p>
                <el-button 
                  type="info" 
                  round 
                  class="cta-button"
                  @click="handleCtaClick(slide)"
                >
                  {{ slide.buttonText }}
                </el-button>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>
</template>
  
<script setup lang="ts">
  import { ref } from 'vue'
  import { useRouter } from 'vue-router'
  const router = useRouter();
  interface Slide {
    id: number
    image: string
    title: string
    description: string
    buttonText: string
  }
  
  const slides = ref<Slide[]>([
    {
      id: 1,
      image: 'https://picsum.photos/1920/1080?random=1',
      title: '发现精彩电影',
      description: '探索各种类型的热门电影和即将上映的影片',
      buttonText: '开始探索'
    },
    {
      id: 2,
      image: 'https://picsum.photos/1920/1080?random=2',
      title: '热门推荐',
      description: '每日精选高分电影榜单',
      buttonText: '查看榜单'
    },
    {
      id: 3,
      image: 'https://picsum.photos/1920/1080?random=3',
      title: '个性推荐',
      description: '根据你的喜好智能推荐影片',
      buttonText: '定制推荐'
    }
  ])
  
  const handleCtaClick = (slide: Slide) => {
    router.push('/getMovieRecommand');
  }
</script>
  
<style lang="scss" scoped>
  .hero-carousel {
    position: relative;
    
    .carousel-item {
      transition: transform 0.6s ease-in-out;
      
      .slide-content {
        height: 600px;
        background-size: cover;
        background-position: center;
        position: relative;
        
        &::after {
          content: '';
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: linear-gradient(
            to top,
            rgba(0, 0, 0, 0.8) 0%,
            rgba(0, 0, 0, 0.3) 50%,
            rgba(0, 0, 0, 0.8) 100%
          );
        }
      }
    }
  
    .content-overlay {
      position: relative;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1;
      padding: 2rem;
      
      .container {
        max-width: 1200px;
        margin: 0 auto;
        text-align: center;
        color: white;
      }
    }
  
    .title {
      font-size: 3rem;
      font-weight: 700;
      margin-bottom: 1.5rem;
      text-shadow: 2px 2px 8px rgba(0, 0, 0, 0.6);
      
      @media (max-width: 768px) {
        font-size: 2rem;
      }
    }
  
    .description {
      font-size: 1.25rem;
      margin-bottom: 2rem;
      max-width: 600px;
      margin-left: auto;
      margin-right: auto;
      line-height: 1.6;
      text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.5);
      
      @media (max-width: 768px) {
        font-size: 1rem;
      }
    }
  
    .cta-button {
      padding: 1.3rem 2.5rem;
      font-size: 1.1rem;
      letter-spacing: 0.05em;
      transition: all 0.3s ease;
      
      &:hover {
        transform: translateY(-2px);
        box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
      }
    }
  
    // 覆盖Element Plus默认样式
    :deep(.el-carousel__indicators) {
      bottom: 40px;
      
      .el-carousel__indicator {
        padding: 8px;
        
        .el-carousel__button {
          width: 30px;
          height: 4px;
          border-radius: 2px;
          background-color: rgba(255, 255, 255, 0.5);
          transition: all 0.3s ease;
        }
        
        &.is-active .el-carousel__button {
          background-color: var(--el-color-info);
          width: 40px;
        }
      }
    }
  
    :deep(.el-carousel__arrow) {
      background-color: rgba(255, 255, 255, 0.2);
      border: 2px solid rgba(255, 255, 255, 0.5);
      transition: all 0.3s ease;
      
      &:hover {
        background-color: var(--el-color-info);
        border-color: var(--el-color-info);
        
        .el-icon {
          color: white;
        }
      }
      
      .el-icon {
        color: rgba(255, 255, 255, 0.8);
        font-size: 24px;
      }
    }
  }
</style>