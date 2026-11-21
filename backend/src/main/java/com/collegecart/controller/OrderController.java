package com.collegecart.controller;

import com.collegecart.dto.CheckoutOrderResponse;
import com.collegecart.dto.CheckoutRequest;
import com.collegecart.dto.VendorOrderResponse;
import com.collegecart.dto.VendorOrderStatusUpdateRequest;
import com.collegecart.service.OrderService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/api/orders/checkout")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CheckoutOrderResponse> checkout(
            @Valid @RequestBody CheckoutRequest request,
            Authentication authentication) {
        CheckoutOrderResponse order = orderService.placeOrder(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @GetMapping("/api/orders")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<CheckoutOrderResponse>> getStudentOrders(
            Authentication authentication) {
        return ResponseEntity.ok(orderService.findStudentOrders(authentication));
    }

    @GetMapping("/api/orders/{orderId}")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<CheckoutOrderResponse> getStudentOrder(
            @PathVariable Long orderId,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.findStudentOrder(orderId, authentication));
    }

    @GetMapping("/api/vendor/orders")
    @PreAuthorize("hasRole('VENDOR') and @vendorApprovalChecker.isApproved(authentication)")
    public ResponseEntity<List<VendorOrderResponse>> getVendorOrders(
            Authentication authentication) {
        return ResponseEntity.ok(orderService.findVendorOrders(authentication));
    }

    @GetMapping("/api/vendor/orders/{vendorOrderId}")
    @PreAuthorize("hasRole('VENDOR') and @vendorApprovalChecker.isApproved(authentication)")
    public ResponseEntity<VendorOrderResponse> getVendorOrder(
            @PathVariable Long vendorOrderId,
            Authentication authentication) {
        return ResponseEntity.ok(orderService.findVendorOrder(vendorOrderId, authentication));
    }

    @PatchMapping("/api/vendor/orders/{vendorOrderId}/status")
    @PreAuthorize("hasRole('VENDOR') and @vendorApprovalChecker.isApproved(authentication)")
    public ResponseEntity<VendorOrderResponse> updateVendorOrderStatus(
            @PathVariable Long vendorOrderId,
            @Valid @RequestBody VendorOrderStatusUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(
                orderService.updateVendorOrderStatus(vendorOrderId, request, authentication));
    }
}
