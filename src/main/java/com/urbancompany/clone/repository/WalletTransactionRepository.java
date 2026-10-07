package com.urbancompany.clone.repository;

import com.urbancompany.clone.model.WalletTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByProviderIdOrderByCreatedAtDesc(Long providerId);
    List<WalletTransaction> findByProviderIdAndStatusOrderByCreatedAtDesc(Long providerId, WalletTransaction.Status status);
    List<WalletTransaction> findByBookingId(Long bookingId);
}
