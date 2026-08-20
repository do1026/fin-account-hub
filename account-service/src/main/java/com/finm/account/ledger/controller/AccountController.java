package com.finm.account.ledger.controller;

import com.finm.account.config.ApiResponse;
import com.finm.account.ledger.dto.AccountCreateRequestDto;
import com.finm.account.ledger.dto.AccountResponseDto;
import com.finm.account.ledger.service.AccountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Account", description = "계좌 API (JWT 인증 필요)")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "신규 계좌 개설", description = "새로운 계좌를 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponseDto>> createAccount(@Valid @RequestBody AccountCreateRequestDto request) {
        AccountResponseDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @Operation(summary = "보유 계좌 목록 조회", description = "특정 사용자의 활성 계좌 목록을 조회합니다.")
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<AccountResponseDto>>> getAccountsByUserId(
            @Parameter(description = "사용자 ID", required = true) @PathVariable Long userId) {
        List<AccountResponseDto> response = accountService.getAccountsByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "계좌 해지", description = "계좌 상태를 'CLOSED'로 변경합니다 (Soft Delete).")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<ApiResponse<Void>> closeAccount(
            @Parameter(description = "계좌 ID", required = true) @PathVariable Long accountId) {
        accountService.closeAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success("계좌가 성공적으로 해지되었습니다."));
    }

    @Operation(summary = "계좌 입금/잔액 증가", description = "이체 서비스(내부 통신)에서 호출하는 입금 API")
    @PostMapping("/{accountNumber}/deposit")
    public ResponseEntity<ApiResponse<Void>> deposit(
            @PathVariable String accountNumber,
            @RequestParam Long amount) {
        accountService.deposit(accountNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("입금이 완료되었습니다."));
    }

    @Operation(summary = "계좌 출금/잔액 차감", description = "이체 서비스(내부 통신)에서 호출하는 출금 API")
    @PostMapping("/{accountNumber}/withdraw")
    public ResponseEntity<ApiResponse<Void>> withdraw(
            @PathVariable String accountNumber,
            @RequestParam Long amount) {
        accountService.withdraw(accountNumber, amount);
        return ResponseEntity.ok(ApiResponse.success("출금이 완료되었습니다."));
    }

    @Operation(summary = "계좌번호로 소유자 ID 조회", description = "계좌번호를 기반으로 소유자의 사용자 ID를 조회합니다.")
    @GetMapping("/owner/{accountNumber}")
    public Long getUserIdByAccountNumber(
            @Parameter(description = "계좌 번호", required = true) @PathVariable String accountNumber) {
        // ApiResponse로 감싸지 않고 Long 값을 바로 리턴
        return accountService.getUserIdByAccountNumber(accountNumber);
    }
}
