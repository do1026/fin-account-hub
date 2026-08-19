import React, { useMemo } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { UserProfileWidget, useAuth } from '../entities/UserEntity';
import { notificationApi } from '../entities/NotificationEntity';

export const HeaderWidget = () => {
  const location = useLocation();
  const { user } = useAuth();

  const pageTitle = useMemo(() => {
    switch (location.pathname) {
      case '/dashboard':
        return '계좌 관리 메인';
      case '/transfer':
        return '입출금 / 이체 실행';
      case '/history':
        return '거래 & 알림 내역';
      default:
        return '스마트 뱅크';
    }
  }, [location.pathname]);

  const { data: notifications = [] } = useQuery({
    queryKey: ['notifications', user?.userId],
    queryFn: () => notificationApi.getNotifications(user?.userId),
    enabled: !!user?.userId,
  });
  const unreadCount = notifications.filter((n) => !n.isRead).length;

  return (
    <header className="bg-white border-b border-slate-200 px-6 py-4 flex items-center justify-between shrink-0 shadow-sm z-10">
      <div className="flex items-center space-x-3">
        <h2 className="text-xl font-bold text-slate-800">{pageTitle}</h2>
      </div>

      <div className="flex items-center space-x-4">
        <div className="relative">
          <Link to="/history" state={{ defaultTab: 'notifications' }} className="relative p-2 block text-slate-600 hover:text-slate-900 hover:bg-slate-100 rounded-xl transition">
            <i className="fa-regular fa-bell text-lg"></i>
            {unreadCount > 0 && (
              <span className="absolute top-1 right-1 w-4 h-4 bg-rose-500 text-white rounded-full text-[10px] font-bold flex items-center justify-center">
                {unreadCount}
              </span>
            )}
          </Link>
        </div>
        <div className="h-6 w-px bg-slate-200"></div>
        <UserProfileWidget />
      </div>
    </header>
  );
};