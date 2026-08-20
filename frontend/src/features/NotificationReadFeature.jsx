import React from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { NotificationItem, notificationApi } from '../entities/NotificationEntity.jsx';
import { showToast } from '../shared/Toast';

export const useMarkNotificationRead = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: notificationApi.markAsRead,
    onSuccess: () => {
      showToast.success('알림이 읽음 처리 되었습니다.');
      queryClient.invalidateQueries({ queryKey: ['notifications'] });
    },
    onError: (err) => {
      showToast.error(err.response?.data?.message || '알림 처리에 실패했습니다.');
    },
  });
};

export const NotificationReadButton = ({ notificationId, markAsRead, isLoading }) => {
  const handleRead = (e) => {
    e.stopPropagation();
    markAsRead(notificationId);
  };

  return (
    <button onClick={handleRead} disabled={isLoading} className="text-[11px] font-semibold text-brand-600 hover:text-brand-800 bg-white border border-brand-200 px-2.5 py-1 rounded-lg shadow-sm transition disabled:opacity-50">
      {isLoading ? '처리중...' : '읽음 처리'}
    </button>
  );
};

export const NotificationListItem = ({ notification }) => {
  const { mutate: markAsRead, isLoading } = useMarkNotificationRead();

  const handleMarkAsRead = () => {
    // 👉 notificationId 대신 백엔드의 id 사용
    if (!notification.read && !isLoading) {
      markAsRead(notification.id);
    }
  };

  return (
    <NotificationItem
      notification={notification}
      onMarkAsRead={handleMarkAsRead}
      actionButton={
        <NotificationReadButton
          // 👉 notificationId 대신 id 전달
          notificationId={notification.id}
          markAsRead={markAsRead}
          isLoading={isLoading}
        />
      }
    />
  );
};