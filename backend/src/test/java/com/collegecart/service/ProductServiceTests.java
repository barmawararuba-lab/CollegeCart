package com.collegecart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private VendorProfileRepository vendorProfileRepository;

    @Mock
    private VendorApprovalChecker vendorApprovalChecker;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProductService productService;

    private User vendorUser;
    private VendorProfile vendorProfile;
    private Category activeCategory;

    @BeforeEach
    void setUp() {
        vendorUser = new User();
        vendorUser.setId(10L);
        vendorUser.setEmail("vendor@example.edu");
        vendorUser.setRole(Role.VENDOR);
        vendorUser.setActive(true);

        vendorProfile = org.mockito.Mockito.mock(VendorProfile.class);

        activeCategory = new Category();
        activeCategory.setId(30L);
        activeCategory.setName("Books");
        activeCategory.setActive(true);
    }

    @Test
    void approvedVendorCanCreateProduct() {
        givenApprovedVendor();
        when(categoryRepository.findById(30L)).thenReturn(Optional.of(activeCategory));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProductResponse response = productService.createProduct(createRequest(), authentication);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        Product savedProduct = productCaptor.getValue();
        assertEquals("Textbook", savedProduct.getName());
        assertEquals(vendorProfile, savedProduct.getVendorProfile());
        assertEquals(activeCategory, savedProduct.getCategory());
        assertEquals("12.50", savedProduct.getPrice().toPlainString());
        assertEquals("Books", response.getCategory());
    }

    @Test
    void unapprovedVendorCannotCreateProduct() {
        givenVendor(false, true);

        assertThrows(AccessDeniedException.class,
                () -> productService.createProduct(createRequest(), authentication));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void inactiveVendorCannotCreateProduct() {
        givenVendor(true, false);

        assertThrows(AccessDeniedException.class,
                () -> productService.createProduct(createRequest(), authentication));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void vendorProductListIsScopedToItsVendorProfile() {
        givenApprovedVendor();
        givenVendorProfileId();
        when(productRepository.findByVendorProfileId(20L))
                .thenReturn(List.of(product(101L, "Own book")));

        List<ProductResponse> products = productService.findVendorProducts(authentication);

        assertEquals(1, products.size());
        assertEquals("Own book", products.get(0).getName());
        verify(productRepository).findByVendorProfileId(20L);
    }

    @Test
    void vendorCannotUpdateAnotherVendorsProduct() {
        givenApprovedVendor();
        givenVendorProfileId();
        when(productRepository.findByIdAndVendorProfileId(999L, 20L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, updateRequest(), authentication));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void vendorCannotDeleteAnotherVendorsProduct() {
        givenApprovedVendor();
        givenVendorProfileId();
        when(productRepository.findByIdAndVendorProfileId(999L, 20L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.deactivateProduct(999L, authentication));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void inactiveCategoryCannotBeAssigned() {
        givenApprovedVendor();
        Category inactiveCategory = new Category();
        inactiveCategory.setId(30L);
        inactiveCategory.setActive(false);
        when(categoryRepository.findById(30L)).thenReturn(Optional.of(inactiveCategory));

        assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(createRequest(), authentication));

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deletingOwnedProductSetsActiveToFalse() {
        givenApprovedVendor();
        givenVendorProfileId();
        Product existingProduct = product(101L, "Own book");
        when(productRepository.findByIdAndVendorProfileId(101L, 20L))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.save(existingProduct)).thenReturn(existingProduct);

        productService.deactivateProduct(101L, authentication);

        assertFalse(existingProduct.isActive());
        verify(productRepository).save(existingProduct);
    }

    @Test
    void returnsActiveMarketplaceProducts() {
        Product marketplaceProduct = product(101L, "Available book");
        when(productRepository.findByActiveTrue()).thenReturn(List.of(marketplaceProduct));

        List<ProductResponse> products = productService.findActiveMarketplaceProducts();

        assertEquals(1, products.size());
        assertEquals("Available book", products.get(0).getName());
        assertTrue(products.get(0).isActive());
        verify(productRepository).findByActiveTrue();
    }

    private void givenApprovedVendor() {
        givenVendor(true, true);
    }

    private void givenVendor(boolean approved, boolean active) {
        vendorUser.setActive(active);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("vendor@example.edu");
        when(userRepository.findByEmail("vendor@example.edu"))
                .thenReturn(Optional.of(vendorUser));
        if (active) {
            when(vendorProfileRepository.findByUserId(10L))
                    .thenReturn(Optional.of(vendorProfile));
            when(vendorApprovalChecker.isApproved(authentication)).thenReturn(approved);
        }
    }

    private void givenVendorProfileId() {
        when(vendorProfile.getId()).thenReturn(20L);
    }

    private ProductCreateRequest createRequest() {
        ProductCreateRequest request = new ProductCreateRequest();
        request.setName("Textbook");
        request.setDescription("Course textbook");
        request.setPrice(new BigDecimal("12.50"));
        request.setStock(4);
        request.setCategoryId(30L);
        request.setImageUrl("https://example.edu/book.png");
        return request;
    }

    private ProductUpdateRequest updateRequest() {
        ProductUpdateRequest request = new ProductUpdateRequest();
        request.setName("Updated textbook");
        request.setDescription("Updated course textbook");
        request.setPrice(new BigDecimal("13.00"));
        request.setStock(3);
        request.setCategoryId(30L);
        return request;
    }

    private Product product(Long id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Product description");
        product.setPrice(new BigDecimal("12.50"));
        product.setStock(5);
        product.setCategory(activeCategory);
        product.setVendorProfile(vendorProfile);
        product.setActive(true);
        return product;
    }
}
