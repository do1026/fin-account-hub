import React, { createContext, useContext, useState, useEffect } from 'react';
import { api } from '../shared/api';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(() => {
    const saved = localStorage.getItem('user');
    return saved ? JSON.parse(saved) : { userId: 1, name: '홍길동', email: 'hong@test.com', isLoggedIn: false, token: null };
  });

  const login = (userData) => {
    const updated = { ...userData, isLoggedIn: true };
    setUser(updated);
    localStorage.setItem('user', JSON.stringify(updated));
    
    // 'Bearer '가 실수로 포함되어 들어와도 순수 토큰만 추출해서 저장
    const rawToken = userData.token ? userData.token.replace(/^Bearer\s+/i, '') : '';
    localStorage.setItem('authToken', rawToken);
  };

  const logout = () => {
    const updated = { userId: null, name: '', email: '', isLoggedIn: false, token: null };
    setUser(updated);
    localStorage.removeItem('user');
    localStorage.removeItem('authToken');
    window.location.href = '/auth'; // 로그아웃 후 로그인 페이지로 이동
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);

export const UserProfileWidget = () => {
  const { user, logout } = useAuth();

  return (
    <div className="flex items-center space-x-3">
      <div className="w-8 h-8 rounded-full bg-slate-200 text-slate-700 font-bold flex items-center justify-center text-xs">
        {user.isLoggedIn ? user.name.charAt(0) : '?'}
      </div>
      <div className="hidden md:block text-left">
        <div className="text-xs font-bold text-slate-800">{user.isLoggedIn ? user.name : '미인증'}</div>
        <div className="text-[10px] text-slate-400">{user.isLoggedIn ? user.email : '로그인 필요'}</div>
      </div>
      {user.isLoggedIn && (
        <button onClick={logout} title="로그아웃" className="text-slate-400 hover:text-rose-600 p-1 transition text-xs">
          <i className="fa-solid fa-power-off"></i>
        </button>
      )}
    </div>
  );
};