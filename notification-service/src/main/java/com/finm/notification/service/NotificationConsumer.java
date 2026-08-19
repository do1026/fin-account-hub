package com.finm.notification.service;

import com.finm.notification.domain.History;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

    private final HistoryService historyService;
    private final SseEmitterService sseEmitterService;

    @KafkaListener(topics = "transfer-events", groupId = "notification-group")
    public void consumeTransferEvent(Map<String, Object> event) {
        // 💡 카프카 메시지(Jackson 역직렬화 설정 기준 Map 또는 DTO 객체)를 수신합니다.
        log.info("Received Kafka Event from transfer-events: {}", event);

        try {
            // 1. Kafka 메시지 객체에서 데이터 추출 (이체 서비스에서 전달하는 키값에 맞게 조정하세요)
            Long userId = event.get("userId") != null ? ((Number) event.get("userId")).longValue() : 1L;
            Long accountNumber = event.get("accountNumber") != null ? ((Number) event.get("accountNumber")).longValue() : 123456789L;
            String transactionType = event.get("transactionType") != null ? (String) event.get("transactionType") : "TRANSFER";
            Long amount = event.get("amount") != null ? ((Number) event.get("amount")).longValue() : 0L;
            Long balanceAfter = event.get("balanceAfter") != null ? ((Number) event.get("balanceAfter")).longValue() : 0L;

            // 2. 이체 성공 / 실패 상태값 확인 (예: SUCCESS, FAIL 등)
            String status = event.get("status") != null ? (String) event.get("status") : "SUCCESS";

            // 3. 성공 / 실패에 따른 알림 문구(description) 동적 생성
            String description;
            if ("SUCCESS".equalsIgnoreCase(status)) {
                description = String.format("계좌 이체 성공 (%d원)", amount);
            } else {
                String failureReason = event.get("errorMessage") != null ? (String) event.get("errorMessage") : "알 수 없는 오류";
                description = String.format("계좌 이체 실패 (%s)", failureReason);
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

            // 5. 실시간 SSE 알림 전송 (성공/실패 여부와 관계없이 모두 알림 전송)
            sseEmitterService.sendNotification(accountNumber, savedHistory);

            log.info("Successfully processed transfer notification [Status: {}] for userId: {}, account: {}",
                    status, userId, accountNumber);

        } catch (Exception e) {
            log.error("Failed to process Kafka event: {}", event, e);
        }
    }
}