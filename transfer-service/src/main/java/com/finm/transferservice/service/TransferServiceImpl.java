package com.finm.transferservice.service;

import com.finm.transferservice.client.AccountServiceClient;
import com.finm.transferservice.domain.TransactionType;
import com.finm.transferservice.domain.Transfer;
import com.finm.transferservice.dto.DepositRequest;
import com.finm.transferservice.dto.TransferRequest;
import com.finm.transferservice.dto.TransferResponse;
import com.finm.transferservice.dto.WithdrawRequest;
import com.finm.transferservice.kafka.TransferProducerService;
import com.finm.transferservice.repository.TransferRepository;
import com.finm.transferservice.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransferServiceImpl implements TransferService {

    private static final String TOPIC_TRANSFER_EVENTS = "transfer-events";

    private final TransferRepository transferRepository;
    private final TransferProducerService transferProducerService;
    private final AccountServiceClient accountServiceClient;

    // 현금 입금 처리
    @Override
    @Transactional
    public TransferResponse deposit(DepositRequest request) {

        // 엔티티 생성 (초기 상태 - REQUESTED)
        Transfer transfer = Transfer.builder()
                .transactionType(TransactionType.DEPOSIT)
                .fromAccount(null)
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .build();

        // account-service 호출하여 입금 계좌 잔액 증가
        try {
            accountServiceClient.depositBalance(request.getToAccount(), request.getAmount());
            // 거래 완료 처리
            transfer.markCompleted();
        } catch (Exception e) {
            log.error("[TransferService] Deposit failed for account: {}", request.getToAccount(), e);
            // 거래 실패 처리
            transfer.markFailed(e.getMessage());
            Transfer savedFailedTransfer = transferRepository.save(transfer);

            // 입금 실패 시에도 실패 이벤트 Kafka 발행
            transferProducerService.send(TOPIC_TRANSFER_EVENTS, TransferResponse.from(savedFailedTransfer));
        }

        // DB 저장
        Transfer savedTransfer = transferRepository.save(transfer);
        TransferResponse response = TransferResponse.from(savedTransfer);

        // Kafka 이벤트 발행 (비동기 알림 및 타 서비스 전파)
        transferProducerService.send(TOPIC_TRANSFER_EVENTS, response);

        log.info("[TransferService] Deposit completed: transferId={}", response.getTransferId());
        return response;
    }

    // 현금 출금 처리
    @Override
    @Transactional
    public TransferResponse withdraw(WithdrawRequest request) {
        // 엔티티 생성
        Transfer transfer = Transfer.builder()
                .transactionType(TransactionType.WITHDRAW)
                .fromAccount(request.getFromAccount())
                .toAccount(null)
                .amount(request.getAmount())
                .build();

        // account-service 호출하여 출금 계좌 잔액 차감
        try {
            accountServiceClient.withdrawBalance(request.getFromAccount(), request.getAmount());
            // 거래 완료 처리
            transfer.markCompleted();
        } catch (Exception e) {
            log.error("[TransferService] Withdraw failed for account: {}", request.getFromAccount(), e);
        // 거래 실패 처리
            transfer.markFailed(e.getMessage());
            Transfer savedFailedTransfer = transferRepository.save(transfer);

            // 출금 실패 시에도 실패 이벤트 Kafka 발행
            transferProducerService.send(TOPIC_TRANSFER_EVENTS, TransferResponse.from(savedFailedTransfer));
            throw new RuntimeException("출금 처리에 실패했습니다: " + e.getMessage(), e);
        }

        // DB 저장
        Transfer savedTransfer = transferRepository.save(transfer);
        TransferResponse response = TransferResponse.from(savedTransfer);

        // Kafka 이벤트 발행
        transferProducerService.send(TOPIC_TRANSFER_EVENTS, response);

        log.info("[TransferService] Withdraw completed: transferId={}", response.getTransferId());
        return response;
    }

    // 계좌 간 이체 처리
    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {

        // 엔티티 생성
        Transfer transfer = Transfer.builder()
                .transactionType(TransactionType.TRANSFER)
                .fromAccount(request.getFromAccount())
                .toAccount(request.getToAccount())
                .amount(request.getAmount())
                .build();

        // 출금 계좌 잔액 차감 시도
        try {
            accountServiceClient.withdrawBalance(request.getFromAccount(), request.getAmount());
        } catch (Exception e) {
            log.error("[TransferService] Withdrawal step failed: fromAccount={}", request.getFromAccount(), e);
        // 출금 실패 처리 및 저장 후 예외 전파
            transfer.markFailed("출금 실패: " + e.getMessage());
            Transfer savedFailedTransfer = transferRepository.save(transfer);

            // 출금 단계 실패 시 실패 이벤트 Kafka 발행
            transferProducerService.send(TOPIC_TRANSFER_EVENTS, TransferResponse.from(savedFailedTransfer));
            throw new RuntimeException("출금 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        // 입금 계좌 잔액 증가 시도 (실패 시 보상 트랜잭션 실행)
        try {
            accountServiceClient.depositBalance(request.getToAccount(), request.getAmount());
            // 거래 완료 처리
            transfer.markCompleted();
        } catch (Exception e) {
            log.error("[TransferService] Deposit step failed, rolling back: toAccount={}", request.getToAccount(), e);

            // 보상 트랜잭션: 출금 계좌로 원금 재입금 (Saga Rollback)
            try {
                accountServiceClient.depositBalance(request.getFromAccount(), request.getAmount());
                transfer.markCompensated();
                transfer.markFailed("입금 실패로 인한 원복 완료: " + e.getMessage());
            } catch (Exception compensationEx) {
                log.error("[TransferService] CRITICAL: Compensation failed! Manual adjustment needed: fromAccount={}",
                        request.getFromAccount(), compensationEx);
                transfer.markFailed("입금 실패 및 보상 트랜잭션 실패 (수동 정산 필요): " + compensationEx.getMessage());
            }
            Transfer savedFailedTransfer = transferRepository.save(transfer);

            // 입금 실패 및 보상 트랜잭션 처리 후 실패 이벤트 Kafka 발행
            transferProducerService.send(TOPIC_TRANSFER_EVENTS, TransferResponse.from(savedFailedTransfer));
            throw new RuntimeException("이체 처리 중 오류가 발생하여 출금 금액이 원복되었습니다: " + e.getMessage(), e);
        }

        // 거래 완료 처리 및 DB 저장
        Transfer savedTransfer = transferRepository.save(transfer);
        TransferResponse response = TransferResponse.from(savedTransfer);

        // Kafka 이벤트 발행
        transferProducerService.send(TOPIC_TRANSFER_EVENTS, response);

        log.info("[TransferService] Transfer completed: transferId={}", response.getTransferId());
        return response;
    }

    // 거래 내역 조회
    @Override
    public List<TransferResponse> getHistories(String accountNumber) {
        List<Transfer> list;
        if (accountNumber != null && !accountNumber.isBlank()) {
            list = transferRepository.findByFromAccountOrToAccountOrderByRequestedAtDesc(accountNumber, accountNumber);
        } else {
            list = transferRepository.findAll();
        }

        return list.stream()
                .map(TransferResponse::from)
                .collect(Collectors.toList());
    }
}