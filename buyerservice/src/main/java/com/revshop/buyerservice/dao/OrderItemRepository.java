package com.revshop.buyerservice.dao;

import com.revshop.buyerservice.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    // You can define custom query methods here if needed
}
