package com.collegecart.repository;

import com.collegecart.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByVendorOrderIdOrderByIdAsc(Long vendorOrderId);
}
