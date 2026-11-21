package com.collegecart.controller;

import com.collegecart.dto.ProductCreateRequest;
import com.collegecart.dto.ProductResponse;
import com.collegecart.dto.ProductUpdateRequest;
import com.collegecart.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendor/products")
@PreAuthorize("@vendorApprovalChecker.isApproved(authentication)")
public class VendorProductController {

    private final ProductService productService;

    public VendorProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts(
            Authentication authentication) {
        return ResponseEntity.ok(productService.findVendorProducts(authentication));
    }

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody ProductCreateRequest request,
            Authentication authentication) {
        ProductResponse product = productService.createProduct(request, authentication);
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(
            @PathVariable Long id,
            Authentication authentication) {
        return ResponseEntity.ok(productService.findVendorProduct(id, authentication));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(productService.updateProduct(id, request, authentication));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivateProduct(
            @PathVariable Long id,
            Authentication authentication) {
        productService.deactivateProduct(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
