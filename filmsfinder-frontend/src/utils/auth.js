import http from './http';

export async function register(user) {
    const res = await http.post('/auth/register', user);
    return res.data;
}

export async function login(user) {
    const res = await http.post('/auth/login', user);
    if (res.data && res.data.token) {
        // 登录成功，将 token 存入 localStorage
        localStorage.setItem('jwt_token', res.data.token);
    }
    return res.data;
}

export async function logout() {
    const res = await http.post('/auth/logout');
    localStorage.removeItem('jwt_token');
    return res.data;
}

export async function getCurrentUser() {
    // 检查本地是否有 token，没有就直接返回 null，避免不必要的请求
    if (!localStorage.getItem('jwt_token')) {
        return Promise.reject(new Error("No token found"));
    }
    try {
        const res = await http.get('/auth/me');
        return res.data; // { id, username, userType }
    } catch (error) {
        // 如果请求失败（例如 token 过期导致 401），清除本地无效的 token
        if (error.response && error.response.status === 401) {
            localStorage.removeItem('jwt_token');
        }
        throw error;
    }
}