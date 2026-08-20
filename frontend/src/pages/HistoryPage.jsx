import React, { useState } from 'react';
import { useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { TransferTableRow, transferApi } from '../entities/TransferEntity';
import { useAuth } from '../entities/UserEntity';
import { notificationApi } from '../entities/NotificationEntity';
import { NotificationListItem } from '../features/NotificationReadFeature';

const renderTransfers = (transfers, isLoading, isError) => {
  if (isLoading) return <tr><td colSpan="7" className="p-6 text-center text-slate-400 text-xs">거래 내역을 불러오는 중...</td></tr>;
  if (isError) return <tr><td colSpan="7" className="p-6 text-center text-rose-500 text-xs">거래 내역을 불러오는 데 실패했습니다.</td></tr>;
  if (transfers.length === 0) return <tr><td colSpan="7" className="p-6 text-center text-slate-400 text-xs">거래 내역이 없습니다.</td></tr>;

  return transfers.map((t, idx) => <TransferTableRow key={t.transferId || idx} transfer={t} />);
};

const renderNotifications = (notifications, isLoading, isError) => {
  if (isLoading) return <div className="p-6 text-center text-slate-400 text-xs">알림을 불러오는 중...</div>;
  if (isError) return <div className="p-6 text-center text-rose-500 text-xs">알림을 불러오는 데 실패했습니다.</div>;
  if (notifications.length === 0) return <div className="p-6 text-center text-slate-400 text-xs">새로운 알림이 없습니다.</div>;

  return notifications.map((n) => <NotificationListItem key={n.id} notification={n} />);
};

export const HistoryPage = () => {
  const location = useLocation();
  const { user } = useAuth();
  const defaultTab = location.state?.defaultTab || 'transfers';
  const [tab, setTab] = useState(defaultTab);

  const { data: transfers = [], isLoading: isLoadingTransfers, isError: isErrorTransfers } = useQuery({
    queryKey: ['transfers', user?.userId],
    queryFn: () => transferApi.getTransfers(user?.userId),
    retry: 1,
    enabled: !!user?.userId,
  });

  const { data: notifications = [], isLoading: isLoadingNotifications, isError: isErrorNotifications } = useQuery({
    queryKey: ['notifications', user?.userId],
    queryFn: () => notificationApi.getNotifications(user?.userId),
    retry: 1,
    enabled: !!user?.userId, 
  });

  // 최신순으로 정렬
  const sortedTransfers = [...transfers].sort((a, b) => new Date(b.requestedAt) - new Date(a.requestedAt));
  const sortedNotifications = [...notifications].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

  // 👉 백엔드 필드명인 read로 수정 (!n.isRead -> !n.read)
  const unreadCount = sortedNotifications.filter((n) => !n.read).length;

  return (
    <div className="fade-in space-y-6 w-full">
      <div className="flex border-b border-slate-200">
        <button onClick={() => setTab('transfers')} className={`pb-3 px-4 text-sm font-bold border-b-2 transition ${tab === 'transfers' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-400'}`}>
          <i className="fa-solid fa-list-check mr-2"></i>이체/입출금 거래 내역
        </button>
        <button onClick={() => setTab('notifications')} className={`pb-3 px-4 text-sm font-bold border-b-2 transition relative ${tab === 'notifications' ? 'border-brand-600 text-brand-600' : 'border-transparent text-slate-400'}`}>
          <i className="fa-solid fa-bell mr-2"></i>실시간 알림 내역
          {unreadCount > 0 && <span className="ml-1.5 px-2 py-0.5 rounded-full text-[10px] bg-rose-100 text-rose-600 font-bold">{unreadCount}</span>}
        </button>
      </div>

      {tab === 'transfers' ? (
        <div className="bg-white border border-slate-200 rounded-2xl overflow-hidden shadow-sm">
          <div className="p-4 bg-slate-50 border-b border-slate-200">
            <span className="text-xs font-bold text-slate-600">거래 이력 원장</span>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-xs">
              <thead>
                <tr className="bg-slate-100/70 text-slate-500 font-semibold border-b border-slate-200">
                  <th className="p-3.5">거래 번호</th>
                  <th className="p-3.5">구분</th>
                  <th className="p-3.5">출금계좌</th>
                  <th className="p-3.5">입금계좌</th>
                  <th className="p-3.5 text-right">금액</th>
                  <th className="p-3.5 text-center">상태</th>
                  <th className="p-3.5 text-right">거래일시</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-slate-700">
                {renderTransfers(sortedTransfers, isLoadingTransfers, isErrorTransfers)}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
          <h3 className="font-bold text-slate-800 text-sm mb-4 border-b border-slate-100 pb-3">실시간 서비스 알림 피드</h3>
          <div className="space-y-3">
            {renderNotifications(sortedNotifications, isLoadingNotifications, isErrorNotifications)}
          </div>
        </div>
      )}
    </div>
  );
};