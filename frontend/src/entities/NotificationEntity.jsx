import React from 'react';
import { api } from '../shared/api';

export const notificationApi = {
  getNotifications: async (userId) => {
    const res = await api.get('/notifications', {
      params: { userId }
    });
    return res.data;
  },
  markAsRead: async (notificationId) => {
    const res = await api.patch(`/notifications/${notificationId}/read`);
    console.log("읽음 처리 성공 응답:", res); // 👉 res 자체를 찍어보기
    return res.data;
  },
};

export const NotificationItem = ({ notification, actionButton, onMarkAsRead }) => {
  // 👉 백엔드 필드명인 id, description, read 에 맞게 매핑
  const message = notification.description;
  const isRead = notification.read; // 👉 isRead 대신 read 속성 사용

  const isFail = notification.transactionType === 'TRANSACTION_FAILED';
  const isUnread = !isRead;

  return (
    <div
      onClick={isUnread && onMarkAsRead ? onMarkAsRead : undefined}
      className={`p-4 rounded-xl border transition flex items-start justify-between space-x-3 ${
        isRead ? 'bg-slate-50 border-slate-200 opacity-70' : 'bg-brand-50/40 border-brand-200'
      } ${isUnread && onMarkAsRead ? 'cursor-pointer hover:bg-brand-50/60' : ''}`}
    >
      <div className="flex items-start space-x-3">
        <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs text-white ${isFail ? 'bg-rose-500' : 'bg-brand-600'}`}>
          <i className={`fa-solid ${isFail ? 'fa-circle-exclamation' : 'fa-check'}`}></i>
        </div>
        <div>
          <div className="text-xs font-bold text-slate-800">{message}</div>
          <div className="text-[10px] text-slate-400 mt-1">{notification.createdAt || '방금 전'}</div>
        </div>
      </div>
      <div onClick={(e) => e.stopPropagation()}>
        {/* 👉 읽지 않았으면 [읽음 처리] 버튼, 읽었으면 '읽음' 텍스트 출력 */}
        {!isRead ? actionButton : <span className="text-[10px] text-slate-400 font-semibold">읽음</span>}
      </div>
    </div>
  );
};