<template>
    <header class="header-container">
      <!-- 网站Logo -->
      <router-link to="/" class="logo">
        <span class="logo-highlight">SEU</span>Mov
      </router-link>
  
      <!-- 导航菜单 -->
      <nav class="nav-menu">
        <router-link 
          v-for="item in navItems" 
          :key="item.path" 
          :to="item.path"
          class="nav-link"
          active-class="active"
        >
          {{ item.title }}
        </router-link>
      </nav>
  
      <!-- 搜索框 -->
      <div class="search-box">
        <input
          type="text"
          placeholder="Search movies..."
          v-model="searchQuery"
          @keyup.enter="performSearch"
          class="search-input"
        />
        <button @click="performSearch" class="search-button">
            <el-icon><Search /></el-icon>
        </button>
      </div>
  
      <!-- 用户控制区 -->
      <div class="user-controls">
        <template v-if="isLoggedIn">
          <div class="user-dropdown" @click="toggleDropdown">
            <img :src="userAvatar" alt="User Avatar" class="user-avatar" />
            <transition name="fade">
              <div v-show="showDropdown" class="dropdown-menu">
                <!-- <router-link to="/profile" class="dropdown-item">个人信息</router-link> -->
                <button @click="showinfo" class="dropdown-item">个人信息</button>
                <button @click="logout" class="dropdown-item">退出</button>
              </div>
            </transition>
          </div>
        </template>
        <template v-else>
          <button class="auth-button" @click="openLogin">登录</button>
          <button class="auth-button signup" @click="openSignup">注册</button>
        </template>
      </div>
    </header>
</template>
  
<script setup lang="ts">
  import { useUserStore } from '@/stores/user';
  import { ref, reactive, onMounted, watch, onUnmounted } from 'vue'
  import { useRoute, useRouter } from 'vue-router'
  const userStore = useUserStore();
  interface NavItem {
    title: string
    path: string
  }
  const route = useRoute()
  // 响应式数据
  const searchQuery = ref('')
  const showDropdown = ref(false)
  const isLoggedIn = ref(userStore.isLoggedIn || false) // 根据实际登录状态修改
  const userAvatar = ref('/src/assets/app.ico')
  
  // 导航菜单项
  const navItems: NavItem[] = reactive([
    { title: '首页', path: '/' },
    { title: '打分', path: '/dafen' },
    { title: '记录', path: '/rated' },
    { title: '最新', path: '/new' },
    { title: '最热', path: '/hot' },
    { title: '猜你喜欢', path: '/getMovieRecommand' }
  ])
  const router = useRouter()
  const dropdownRef = ref<HTMLElement | null>(null)
  // 方法
  const performSearch = () => {
    if (searchQuery.value.trim()) {
      router.push({ path: '/search', query: { q: searchQuery.value.trim() } })
      searchQuery.value = ''
    }
  }
  
  const toggleDropdown = () => {
    showDropdown.value = !showDropdown.value
  }
  
  const showinfo = () => {
    const userInfo = JSON.parse(userStore.userInfos||"");
    const userId = userInfo.userId;
    console.log(userId);
  }

  const logout = () => {
    // 实现登出逻辑
    isLoggedIn.value = false
    showDropdown.value = false
    userStore.logout();

  }
  // 点击外部关闭下拉菜单
  const handleClickOutside = (event: MouseEvent) => {
    if (dropdownRef.value) {
      showDropdown.value = false
    }
  }
  const openLogin = () => {
    router.push('/LoginRegister');
  }
  
  const openSignup = () => {
    router.push('/LoginRegister');
  }
  onMounted(()=>{
    document.addEventListener('click', handleClickOutside)
    showDropdown.value = false;
    isLoggedIn.value = userStore.isLoggedIn;
    if(isLoggedIn.value){
      const userData = JSON.parse(userStore.userInfos || "aaa");
      const userId = userData.userId;
      const username = userData.username;
    }
  })
  onUnmounted(() => {
    document.removeEventListener('click', handleClickOutside)
  })
  watch(() => userStore.isLoggedIn, (newValue) => {
    isLoggedIn.value = newValue;
    showDropdown.value = false;
  });
  watch(() => route.path, () => {
    showDropdown.value = false
  })
