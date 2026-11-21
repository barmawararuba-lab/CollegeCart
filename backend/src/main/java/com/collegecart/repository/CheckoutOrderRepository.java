package com.collegecart.repository;

import com.collegecart.entity.CheckoutOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckoutOrderRepository extends JpaRepository<CheckoutOrder, Long> {

    List<CheckoutOrder> findByStudentIdOrderByPlacedAtDesc(Long studentId);

    Optional<CheckoutOrder> findByIdAndStudentId(Long id, Long studentId);

    boolean existsByOrderNumber(String orderNumber);
}
