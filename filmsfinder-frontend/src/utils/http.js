import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  timeout: 5000,
  withCredentials: true  // 发送跨域请求时携带 Cookie
});

http.interceptors.request.use(config => {
  // 从 localStorage 中获取 token
  const token = localStorage.getItem('jwt_token');

  // 如果 token 存在，则添加到请求头中
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
}, error => {
  return Promise.reject(error);
});

export default http;