<template>
    <footer class="footer">
        <div class="footer-container">
            <div class="footer-section">
                <h3 class="footer-title">关于我们</h3>
                <p class="footer-text">SEUMov网站是一个专注于电影推荐的平台，致力于为用户提供最优质的电影推荐服务。</p>
            </div>
            <div class="footer-section">
                <h3 class="footer-title">快速链接</h3>
                <ul class="footer-links">
                    <li><router-link to="/">首页</router-link></li>
                    <li><router-link to="/movies">电影列表</router-link></li>
                    <li><router-link to="/about">关于我们</router-link></li>
                    <li><router-link to="/contact">推荐大模型</router-link></li>
                </ul>
            </div>
            <div class="footer-section">
                <h3 class="footer-title">社交媒体</h3>
                <ul class="footer-social-links">
                    <li><a href="#" target="_blank"><i class="fab fa-facebook-f"></i></a></li>
                    <li><a href="#" target="_blank"><i class="fab fa-twitter"></i></a></li>
                    <li><a href="#" target="_blank"><i class="fab fa-instagram"></i></a></li>
                </ul>
            </div>
        </div>
        <div class="footer-bottom">
            <p>Copyright &copy; 2025 电影推荐系统. All Rights Reserved.</p>
        </div>
    </footer>
</template>

<script setup lang="ts">
// 这里可以添加 Footer 组件的逻辑，如果有的话
</script>

<style lang="scss" scoped>
$primary-color: #2c3e50;
$secondary-color: #4900bd;
$text-color: #34495e;
$background-color: #f8f9fa;
$border-radius: 8px;
$box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);

.footer {
    background-color: $background-color;
    padding: 4rem 0;
    border-top: 1px solid rgba(0, 0, 0, 0.1);

    .footer-container {
        max-width: 1200px;
        margin: 0 auto;
        padding: 0 1rem;
        display: grid;
        grid-template-columns: repeat(1, 1fr);
        gap: 2rem;

        @media (min-width: 768px) {
            grid-template-columns: repeat(3, 1fr);
        }
    }

    .footer-section {
        .footer-title {
            font-size: 1.2rem;
            color: $primary-color;
            margin-bottom: 1rem;
            font-weight: 600;
        }

        .footer-text {
            color: $text-color;
            line-height: 1.5;
        }

        .footer-links {
            list-style: none;
            padding: 0;
            margin: 0;

            li {
                margin-bottom: 0.5rem;

                a {
                    color: $text-color;
                    text-decoration: none;
                    transition: color 0.3s ease;

                    &:hover {
                        color: $secondary-color;
                    }
                }
            }
        }

        .footer-social-links {
            list-style: none;
            padding: 0;
            margin: 0;
            display: flex;

            li {
                margin-right: 1rem;

                a {
                    color: $text-color;
                    font-size: 1.2rem;
                    transition: color 0.3s ease;

                    &:hover {
                        color: $secondary-color;
                    }
                }
            }
        }
    }

    .footer-bottom {
        text-align: center;
        margin-top: 2rem;
        padding-top: 1rem;
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        color: $text-color;
    }
}
</style>
    