import {createRouter, createWebHistory} from 'vue-router';
import MovieList from '@/views/MovieList.vue';
import MovieDetail from '@/views/MovieDetail.vue';
import AddMovie from '@/views/AddMovie.vue';
import EditMovie from '@/views/EditMovie.vue';
import Login from '@/views/Login.vue';
import Register from '@/views/Register.vue';

const routes = [
    {path: '/', component: MovieList},
    {path: '/movie/:id', component: MovieDetail},

    // 管理员可见的增/改路由
    {path: '/add-movie', component: AddMovie},
    {path: '/edit-movie/:id', component: EditMovie},

    // 用户身份管理
    {path: '/login', component: Login},
    {path: '/register', component: Register}
];

const router = createRouter({
    history: createWebHistory(),
    routes,
    // 每次路由切换都滚动到页面顶部
    scrollBehavior(to, from, savedPosition) {
        return {left: 0, top: 0};
    }
});

export default router;