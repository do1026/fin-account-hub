package com.finm.transferservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.finm.transferservice.domain.TransactionType;
import com.finm.transferservice.domain.Transfer;
import com.finm.transferservice.domain.TransferStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private String transferId;

    private TransactionType transactionType;
    private String fromAccount;
    private String toAccount;
    private Long amount;
    private TransferStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime completedAt;

    public static TransferResponse from(Transfer transfer) {
        return TransferResponse.builder()
                .transferId(transfer.getTransferId())
                .transactionType(transfer.getTransactionType())
                .fromAccount(transfer.getFromAccount())
                .toAccount(transfer.getToAccount())
                .amount(transfer.getAmount())
                .status(transfer.getStatus())
                .requestedAt(transfer.getRequestedAt())
                .completedAt(transfer.getCompletedAt())
                .build();
    }
}