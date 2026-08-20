import React, { useState } from 'react';
import { useMutation, useQueryClient } from '@tanstack/react-query';
import { transferApi } from '../entities/TransferEntity';
import { useAuth } from '../entities/UserEntity';
import { showToast } from '../shared/Toast';

export const TransferExecuteForm = ({ accounts }) => {
  const [fromAccount, setFromAccount] = useState('');
  const [toAccount, setToAccount] = useState('');
  const [amount, setAmount] = useState('');
  const queryClient = useQueryClient();
  const { user } = useAuth();

  const { mutate: executeTransaction, isLoading } = useMutation({
    mutationFn: transferApi.transfer,
    onSuccess: () => {
      showToast.success('계좌 이체가 성공적으로 처리되었습니다.');
      
      // 계좌 및 거래내역 갱신
      queryClient.invalidateQueries({ queryKey: ['accounts'] });
      queryClient.invalidateQueries({ queryKey: ['transfers'] });

      // 👉 백엔드가 카프카를 통해 알림을 DB에 적재할 시간을 0.6초 정도 준 뒤
      // 헤더와 알림 관련 모든 쿼리를 강제로 재조회(Refetch)시킵니다.
      setTimeout(() => {
        queryClient.refetchQueries({ queryKey: ['notifications'] });
        if (user?.userId) {
          queryClient.refetchQueries({ queryKey: ['notifications', user.userId] });
        }
      }, 600);

      // 폼 초기화
      setFromAccount('');
      setToAccount('');
      setAmount('');
    },
    onError: (err) => {
      showToast.error(err.response?.data?.message || '이체 처리 중 오류가 발생했습니다.');
    },
  });

  const handleSubmit = (e) => {
    e.preventDefault();
    const variables = { fromAccount, toAccount, amount: Number(amount) };
    executeTransaction(variables);
  };

  return (
    <div className="max-w-2xl mx-auto bg-white border border-slate-200 rounded-2xl p-6 shadow-sm">
      <div className="border-b border-slate-200 pb-4 mb-6">
        <h3 className="font-bold text-slate-800">계좌 이체</h3>
        <p className="text-xs text-slate-400 mt-0.5">계좌로 금액을 이체합니다.</p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">출금 계좌선택</label>
          <select value={fromAccount} onChange={(e) => setFromAccount(e.target.value)} required className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none">
            <option value="">계좌를 선택하세요</option>
            {accounts.map((a) => (
              <option key={a.accountId} value={a.accountNumber}>{a.accountNumber} (잔액: {a.balance?.toLocaleString()}원)</option>
            ))}
          </select>
        </div>
        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">입금 대상 계좌번호</label>
          <input type="text" required placeholder="예: 110-001-2222" value={toAccount} onChange={(e) => setToAccount(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none" />
        </div>

        <div>
          <label className="block text-xs font-semibold text-slate-600 mb-1">금액 (원)</label>
          <input type="number" min="100" required placeholder="5000" value={amount} onChange={(e) => setAmount(e.target.value)} className="w-full px-4 py-2.5 rounded-xl border border-slate-200 text-sm focus:outline-none" />
        </div>

        <button type="submit" disabled={isLoading} className="w-full bg-brand-600 hover:bg-brand-700 text-white font-semibold py-3 rounded-xl shadow-md text-sm transition disabled:opacity-50">
          {isLoading ? '처리 중...' : '이체 실행'}
        </button>
      </form>
    </div>
  );
};