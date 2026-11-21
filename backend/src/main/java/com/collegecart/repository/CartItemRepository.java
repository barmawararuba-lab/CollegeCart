package com.collegecart.repository;

import com.collegecart.entity.CartItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartIdOrderByIdAsc(Long cartId);

    Optional<CartItem> findByIdAndCartId(Long id, Long cartId);

    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);
}
