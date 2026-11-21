package com.collegecart.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.collegecart.config.SecurityConfig;
import com.collegecart.dto.ListingBrowseResponse;
import com.collegecart.dto.ListingResponse;
import com.collegecart.entity.Category;
import com.collegecart.entity.StudentListing;
import com.collegecart.entity.User;
import com.collegecart.enums.ListingCondition;
import com.collegecart.enums.ListingStatus;
import com.collegecart.repository.UserRepository;
import com.collegecart.service.JwtService;
import com.collegecart.service.StudentListingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StudentListingController.class)
@Import(SecurityConfig.class)
class StudentListingControllerTests {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private StudentListingService listingService;
    @MockitoBean private JwtService jwtService;
    @MockitoBean private UserRepository userRepository;

    private ListingResponse listingResponse;
    private ListingBrowseResponse browseResponse;

    @BeforeEach
    void setUp() {
        StudentListing listing = listing();
        listingResponse = new ListingResponse(listing);
        browseResponse = new ListingBrowseResponse(new PageImpl<>(List.of(listing), PageRequest.of(1, 5), 20));
    }

    @Test
    void browseEndpointReturnsPageAndPassesFiltersAndSort() throws Exception {
        when(listingService.browseListings(any(), any(), any(), any(), any(), any(Pageable.class)))
                .thenReturn(browseResponse);
        mockMvc.perform(get("/api/secondhand/listings")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .param("search", "textbook")
                        .param("categoryId", "21")
                        .param("condition", "GOOD")
                        .param("minPrice", "10.00")
                        .param("maxPrice", "50.00")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "askingPrice,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(51))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.content[0].sellerDisplayName").value("Avery Student"));
        verify(listingService).browseListings(eq("textbook"), eq(21L), eq(ListingCondition.GOOD),
                eq(new BigDecimal("10.00")), eq(new BigDecimal("50.00")), any(Pageable.class));
    }

    @Test
    void availableDetailEndpointReturnsListingAndProtectsSellerContactFields() throws Exception {
        when(listingService.findAvailableListing(51L)).thenReturn(listingResponse);
        mockMvc.perform(get("/api/secondhand/listings/51")
                        .with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Course textbook"))
                .andExpect(jsonPath("$.categoryName").value("Books"))
                .andExpect(jsonPath("$.sellerDisplayName").value("Avery Student"))
                .andExpect(jsonPath("$.sellerEmail").doesNotExist())
                .andExpect(jsonPath("$.sellerPhone").doesNotExist());
    }

    @Test
    void ownListingsEndpointReturnsList() throws Exception {
        when(listingService.findMyListings(any(Authentication.class))).thenReturn(List.of(listingResponse));
        mockMvc.perform(get("/api/student/listings")
                        .with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(51));
    }

    @Test
    void createEndpointReturnsCreatedAndDelegatesValidatedRequest() throws Exception {
        when(listingService.createListing(any(), any(Authentication.class))).thenReturn(listingResponse);
        mockMvc.perform(post("/api/student/listings")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(51))
                .andExpect(jsonPath("$.askingPrice").value(25.5))
                .andExpect(jsonPath("$.imageUrls[0]").value("https://img.example/book.png"));
        verify(listingService).createListing(any(com.collegecart.dto.ListingCreateRequest.class),
                any(Authentication.class));
    }

    @Test
    void ownListingDetailEndpointReturnsOwnedListing() throws Exception {
        when(listingService.findMyListing(eq(51L), any(Authentication.class))).thenReturn(listingResponse);
        mockMvc.perform(get("/api/student/listings/51")
                        .with(user("student@example.edu").roles("STUDENT")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"));
    }

    @Test
    void updateEndpointReturnsUpdatedListing() throws Exception {
        when(listingService.updateListing(eq(51L), any(), any(Authentication.class))).thenReturn(listingResponse);
        mockMvc.perform(put("/api/student/listings/51")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreateJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Course textbook"));
        verify(listingService).updateListing(eq(51L), any(com.collegecart.dto.ListingUpdateRequest.class),
                any(Authentication.class));
    }

    @Test
    void statusEndpointReturnsUpdatedListing() throws Exception {
        ListingResponse soldResponse = new ListingResponse(listing(ListingStatus.SOLD));
        when(listingService.updateListingStatus(eq(51L), any(), any(Authentication.class)))
                .thenReturn(soldResponse);
        mockMvc.perform(patch("/api/student/listings/51/status")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"SOLD\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SOLD"));
        verify(listingService).updateListingStatus(eq(51L),
                any(com.collegecart.dto.ListingStatusUpdateRequest.class), any(Authentication.class));
    }

    @Test
    void invalidCreateUpdateAndStatusBodiesReturnBadRequest() throws Exception {
        String invalid = "{\"title\":\"\",\"description\":\"\",\"askingPrice\":0,\"categoryId\":0," +
                "\"condition\":null,\"imageUrls\":[\"file:///bad.png\"]}";
        mockMvc.perform(post("/api/student/listings")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/api/student/listings/51")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON).content(invalid))
                .andExpect(status().isBadRequest());
        mockMvc.perform(patch("/api/student/listings/51/status")
                        .with(user("student@example.edu").roles("STUDENT"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"AVAILABLE\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unauthenticatedAndNonStudentRequestsAreRejected() throws Exception {
        mockMvc.perform(get("/api/secondhand/listings/51")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/student/listings")
                        .with(user("vendor@example.edu").roles("VENDOR")))
                .andExpect(status().isForbidden());
    }

    private StudentListing listing() {
        return listing(ListingStatus.AVAILABLE);
    }

    private StudentListing listing(ListingStatus status) {
        Category category = new Category();
        category.setId(21L);
        category.setName("Books");
        User seller = new User();
        seller.setFullName("Avery Student");
        seller.setEmail("private@example.edu");
        seller.setPhoneNumber("5551234567");
        StudentListing listing = new StudentListing();
        listing.setId(51L);
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle("Course textbook");
        listing.setDescription("Used textbook in good condition");
        listing.setAskingPrice(new BigDecimal("25.50"));
        listing.setCondition(ListingCondition.GOOD);
        listing.setStatus(status);
        com.collegecart.entity.StudentListingImage image = new com.collegecart.entity.StudentListingImage();
        image.setListing(listing);
        image.setImageUrl("https://img.example/book.png");
        image.setDisplayOrder(0);
        listing.setImages(List.of(image));
        ReflectionTestUtils.setField(listing, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(listing, "updatedAt", LocalDateTime.now());
        return listing;
    }

    private String validCreateJson() {
        return """
                {"title":"Course textbook","description":"Used textbook in good condition",
                 "askingPrice":25.50,"categoryId":21,"condition":"GOOD",
                 "imageUrls":["https://img.example/book.png"]}
                """;
    }
}
