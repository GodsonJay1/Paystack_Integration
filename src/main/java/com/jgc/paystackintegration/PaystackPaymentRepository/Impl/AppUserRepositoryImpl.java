package com.jgc.paystackintegration.PaystackPaymentRepository.Impl;

import com.jgc.paystackintegration.model.domain.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepositoryImpl extends JpaRepository<AppUser, Long> {
}
