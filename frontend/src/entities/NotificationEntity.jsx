import React from 'react';
import { api } from '../shared/api';

export const notificationApi = {
  getNotifications: async (userId) => { // 👈 userId를 인자로 받습니다.
    const res = await api.get('/notifications', {
      params: { userId } // 👈 쿼리 파라미터로 전달
    });
    return res.data;
  },
  markAsRead: async (notificationId) => {
    const res = await api.patch(`/notifications/${notificationId}/read`);
    return res.data;
  },
};

export const NotificationItem = ({ notification, actionButton }) => {
  const isFail = notification.type === 'TRANSACTION_FAILED';

  return (
    <div className={`p-4 rounded-xl border transition flex items-start justify-between space-x-3 ${
      notification.isRead ? 'bg-slate-50 border-slate-200 opacity-70' : 'bg-brand-50/40 border-brand-200'
    }`}>
      <div className="flex items-start space-x-3">
        <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs text-white ${isFail ? 'bg-rose-500' : 'bg-brand-600'}`}>
          <i className={`fa-solid ${isFail ? 'fa-circle-exclamation' : 'fa-check'}`}></i>
        </div>
        <div>
          <div className="text-xs font-bold text-slate-800">{notification.message}</div>
          <div className="text-[10px] text-slate-400 mt-1">{notification.createdAt}</div>
        </div>
      </div>
      {!notification.isRead ? actionButton : <span className="text-[10px] text-slate-400 font-semibold">읽음</span>}
    </div>
  );
};