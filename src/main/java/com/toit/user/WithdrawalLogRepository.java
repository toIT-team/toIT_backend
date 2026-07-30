package com.toit.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WithdrawalLogRepository extends JpaRepository<WithdrawalLog, Long> {
}
