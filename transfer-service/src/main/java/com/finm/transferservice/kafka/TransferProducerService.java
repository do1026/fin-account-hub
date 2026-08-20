package com.finm.transferservice.kafka;

import com.finm.avro.TransferEventAvro;
import com.finm.transferservice.domain.Transfer;
import com.finm.transferservice.dto.TransferResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class TransferProducerService {

    private final KafkaTemplate<String, TransferEventAvro> kafkaTemplate;

    // 이체 완료(TransferResponse 기반) 이벤트 발행
    public TransferResponse send(String topic, TransferResponse responseDto) {
        TransferEventAvro event = TransferEventAvro.newBuilder()
                .setTransferId(responseDto.getTransferId())
                .setTransactionType(responseDto.getTransactionType() != null ? responseDto.getTransactionType().name() : "TRANSFER")
                .setFromAccount(cleanAccount(responseDto.getFromAccount()))
                .setToAccount(cleanAccount(responseDto.getToAccount()))
                .setAmount(responseDto.getAmount())
                .setStatus(responseDto.getStatus() != null ? responseDto.getStatus().name() : "COMPLETED")
                .build();

        sendEvent(topic, responseDto.getTransferId(), event);
        return responseDto;
    }

    // 이체 도메인 엔티티(Transfer) 기반 Avro 이벤트 전송
    public void sendTransferEvent(String topic, Transfer transfer) {
        TransferEventAvro event = TransferEventAvro.newBuilder()
                .setTransferId(transfer.getTransferId())
                .setTransactionType(transfer.getTransactionType() != null ? transfer.getTransactionType().name() : "TRANSFER")
                .setFromAccount(cleanAccount(transfer.getFromAccount()))
                .setToAccount(cleanAccount(transfer.getToAccount()))
                .setAmount(transfer.getAmount())
                .setStatus(transfer.getStatus() != null ? transfer.getStatus().name() : "COMPLETED")
                .build();

        sendEvent(topic, transfer.getTransferId(), event);
    }

    // 공통 비동기 전송 및 콜백 처리
    private void sendEvent(String topic, String key, TransferEventAvro event) {
        try {
            kafkaTemplate.send(topic, key, event).whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("[Avro Producer] Failed to send event to topic '{}': transferId={}", topic, key, ex);
                } else {
                    log.info("[Avro Producer] Sent event to topic '{}': transferId={}, offset={}",
                            topic, key, result.getRecordMetadata().offset());
                }
            });
        } catch (Exception e) {
            log.error("[Avro Producer] Immediate failure submitting to Kafka template: transferId={}", key, e);
        }
    }

    // 계좌번호 정제 헬퍼 메서드
    private String cleanAccount(String accountNumber) {
        return (accountNumber != null) ? accountNumber.replaceAll("[^0-9]", "") : "";
    }
}