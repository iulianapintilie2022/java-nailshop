package com.example.nailshop.demo.repository;

import com.example.nailshop.demo.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.List;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

Optional<CustomerOrder> findByStripeSessionId(String stripeSessionId);
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT o FROM CustomerOrder o WHERE o.stripeSessionId = :sessionId")

Optional<CustomerOrder> findWithLockByStripeSessionId(
        @Param("sessionId") String sessionId
        
);

Optional<CustomerOrder>
    findFirstByCustomerIdOrderByOrderDateDescIdDesc(Long customerId);

List<CustomerOrder>
    findByCustomerIdOrderByOrderDateDescIdDesc(Long customerId);
}