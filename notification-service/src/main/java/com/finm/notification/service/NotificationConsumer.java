package com.finm.notification.service;

import com.finm.avro.TransferEventAvro;
import com.finm.notification.domain.History;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final HistoryService historyService;
    private final SseEmitterService sseEmitterService;

    @KafkaListener(topics = "transfer-events", groupId = "notification-group")
    public void consumeTransferEvent(TransferEventAvro event) {
        log.info("[Avro 수신 성공] transferId={}, from={}, to={}, amount={}, status={}",
                event.getTransferId(), event.getFromAccount(), event.getToAccount(), event.getAmount(), event.getStatus());

        try {
            // 1. Avro 필드 추출
            String fromAccount = event.getFromAccount() != null ? event.getFromAccount().toString() : "";
            String transactionType = event.getTransactionType() != null ? event.getTransactionType().toString() : "TRANSFER";
            Long amount = event.getAmount();
            String status = event.getStatus() != null ? event.getStatus().toString() : "SUCCESS";

            // 계좌번호 숫자 추출 (하이픈 제거)
            Long accountNumber = !fromAccount.isEmpty()
                    ? Long.parseLong(fromAccount.replaceAll("[^0-9]", ""))
                    : 123456789L;
            Long userId = 1L; // 기본 사용자 ID (필요시 토큰 또는 계좌 서비스 매핑)

            // 2. 알림 메시지 생성
            String description = "SUCCESS".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status)
                    ? String.format("계좌 이체 성공 (%d원)", amount)
                    : "계좌 이체 실패";

            // 3. DB 내역 저장
            History savedHistory = historyService.saveHistory(
                    userId,
                    accountNumber,
                    transactionType,
                    amount,
                    0L,
                    description
            );

            // 4. 실시간 SSE 알림 발송
            sseEmitterService.sendNotification(accountNumber, savedHistory);
            log.info("알림 처리 및 발송 완료: account={}, amount={}", accountNumber, amount);

        } catch (Exception e) {
            log.error("Avro 이벤트 처리 중 예외 발생: {}", event, e);
        }
    }
}