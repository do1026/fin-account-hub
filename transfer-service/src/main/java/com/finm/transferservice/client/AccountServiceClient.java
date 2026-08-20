package com.finm.transferservice.client;

import com.finm.transferservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "account-service", configuration = FeignConfig.class)
public interface AccountServiceClient {

    // 입금 처리 (잔액 증가)
    @PostMapping("/api/accounts/{accountNumber}/deposit")
    void depositBalance(@PathVariable("accountNumber") String accountNumber, @RequestParam("amount") Long amount);

    // 출금 처리 (잔액 차감 및 잔액 부족 시 예외 발생)
    @PostMapping("/api/accounts/{accountNumber}/withdraw")
    void withdrawBalance(@PathVariable("accountNumber") String accountNumber, @RequestParam("amount") Long amount);
}
