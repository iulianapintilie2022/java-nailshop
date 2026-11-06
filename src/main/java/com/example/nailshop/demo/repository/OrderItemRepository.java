package com.example.nailshop.demo.repository;
import java.util.List;
import com.example.nailshop.demo.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
                List<OrderItem> findByOrderId(Long orderId);
}
