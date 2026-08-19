import axios from 'axios';

export const api = axios.create({
  // baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8000/api',
  baseURL:  'http://localhost:8000/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('authToken');
  if (token) {
    // 이미 'Bearer '가 붙어있으면 그대로 쓰고, 아니면 붙여서 전송
    config.headers.Authorization = token.startsWith('Bearer ') ? token : `Bearer ${token}`;
  }
  return config;
});