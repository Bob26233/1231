<template>
  <Images></Images>
  <section class="movie-section">
    <div class="container">
      <h2 class="section-title">热门电影</h2>
      <div class="movie-grid">
        <MovieCard 
          v-for="movie in movies"
          :key="movie.id"
          :movie="movie"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
  import { ref } from 'vue'
  import MovieCard from '@/components/MovieCard.vue'
  import Images from '@/views/Images.vue';
  interface Movie {
    id: number
    title: string
    poster: string
    genres: string
    year: number
    rating: number
    duration?: string
    director?: string
    cast?: string[]
  }

  const movies = ref<Movie[]>([
    {
      id: 1,
      title: '流浪地球2',
      poster: 'https://img9.doubanio.com/view/photo/s_ratio_poster/public/p2916835424.webp',
      genres: '科幻/冒险/灾难',
      year: 2023,
      rating: 8.4,
      duration: '173分钟'
    },
    {
      id: 2,
      title: '无名之辈',
      poster: 'https://img9.doubanio.com/view/photo/s_ratio_poster/public/p2539661066.webp',
      genres: '喜剧/剧情',
      year: 2022,
      rating: 9.0,
      director: '饶晓志'
    },
    {
      id: 3,
      title: '深海',
      poster: 'https://img1.doubanio.com/view/photo/s_ratio_poster/public/p2885941329.webp',
      genres: '动画/奇幻',
      year: 2023,
      rating: 7.8,
      duration: '112分钟'
    },
    {
      id: 4,
      title: '中国乒乓',
      poster: 'https://img1.doubanio.com/view/photo/s_ratio_poster/public/p2887401308.webp',
      genres: '运动/励志/剧情',
      year: 2023,
      rating: 7.0,
      cast: ['邓超', '孙俪']
    }
  ])
</script>

<style lang="scss" scoped>
  $primary-color: #2c3e50;
  $secondary-color: #4900bd;
  $text-color: #34495e;
  $border-radius: 8px;
  $box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);

  .movie-section {
    padding: 4rem 0;
    background-color: #f8f9fa;

    .container {
      max-width: 1200px;
      margin: 0 auto;
      padding: 0 1rem;
    }
  }

  .section-title {
    font-size: 2.2rem;
    color: $primary-color;
    margin-bottom: 2.5rem;
    text-align: center;
    position: relative;

    &::after {
      content: '';
      display: block;
      width: 60px;
      height: 3px;
      background: $secondary-color;
      margin: 1rem auto 0;
    }
  }

  .movie-grid {
    display: grid;
    grid-template-columns: repeat(1, 1fr);
    gap: 2rem;

    @media (min-width: 576px) {
      grid-template-columns: repeat(2, 1fr);
    }

    @media (min-width: 768px) {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (min-width: 992px) {
      grid-template-columns: repeat(4, 1fr);
    }
  }
</style>
    