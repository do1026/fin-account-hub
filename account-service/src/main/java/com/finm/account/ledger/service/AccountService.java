package com.finm.account.ledger.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finm.account.ledger.dto.AccountCreateRequestDto;
import com.finm.account.ledger.dto.AccountResponseDto;
import com.finm.account.ledger.entity.AccountEntity;
import com.finm.account.ledger.entity.AccountStatus;
import com.finm.account.ledger.repository.AccountRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * 신규 계좌 개설 및 시드 잔액 설정
     */
    @Transactional
    public AccountResponseDto createAccount(AccountCreateRequestDto requestDto) {
        // 유니크한 계좌번호 생성 (형식: 110-XXX-XXXXXX)
        String accountNumber = generateUniqueAccountNumber();

        // 시드 잔액 설정 (기본값 0)
        Long initialBalance = requestDto.getBalance() != null ? requestDto.getBalance() : 0L;

        AccountEntity accountEntity = AccountEntity.builder()
                .accountNumber(accountNumber)
                .userId(requestDto.getUserId())
                .balance(initialBalance)
                .accountStatus(AccountStatus.ACTIVE) // Enum 타입 적용
                .build();

        AccountEntity savedAccount = accountRepository.save(accountEntity);

        return AccountResponseDto.fromEntity(savedAccount);
    }

    /**
     * 본인 소유의 ACTIVE 상태 계좌 목록 조회
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAccountsByUserId(Long userId) {
        // Enum 타입으로 조회
        List<AccountEntity> accounts = accountRepository.findByUserIdAndAccountStatus(userId, AccountStatus.ACTIVE);

        return accounts.stream()
                .map(AccountResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 계좌 해지 (Soft Delete: 상태를 CLOSED로 변경)
     */
    @Transactional
    public void closeAccount(Long accountId) {
        AccountEntity account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌를 찾을 수 없습니다. ID: " + accountId));

        if (account.getAccountStatus() == AccountStatus.CLOSED) {
            throw new IllegalStateException("이미 해지된 계좌입니다.");
        }

        // 엔티티 내 정의된 close() 메서드 호출
        account.close();
    }

    /**
     * 유니크 난수 계좌번호 생성 (110-XXX-XXXXXX)
     */
    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String accountNumber;
        do {
            int middle = 100 + random.nextInt(900); // 3자리
            int last = 100000 + random.nextInt(900000); // 6자리
            accountNumber = String.format("110-%03d-%06d", middle, last);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return accountNumber;
    }

    @Transactional
    public void deposit(String accountNumber, Long amount) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));

        account.deposit(amount);
    }

    @Transactional
    public void withdraw(String accountNumber, Long amount) {
        AccountEntity account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountNumber));

        account.withdraw(amount);
    }

    public Long getUserIdByAccountNumber(String accountNumber) {
        // 하이픈이 있든 없든 무시하고 찾아주는 쿼리 호출
        AccountEntity account = accountRepository.findByAccountNumberIgnoreHyphen(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("해당 계좌가 존재하지 않습니다: " + accountNumber));

        return account.getUserId();
    }
}