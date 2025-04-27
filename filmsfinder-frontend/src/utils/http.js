import axios from 'axios';

const http = axios.create({
  baseURL: '/api',
  timeout: 5000,
  withCredentials: true  // 发送跨域请求时携带 Cookie
});

export default http;