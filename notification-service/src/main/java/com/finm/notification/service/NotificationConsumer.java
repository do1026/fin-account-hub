package com.finm.notification.service;

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
    public void consumeTransferEvent(Object record) {
        log.info("Received Kafka Event from transfer-events: {}", record);

        try {
            // 💡 TODO: 추후 record 객체(DTO 등)에서 실제 이벤트 데이터를 추출하도록 변경하세요.
            Long userId = 1L; // 👈 추가된 userId (임시값 지정 혹은 record에서 추출)
            Long accountNumber = 123456789L;
            String transactionType = "TRANSFER";
            Long amount = 10000L;
            Long balanceAfter = 50000L;
            String description = "계좌 이체 알림";

            // 1. DB 저장 (userId 파라미터 추가)
            History savedHistory = historyService.saveHistory(userId, accountNumber, transactionType, amount, balanceAfter, description);

            // 2. 실시간 SSE 알림 전송
            sseEmitterService.sendNotification(accountNumber, savedHistory);
            log.info("Successfully saved history and sent SSE notification for userId: {}, account: {}", userId, accountNumber);

        } catch (Exception e) {
            log.error("Failed to process Kafka event: {}", record, e);
        }
    }
}