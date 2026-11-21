package com.collegecart.repository;

import com.collegecart.entity.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

    List<OrderStatusHistory> findByVendorOrderIdOrderByChangedAtAscIdAsc(Long vendorOrderId);
}
