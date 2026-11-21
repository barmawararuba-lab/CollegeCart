package com.collegecart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collegecart.config.SecurityConfig;
import com.collegecart.dto.CheckoutOrderResponse;
import com.collegecart.dto.CheckoutRequest;
import com.collegecart.dto.OrderItemResponse;
import com.collegecart.dto.OrderStatusHistoryResponse;
import com.collegecart.dto.VendorOrderResponse;
import com.collegecart.dto.VendorOrderStatusUpdateRequest;
import com.collegecart.entity.CheckoutOrder;
import com.collegecart.entity.OrderItem;
import com.collegecart.entity.OrderStatusHistory;
import com.collegecart.entity.User;
import com.collegecart.entity.VendorOrder;
import com.collegecart.entity.VendorProfile;
import com.collegecart.enums.OrderStatus;
import com.collegecart.enums.PaymentMethod;
import com.collegecart.enums.Role;
import com.collegecart.repository.UserRepository;
import com.collegecart.security.VendorApprovalChecker;
import com.collegecart.service.JwtService;
import com.collegecart.service.OrderService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

@WebMvcTest(OrderController.class)
@Import(SecurityConfig.class)
class OrderControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean(name = "vendorApprovalChecker")
    private VendorApprovalChecker vendorApprovalChecker;

    private CheckoutOrderResponse checkoutResponse;
    private VendorOrderResponse vendorResponse;

    @BeforeEach
    void setUp() {
        when(vendorApprovalChecker.isApproved(any(Authentication.class))).thenReturn(true);
        checkoutResponse = checkoutResponse();
        vendorResponse = checkoutResponse.getVendorOrders().get(0);
    }

    @Test
    void checkoutEndpointReturnsCreatedAndOrderSnapshotFields() throws Exception {
        when(orderService.placeOrder(any(CheckoutRequest.class), any(Authentication.class)))
                .thenReturn(checkoutResponse);

        mockMvc.perform(post("/api/orders/checkout")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"pickupNote":"Student Centre", "paymentMethod":"CASH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(70))
                .andExpect(jsonPath("$.orderNumber").value("CC-TEST-70"))
                .andExpect(jsonPath("$.total").value(8.5))
                .andExpect(jsonPath("$.paymentMethod").value("CASH"))
                .andExpect(jsonPath("$.vendorOrders[0].vendorShopName").value("Campus Books"))
                .andExpect(jsonPath("$.vendorOrders[0].items[0].productName").value("Notebook"))
                .andExpect(jsonPath("$.vendorOrders[0].items[0].unitPrice").value(4.25))
                .andExpect(jsonPath("$.vendorOrders[0].items[0].subtotal").value(8.5))
                .andExpect(jsonPath("$.vendorOrders[0].statusHistory[0].newStatus").value("PENDING"));
    }

    @Test
    void studentOrderHistoryEndpointReturnsList() throws Exception {
        when(orderService.findStudentOrders(any(Authentication.class)))
                .thenReturn(List.of(checkoutResponse));

        mockMvc.perform(get("/api/orders")
                        .with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(70))
                .andExpect(jsonPath("$[0].vendorOrders[0].items[0].quantity").value(2));
    }

    @Test
    void studentOrderDetailEndpointReturnsCheckout() throws Exception {
        when(orderService.findStudentOrder(eq(70L), any(Authentication.class)))
                .thenReturn(checkoutResponse);

        mockMvc.perform(get("/api/orders/70")
                        .with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("CC-TEST-70"))
                .andExpect(jsonPath("$.vendorOrders[0].status").value("PENDING"));
    }

    @Test
    void vendorOrderListEndpointReturnsList() throws Exception {
        when(orderService.findVendorOrders(any(Authentication.class)))
                .thenReturn(List.of(vendorResponse));

        mockMvc.perform(get("/api/vendor/orders")
                        .with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(80))
                .andExpect(jsonPath("$[0].vendorShopName").value("Campus Books"))
                .andExpect(jsonPath("$[0].items[0].productName").value("Notebook"));
    }

    @Test
    void vendorOrderDetailEndpointReturnsOwnedOrder() throws Exception {
        when(orderService.findVendorOrder(eq(80L), any(Authentication.class)))
                .thenReturn(vendorResponse);

        mockMvc.perform(get("/api/vendor/orders/80")
                        .with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(80))
                .andExpect(jsonPath("$.total").value(8.5));
    }

    @Test
    void vendorStatusEndpointReturnsUpdatedOrder() throws Exception {
        ReflectionTestUtils.setField(vendorResponse, "status", OrderStatus.ACCEPTED);
        when(orderService.updateVendorOrderStatus(
                eq(80L), any(VendorOrderStatusUpdateRequest.class), any(Authentication.class)))
                .thenReturn(vendorResponse);

        mockMvc.perform(patch("/api/vendor/orders/80/status")
                        .with(user("vendor@example.edu").roles("VENDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"ACCEPTED", "note":"Accepted for pickup"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"))
                .andExpect(jsonPath("$.statusHistory[0].newStatus").value("PENDING"));
        verify(orderService).updateVendorOrderStatus(
                eq(80L), any(VendorOrderStatusUpdateRequest.class), any(Authentication.class));
    }

    @Test
    void checkoutAndStatusValidationReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/orders/checkout")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pickupNote\":\"missing payment\"}"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(patch("/api/vendor/orders/80/status")
                        .with(user("vendor@example.edu").roles("VENDOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":\"missing status\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void studentAndVendorRoleRestrictionsAreEnforced() throws Exception {
        mockMvc.perform(get("/api/orders")
                        .with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/vendor/orders")
                        .with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isForbidden());
    }

    @Test
    void vendorApprovalIsRequired() throws Exception {
        when(vendorApprovalChecker.isApproved(any(Authentication.class))).thenReturn(false);

        mockMvc.perform(get("/api/vendor/orders")
                        .with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedOrderRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/vendor/orders")).andExpect(status().isUnauthorized());
    }

    private CheckoutOrderResponse checkoutResponse() {
        User student = new User();
        student.setId(10L);
        student.setFullName("Test Student");
        student.setEmail("student@example.edu");
        student.setRole(Role.STUDENT);
        student.setActive(true);

        VendorProfile vendorProfile = new VendorProfile();
        ReflectionTestUtils.setField(vendorProfile, "id", 40L);
        vendorProfile.setShopName("Campus Books");
        vendorProfile.setShopAddress("Student Centre");

        CheckoutOrder checkoutOrder = new CheckoutOrder();
        checkoutOrder.setId(70L);
        checkoutOrder.setStudent(student);
        checkoutOrder.setOrderNumber("CC-TEST-70");
        checkoutOrder.setPlacedAt(LocalDateTime.now());
        checkoutOrder.setTotal(new BigDecimal("8.50"));
        checkoutOrder.setPickupNote("Student Centre");
        checkoutOrder.setPaymentMethod(PaymentMethod.CASH);

        VendorOrder vendorOrder = new VendorOrder();
        vendorOrder.setId(80L);
        vendorOrder.setCheckoutOrder(checkoutOrder);
        vendorOrder.setVendorProfile(vendorProfile);
        vendorOrder.setTotal(new BigDecimal("8.50"));
        vendorOrder.setStatus(OrderStatus.PENDING);
        ReflectionTestUtils.setField(vendorOrder, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(vendorOrder, "updatedAt", LocalDateTime.now());

        OrderItem orderItem = new OrderItem();
        orderItem.setId(90L);
        orderItem.setVendorOrder(vendorOrder);
        orderItem.setProductNameSnapshot("Notebook");
        orderItem.setProductImageUrlSnapshot("https://img.test/notebook.png");
        orderItem.setUnitPriceSnapshot(new BigDecimal("4.25"));
        orderItem.setQuantity(2);
        vendorOrder.setItems(new ArrayList<>(List.of(orderItem)));

        OrderStatusHistory history = new OrderStatusHistory();
        history.setId(100L);
        history.setVendorOrder(vendorOrder);
        history.setPreviousStatus(null);
        history.setNewStatus(OrderStatus.PENDING);
        history.setChangedAt(LocalDateTime.now());
        history.setNote("Order placed");
        vendorOrder.setStatusHistory(new ArrayList<>(List.of(history)));
        checkoutOrder.setVendorOrders(new ArrayList<>(List.of(vendorOrder)));

        return new CheckoutOrderResponse(checkoutOrder);
    }
}
