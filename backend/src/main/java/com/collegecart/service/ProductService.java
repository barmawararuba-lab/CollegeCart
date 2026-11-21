package com.collegecart.service;

import com.collegecart.dto.ProductCreateRequest;
import com.collegecart.dto.ProductResponse;
import com.collegecart.dto.ProductUpdateRequest;
import com.collegecart.entity.Category;
import com.collegecart.entity.Product;
import com.collegecart.entity.User;
import com.collegecart.entity.VendorProfile;
import com.collegecart.enums.Role;
import com.collegecart.exception.ResourceNotFoundException;
import com.collegecart.repository.CategoryRepository;
import com.collegecart.repository.ProductRepository;
import com.collegecart.repository.UserRepository;
import com.collegecart.repository.VendorProfileRepository;
import com.collegecart.security.VendorApprovalChecker;
import java.util.List;
import java.util.Locale;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final VendorApprovalChecker vendorApprovalChecker;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository,
            UserRepository userRepository,
            VendorProfileRepository vendorProfileRepository,
            VendorApprovalChecker vendorApprovalChecker) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.vendorProfileRepository = vendorProfileRepository;
        this.vendorApprovalChecker = vendorApprovalChecker;
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findVendorProducts(Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        return productRepository.findByVendorProfileId(vendorProfile.getId())
                .stream()
                .map(ProductResponse::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponse findVendorProduct(Long productId, Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        return findOwnedProduct(productId, vendorProfile.getId())
                .map(ProductResponse::new)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
    }

    @Transactional
    public ProductResponse createProduct(
            ProductCreateRequest request,
            Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        Category category = findActiveCategory(request.getCategoryId());

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);
        product.setVendorProfile(vendorProfile);

        return new ProductResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(
            Long productId,
            ProductUpdateRequest request,
            Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        Product product = findOwnedProduct(productId, vendorProfile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        Category category = findActiveCategory(request.getCategoryId());

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(category);

        return new ProductResponse(productRepository.save(product));
    }

    @Transactional
    public void deactivateProduct(Long productId, Authentication authentication) {
        VendorProfile vendorProfile = resolveApprovedVendor(authentication);
        Product product = findOwnedProduct(productId, vendorProfile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> findActiveMarketplaceProducts() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(ProductResponse::new)
                .toList();
    }

    private VendorProfile resolveApprovedVendor(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Authentication is required");
        }

        String email = authentication.getName().trim().toLowerCase(Locale.ROOT);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.isActive()) {
            throw new AccessDeniedException("Inactive accounts cannot manage products");
        }
        if (user.getRole() != Role.VENDOR) {
            throw new AccessDeniedException("Only vendors can manage vendor products");
        }

        VendorProfile vendorProfile = vendorProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Vendor profile not found"));

        if (!vendorApprovalChecker.isApproved(authentication)) {
            throw new AccessDeniedException("Vendor approval is required to manage products");
        }

        return vendorProfile;
    }

    private Category findActiveCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (!category.isActive()) {
            throw new IllegalArgumentException("Inactive categories cannot be assigned to products");
        }
        return category;
    }

    private java.util.Optional<Product> findOwnedProduct(Long productId, Long vendorProfileId) {
        return productRepository.findByIdAndVendorProfileId(productId, vendorProfileId);
    }
}
