package com.finm.account.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.finm.account.ledger.entity.AccountEntity;
import com.finm.account.ledger.entity.AccountStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<AccountEntity, Long> {

    // 두 번째 매개변수 타입을 String -> AccountStatus로 변경
    List<AccountEntity> findByUserIdAndAccountStatus(Long userId, AccountStatus accountStatus);

    // 계좌번호 존재 여부 확인 (중복 계좌번호 생성 방지용)
    boolean existsByAccountNumber(String accountNumber);

    // 계좌 ID와 유저 ID로 특정 계좌 조회
    Optional<AccountEntity> findByAccountIdAndUserId(Long accountId, Long userId);

    // 계좌번호로 계좌 정보 단건 조회 (입/출금 처리용)
    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    // 하이픈 유무에 상관없이 숫자만 일치하면 조회하는 쿼리
    @Query("SELECT a FROM AccountEntity a WHERE REPLACE(a.accountNumber, '-', '') = REPLACE(:accountNumber, '-', '')")
    Optional<AccountEntity> findByAccountNumberIgnoreHyphen(@Param("accountNumber") String accountNumber);
}