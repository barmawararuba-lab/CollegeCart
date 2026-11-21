package com.collegecart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collegecart.config.SecurityConfig;
import com.collegecart.dto.CategoryResponse;
import com.collegecart.dto.ProductCreateRequest;
import com.collegecart.dto.ProductResponse;
import com.collegecart.dto.ProductUpdateRequest;
import com.collegecart.entity.Category;
import com.collegecart.entity.Product;
import com.collegecart.repository.UserRepository;
import com.collegecart.repository.VendorProfileRepository;
import com.collegecart.security.VendorApprovalChecker;
import com.collegecart.service.CategoryService;
import com.collegecart.service.JwtService;
import com.collegecart.service.ProductService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = {
        CategoryController.class,
        VendorProductController.class,
        ProductController.class
})
@Import(SecurityConfig.class)
class ProductControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private VendorProfileRepository vendorProfileRepository;

    @MockitoBean(name = "vendorApprovalChecker")
    private VendorApprovalChecker vendorApprovalChecker;

    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        when(vendorApprovalChecker.isApproved(any(Authentication.class))).thenReturn(true);
        productResponse = productResponse();
    }

    @Test
    void vendorProductRoutesReturnExpectedStatusesAndResponseFields() throws Exception {
        when(productService.findVendorProducts(any(Authentication.class)))
                .thenReturn(List.of(productResponse));
        when(productService.createProduct(any(ProductCreateRequest.class), any(Authentication.class)))
                .thenReturn(productResponse);
        when(productService.findVendorProduct(eq(7L), any(Authentication.class)))
                .thenReturn(productResponse);
        when(productService.updateProduct(eq(7L), any(ProductUpdateRequest.class), any(Authentication.class)))
                .thenReturn(productResponse);
        doNothing().when(productService).deactivateProduct(eq(7L), any(Authentication.class));

        mockMvc.perform(get("/api/vendor/products").with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].category").value("Books"));

        mockMvc.perform(post("/api/vendor/products")
                        .with(user("vendor@example.edu").roles("VENDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Textbook"))
                .andExpect(jsonPath("$.categoryId").value(4))
                .andExpect(jsonPath("$.image").value("https://example.edu/book.png"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.updatedAt").exists());

        mockMvc.perform(get("/api/vendor/products/7").with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        mockMvc.perform(put("/api/vendor/products/7")
                        .with(user("vendor@example.edu").roles("VENDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Textbook"));

        mockMvc.perform(delete("/api/vendor/products/7")
                        .with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isNoContent());
    }

    @Test
    void invalidCreateAndUpdateBodiesReturnBadRequest() throws Exception {
        String invalidRequest = """
                {"name":"", "description":"", "price":0, "stock":-1, "categoryId":null,
                 "imageUrl":"javascript:alert(1)"}
                """;

        mockMvc.perform(post("/api/vendor/products")
                        .with(user("vendor@example.edu").roles("VENDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/vendor/products/7")
                        .with(user("vendor@example.edu").roles("VENDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedVendorRequestIsRejected() throws Exception {
        mockMvc.perform(get("/api/vendor/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void categoryAndMarketplaceRoutesReturnActiveData() throws Exception {
        Category category = new Category();
        category.setId(4L);
        category.setName("Books");
        category.setActive(true);
        when(categoryService.findActiveCategories()).thenReturn(List.of(new CategoryResponse(category)));
        when(productService.findActiveMarketplaceProducts()).thenReturn(List.of(productResponse));

        mockMvc.perform(get("/api/categories").with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Books"));

        mockMvc.perform(get("/api/products").with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].active").value(true));
    }

    private ProductResponse productResponse() {
        Category category = new Category();
        category.setId(4L);
        category.setName("Books");

        Product product = new Product();
        product.setId(7L);
        product.setName("Textbook");
        product.setDescription("Course textbook");
        product.setPrice(new BigDecimal("12.50"));
        product.setStock(5);
        product.setImageUrl("https://example.edu/book.png");
        product.setActive(true);
        product.setCategory(category);
        ReflectionTestUtils.setField(product, "updatedAt", LocalDateTime.now());
        return new ProductResponse(product);
    }

    private String validRequestJson() {
        return """
                {"name":"Textbook", "description":"Course textbook", "price":12.50,
                 "stock":5, "categoryId":4, "imageUrl":"https://example.edu/book.png"}
                """;
    }
}
