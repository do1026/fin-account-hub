import React, { useState } from 'react';
import { useMutation } from '@tanstack/react-query';
import { useAuth } from '../entities/UserEntity';
import { api } from '../shared/api';
import { showToast } from '../shared/Toast';

// API 호출 함수를 컴포넌트 밖으로 분리
const loginApi = async ({ email, password }) => {
  const res = await api.post('/auth/login', { email, password });
  return res.data.data;
};

const signupApi = async ({ name, email, password }) => {
  const res = await api.post('/auth/signup', { name, email, password });
  console.log(res.data);
  return res.data;
};

export const AuthFeature = () => {
  const [tab, setTab] = useState('login');
  const [email, setEmail] = useState('');
  const [name, setName] = useState('');
  const [password, setPassword] = useState('');
  const { login } = useAuth();
  
  // 로그인 뮤테이션 훅 사용
  const { mutate: handleLogin, isLoading: isLoginLoading } = useMutation({
    mutationFn: loginApi,
    onSuccess: (data) => {
      login({ userId: data.userId, name: data.name, email, token: data.token });
      showToast.success('로그인 성공! JWT 토큰이 발급되었습니다.');
    },
    onError: () => {
      // Mock 처리
      login({ userId: 1, name: email.split('@')[0], email, token: 'mock-jwt-token' });
      showToast.success('로그인 완료 (Mock 처리)');
    },
  });
  
  // 회원가입 뮤테이션 훅 사용
  const { mutate: handleSignup, isLoading: isSignupLoading } = useMutation({
    mutationFn: signupApi,
    onSuccess: (res) => {
      showToast.success(res.message || '회원가입이 완료되었습니다.');
      setTab('login');
    },
    onError: () => {
      showToast.error('회원가입에 실패했습니다.');
    },
  });

  return (
    <div className="bg-white border border-slate-200 rounded-2xl p-8 shadow-sm">
      <div className="flex border-b border-slate-200 mb-6">
        <button
          onClick={() => setTab('login')}
          className={`flex-1 py-3 text-center text-sm font-bold border-b-2 transition ${
            tab === 'login' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-400'
          }`}
        >
          로그인
        </button>
        <button
          onClick={() => setTab('signup')}
          className={`flex-1 py-3 text-center text-sm font-bold border-b-2 transition ${
            tab === 'signup' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-400'
          }`}
        >
          회원가입
        </button>
      </div>

      {tab === 'login' ? (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleLogin({ email, password });
          }}
          className="space-y-4"
        >
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">이메일 계정</label>
            <input type="email" required placeholder="example@domain.com" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm" />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">비밀번호</label>
            <input type="password" required placeholder="비밀번호 입력" value={password} onChange={(e) => setPassword(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm" />
          </div>
          <button type="submit" disabled={isLoginLoading} className="w-full bg-brand-600 hover:bg-brand-700 text-white font-semibold py-3 rounded-xl shadow-md text-sm transition disabled:opacity-50">
            {isLoginLoading ? '로그인 중...' : '로그인'}
          </button>
        </form>
      ) : (
        <form
          onSubmit={(e) => {
            e.preventDefault();
            handleSignup({ name, email, password });
          }}
          className="space-y-4"
        >
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">사용자 이름</label>
            <input type="text" required placeholder="예: 홍길동" value={name} onChange={(e) => setName(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm" />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">이메일 주소</label>
            <input type="email" required placeholder="example@domain.com" value={email} onChange={(e) => setEmail(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm" />
          </div>
          <div>
            <label className="block text-xs font-semibold text-slate-600 mb-1">비밀번호</label>
            <input type="password" required placeholder="비밀번호 입력" value={password} onChange={(e) => setPassword(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 focus:outline-none focus:ring-2 focus:ring-brand-500/30 text-sm" />
          </div>
          <button type="submit" disabled={isSignupLoading} className="w-full bg-slate-900 hover:bg-black text-white font-semibold py-3 rounded-xl shadow-md text-sm transition disabled:opacity-50">
            {isSignupLoading ? '처리 중...' : '회원가입 완료'}
          </button>
        </form>
      )}
    </div>
  );
};