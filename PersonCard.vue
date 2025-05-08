<template>
    <router-link 
      :to="`/person/${person.id}`"
      class="person-card"
    >
      <!-- 人物照片 -->
      <div class="person-photo">
        <el-image
          :src="person.photo || defaultAvatar"
          fit="cover"
          :preview-src-list="[person.photo]"
        >
          <template #error>
            <div class="photo-error">
              <el-icon><User /></el-icon>
            </div>
          </template>
        </el-image>
      </div>
  
      <!-- 人物信息 -->
      <div class="person-info">
        <h3 class="name">{{ person.name }}</h3>
        <div v-if="person.profession" class="profession">
          {{ formatProfession(person.profession) }}
        </div>
      </div>
    </router-link>
  </template>
  
  <script setup lang="ts">
  import { User } from '@element-plus/icons-vue'
  import defaultAvatar from '@/assets/app.ico'
  
  interface Person {
    id: number
    name: string
    photo?: string
    profession?: string
  }
  
  const props = defineProps<{
    person: Person
  }>()
  
  // 格式化职业信息
  const formatProfession = (profession: string) => {
    const professions = profession.split('/')
    return professions.length > 2 
      ? `${professions[0]} / ${professions[1]}...`
      : profession.replace(/\//g, ' / ')
  }
  </script>
  
  <style lang="scss" scoped>
  .person-card {
    display: block;
    background: #fff;
    border-radius: 8px;
    overflow: hidden;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
    transition: transform 0.3s ease, box-shadow 0.3s ease;
    text-decoration: none;
  
    &:hover {
      transform: translateY(-5px);
      box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
    }
  
    .person-photo {
      position: relative;
      width: 100%;
      padding-top: 150%; // 保持2:3比例
      background: #f5f7fa;
  
      .el-image {
        position: absolute;
        top: 0;
        left: 0;
        width: 100%;
        height: 100%;
      }
  
      .photo-error {
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        color: #c0c4cc;
        
        .el-icon {
          width: 40%;
          height: 40%;
        }
      }
    }
  
    .person-info {
      padding: 12px;
      text-align: center;
  
      .name {
        margin: 0;
        font-size: 0.95rem;
        color: #303133;
        line-height: 1.4;
        display: -webkit-box;
        -webkit-line-clamp: 1;
        line-clamp: 1;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
  
      .profession {
        margin-top: 4px;
        font-size: 0.8rem;
        color: #909399;
        line-height: 1.3;
        display: -webkit-box;
        -webkit-line-clamp: 2;
        line-clamp: 2;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }
    }
  }
  
  @media (max-width: 768px) {
    .person-card {
      .person-info {
        padding: 8px;
  
        .name {
          font-size: 0.9rem;
        }
  
        .profession {
          font-size: 0.75rem;
        }
      }
    }
  }
  </style>