package com.finm.transferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequest {
    private Long userId;
    private String fromAccount;
    private String toAccount;
    private Long amount;
}