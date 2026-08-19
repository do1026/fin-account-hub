package com.finm.notification.service;

import com.fin.account.event.TransferEvent; // 자동 생성된 Avro 클래스
import com.finm.notification.domain.History;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final HistoryService historyService;
    private final SseEmitterService sseEmitterService;

    @KafkaListener(topics = "transfer-events", groupId = "notification-group")
    public void consumeTransferEvent(ConsumerRecord<String, TransferEvent> record) {
        TransferEvent event = record.value();
        log.info("Received Avro Kafka Event from transfer-events - Key: {}, Event: {}", record.key(), event);

        try {
            // 1. Avro 객체에서 데이터 추출
            // (Avro 스키마 필드명/타입에 맞게 안전하게 변환)
            Long userId = 1L; // 필요 시 event에서 추출

            // fromAccount/toAccount 문자열 처리
            String fromAccountStr = event.getFromAccount() != null ? event.getFromAccount().toString() : "";
            String toAccountStr = event.getToAccount() != null ? event.getToAccount().toString() : "";
            Long accountNumber = !toAccountStr.isEmpty() ? Long.parseLong(toAccountStr.replaceAll("[^0-9]", "")) : 123456789L;

            String transactionType = event.getTransactionType() != null ? event.getTransactionType().toString() : "TRANSFER";
            Long amount = (long) event.getAmount();
            Long balanceAfter = 0L; // 필요 시 추가 필드 연동

            // 2. 이체 성공 / 실패 상태값 확인
            String status = event.getStatus() != null ? event.getStatus().toString() : "SUCCESS";

            // 3. 성공 / 실패에 따른 알림 문구(description) 동적 생성
            String description;
            if ("SUCCESS".equalsIgnoreCase(status)) {
                description = String.format("계좌 이체 성공 (%d원)", amount);
            } else {
                description = String.format("계좌 이체 실패 (이체 번호: %d)", event.getTransferId());
            }

            // 4. DB 내역 저장
            History savedHistory = historyService.saveHistory(
                    userId,
                    accountNumber,
                    transactionType,
                    amount,
                    balanceAfter,
                    description
            );

            // 5. 실시간 SSE 알림 전송
            sseEmitterService.sendNotification(accountNumber, savedHistory);

            log.info("Successfully processed transfer notification [Status: {}] for transferId: {}, account: {}",
                    status, event.getTransferId(), accountNumber);

        } catch (Exception e) {
            log.error("Failed to process Kafka event: {}", event, e);
        }
    }
}