</script>
  
<style scoped lang="scss">
  .header-container {
    display: flex;
    z-index: 10;
    align-items: center;
    padding: 1rem 2rem;
    background: linear-gradient(135deg, rgba(16, 24, 40, 0.95) 0%, rgba(31, 41, 55, 0.95) 100%);
    backdrop-filter: blur(10px);
    position: sticky;
    top: 0;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  }
  
  .logo {
    font-size: 1.8rem;
    font-weight: 700;
    color: #fff;
    text-decoration: none;
    margin-right: 3rem;
    transition: color 0.3s ease;
  
    &:hover {
      color: #60a5fa;
    }
  
    .logo-highlight {
      color: #60a5fa;
    }
  }
  
  .nav-menu {
    display: flex;
    gap: 2rem;
    margin-right: auto;
  
    .nav-link {
      color: rgba(255, 255, 255, 0.8);
      text-decoration: none;
      font-weight: 500;
      padding: 0.5rem 1rem;
      border-radius: 0.5rem;
      transition: all 0.3s ease;
  
      &:hover {
        background: rgba(96, 165, 250, 0.1);
        color: #60a5fa;
      }
  
      &.active {
        color: #60a5fa;
        background: rgba(96, 165, 250, 0.1);
      }
    }
  }
  
  .search-box {
    display: flex;
    align-items: center;
    margin-right: 2rem;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 0.5rem;
    padding: 0.25rem;
  
    .search-input {
      background: transparent;
      border: none;
      color: white;
      padding: 0.5rem 1rem;
      width: 250px;
      outline: none;
  
      &::placeholder {
        color: rgba(255, 255, 255, 0.6);
      }
    }
  
    .search-button {
      background: none;
      border: none;
      color: rgba(255, 255, 255, 0.8);
      padding: 0.5rem 1rem;
      cursor: pointer;
      transition: color 0.3s ease;
  
      &:hover {
        color: #60a5fa;
      }
    }
  }
  
  .user-controls {
    position: relative;
  
    .auth-button {
      background: rgba(96, 165, 250, 0.1);
      border: 1px solid #60a5fa;
      color: #60a5fa;
      padding: 0.5rem 1.5rem;
      border-radius: 0.5rem;
      margin-left: 1rem;
      cursor: pointer;
      transition: all 0.3s ease;
      
      &:hover {
        background: #60a5fa;
        color: white;
      }
  
      &.signup {
        background: #60a5fa;
        color: white;
  
        &:hover {
          background: #3b82f6;
        }
      }
    }
  
    .user-dropdown {
      position: relative;
      cursor: pointer;
  
      .user-avatar {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        object-fit: cover;
        border: 2px solid #60a5fa;
      }
  
      .dropdown-menu {
        position: absolute;
        right: 0;
        top: 50px;
        background: rgba(31, 41, 55, 0.95);
        border-radius: 0.5rem;
        padding: 0.5rem 0;
        min-width: 150px;
        box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
  
        .dropdown-item {
          display: block;
          padding: 0.75rem 1.5rem;
          color: rgba(255, 255, 255, 0.8);
          text-decoration: none;
          transition: all 0.3s ease;
  
          &:hover {
            background: rgba(96, 165, 250, 0.1);
            color: #60a5fa;
          }
        }
      }
    }
  }
  
  .fade-enter-active,
  .fade-leave-active {
    transition: opacity 0.3s;
  }
  .fade-enter-from,
  .fade-leave-to {
    opacity: 0;
  }
  
  @media (max-width: 1024px) {
    .nav-menu {
      display: none;
    }
    
    .search-box {
      margin-right: 1rem;
      
      .search-input {
        width: 180px;
      }
    }
  }
</style>