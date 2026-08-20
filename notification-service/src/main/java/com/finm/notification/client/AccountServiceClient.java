package com.finm.notification.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-service")
public interface AccountServiceClient {
    @GetMapping("/api/accounts/owner/{accountNumber}")
    Long getUserIdByAccountNumber(@PathVariable("accountNumber") String accountNumber);
}