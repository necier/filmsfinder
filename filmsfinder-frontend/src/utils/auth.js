import http from './http';

export async function register(user) {
    const res = await http.post('/auth/register', user);
    return res.data;
}

export async function login(user) {
    const res = await http.post('/auth/login', user);
    return res.data;
}

export async function logout() {
    const res = await http.post('/auth/logout');
    return res.data;
}

export async function getCurrentUser() {
    // 假设后端提供该接口
    const res = await http.get('/auth/me');
    return res.data;  // { id, username, userType }
}