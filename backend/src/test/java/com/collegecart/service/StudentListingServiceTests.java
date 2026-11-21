package com.collegecart.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collegecart.dto.ListingBrowseResponse;
import com.collegecart.dto.ListingCreateRequest;
import com.collegecart.dto.ListingResponse;
import com.collegecart.dto.ListingStatusUpdateRequest;
import com.collegecart.dto.ListingUpdateRequest;
import com.collegecart.entity.Category;
import com.collegecart.entity.StudentListing;
import com.collegecart.entity.StudentListingImage;
import com.collegecart.entity.User;
import com.collegecart.enums.ListingCondition;
import com.collegecart.enums.ListingStatus;
import com.collegecart.enums.Role;
import com.collegecart.exception.ResourceNotFoundException;
import com.collegecart.repository.CategoryRepository;
import com.collegecart.repository.StudentListingRepository;
import com.collegecart.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class StudentListingServiceTests {

    @Mock private StudentListingRepository listingRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @Mock private EntityManager entityManager;
    @Mock private Authentication authentication;

    @InjectMocks private StudentListingService service;

    private User student;
    private Category activeCategory;

    @BeforeEach
    void setUp() {
        student = student(11L, "student@example.edu", true, Role.STUDENT);
        activeCategory = category(21L, true);
        Mockito.lenient().when(authentication.isAuthenticated()).thenReturn(true);
        Mockito.lenient().when(authentication.getName()).thenReturn("student@example.edu");
        Mockito.lenient().when(userRepository.findByEmail("student@example.edu"))
                .thenReturn(Optional.of(student));
    }

    @Test
    void activeStudentCanCreateListingAndSaveThreeOrderedImages() {
        when(categoryRepository.findById(21L)).thenReturn(Optional.of(activeCategory));
        when(listingRepository.save(any(StudentListing.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ListingResponse response = service.createListing(createRequest(
                List.of("https://img.example/1.png", "https://img.example/2.png", "https://img.example/3.png")),
                authentication);

        ArgumentCaptor<StudentListing> captor = ArgumentCaptor.forClass(StudentListing.class);
        verify(listingRepository).save(captor.capture());
        StudentListing saved = captor.getValue();
        assertEquals(student, saved.getSeller());
        assertEquals(ListingStatus.AVAILABLE, saved.getStatus());
        assertEquals(3, saved.getImages().size());
        assertEquals(List.of(0, 1, 2), saved.getImages().stream()
                .map(StudentListingImage::getDisplayOrder).toList());
        assertEquals(List.of("https://img.example/1.png", "https://img.example/2.png", "https://img.example/3.png"),
                response.getImageUrls());
        assertEquals("Avery Student", response.getSellerDisplayName());
    }

    @Test
    void inactiveStudentCannotCreateListing() {
        student.setActive(false);
        assertThrows(AccessDeniedException.class,
                () -> service.createListing(createRequest(null), authentication));
        verify(listingRepository, never()).save(any(StudentListing.class));
    }

    @Test
    void nonStudentCannotCreateListing() {
        student.setRole(Role.VENDOR);
        assertThrows(AccessDeniedException.class,
                () -> service.createListing(createRequest(null), authentication));
        verify(listingRepository, never()).save(any(StudentListing.class));
    }

    @Test
    void missingAndInactiveCategoriesAreRejected() {
        when(categoryRepository.findById(21L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.createListing(createRequest(null), authentication));

        Category inactive = category(21L, false);
        when(categoryRepository.findById(21L)).thenReturn(Optional.of(inactive));
        assertThrows(IllegalArgumentException.class,
                () -> service.createListing(createRequest(null), authentication));
        verify(listingRepository, never()).save(any(StudentListing.class));
    }

    @Test
    void invalidImageUrlsAndMoreThanThreeImagesAreRejected() {
        when(categoryRepository.findById(21L)).thenReturn(Optional.of(activeCategory));
        assertThrows(IllegalArgumentException.class,
                () -> service.createListing(createRequest(List.of("file:///private.png")), authentication));
        assertThrows(IllegalArgumentException.class,
                () -> service.createListing(createRequest(List.of(
                        "https://img.example/1", "https://img.example/2", "https://img.example/3", "https://img.example/4")), authentication));
        verify(listingRepository, never()).save(any(StudentListing.class));
    }

    @Test
    void browsePassesSearchFiltersPaginationAndSortToAvailableListingQuery() {
        StudentListing result = listing(1L, student, activeCategory, ListingStatus.AVAILABLE);
        Pageable pageable = PageRequest.of(2, 7, Sort.by(Sort.Direction.ASC, "askingPrice"));
        when(listingRepository.searchAvailableListings(
                "textbook", 21L, ListingCondition.GOOD, new BigDecimal("10.00"), new BigDecimal("80.00"), pageable))
                .thenReturn(new PageImpl<>(List.of(result), pageable, 100));

        ListingBrowseResponse response = service.browseListings(" textbook ", 21L, ListingCondition.GOOD,
                new BigDecimal("10.00"), new BigDecimal("80.00"), pageable);

        assertEquals(1, response.getContent().size());
        assertEquals(ListingStatus.AVAILABLE, response.getContent().get(0).getStatus());
        assertEquals(2, response.getPage());
        assertEquals(7, response.getSize());
        assertEquals(100, response.getTotalElements());
        verify(listingRepository).searchAvailableListings(
                "textbook", 21L, ListingCondition.GOOD, new BigDecimal("10.00"), new BigDecimal("80.00"), pageable);
    }

    @Test
    void publicBrowseRejectsInvalidPriceRange() {
        assertThrows(IllegalArgumentException.class, () -> service.browseListings(
                null, null, null, new BigDecimal("20"), new BigDecimal("10"), Pageable.unpaged()));
        verify(listingRepository, never()).searchAvailableListings(any(), any(), any(), any(), any(), any());
    }

    @Test
    void publicDetailHidesSoldAndRemovedListings() {
        for (ListingStatus status : List.of(ListingStatus.SOLD, ListingStatus.REMOVED)) {
            when(listingRepository.findById(31L))
                    .thenReturn(Optional.of(listing(31L, student, activeCategory, status)));
            assertThrows(ResourceNotFoundException.class, () -> service.findAvailableListing(31L));
        }
    }

    @Test
    void publicDetailReturnsAvailableListingAndOnlySellerDisplayName() {
        StudentListing available = listing(31L, student, activeCategory, ListingStatus.AVAILABLE);
        when(listingRepository.findById(31L)).thenReturn(Optional.of(available));
        ListingResponse response = service.findAvailableListing(31L);
        assertEquals("Avery Student", response.getSellerDisplayName());
        assertTrue(ListingResponse.class.getDeclaredFields().length > 0);
        assertTrue(java.util.Arrays.stream(ListingResponse.class.getDeclaredFields())
                .noneMatch(field -> field.getName().toLowerCase().contains("email")
                        || field.getName().toLowerCase().contains("phone")));
    }

    @Test
    void ownListingsIncludeAllStatusesAndAreScopedToStudent() {
        Pageable pageable = Pageable.unpaged();
        when(listingRepository.findBySellerIdOrderByCreatedAtDesc(11L, pageable)).thenReturn(
                new PageImpl<>(List.of(
                        listing(1L, student, activeCategory, ListingStatus.AVAILABLE),
                        listing(2L, student, activeCategory, ListingStatus.SOLD),
                        listing(3L, student, activeCategory, ListingStatus.REMOVED))));
        List<ListingResponse> results = service.findMyListings(authentication);
        assertEquals(List.of(ListingStatus.AVAILABLE, ListingStatus.SOLD, ListingStatus.REMOVED),
                results.stream().map(ListingResponse::getStatus).toList());
        verify(listingRepository).findBySellerIdOrderByCreatedAtDesc(11L, pageable);
    }

    @Test
    void studentCannotReadOrEditAnotherStudentsListing() {
        when(listingRepository.findByIdAndSellerId(99L, 11L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.findMyListing(99L, authentication));
        assertThrows(ResourceNotFoundException.class,
                () -> service.updateListing(99L, updateRequest(List.of()), authentication));
        verify(listingRepository, never()).save(any(StudentListing.class));
    }

    @Test
    void ownerCanUpdateAvailableListingAndReplaceOrderedImages() {
        StudentListing existing = listing(41L, student, activeCategory, ListingStatus.AVAILABLE);
        existing.getImages().add(image(existing, "https://img.example/old.png", 0));
        when(listingRepository.findByIdAndSellerId(41L, 11L)).thenReturn(Optional.of(existing));
        when(categoryRepository.findById(21L)).thenReturn(Optional.of(activeCategory));
        when(listingRepository.save(existing)).thenReturn(existing);

        ListingResponse updated = service.updateListing(41L,
                updateRequest(List.of("https://img.example/new1.png", "https://img.example/new2.png")), authentication);

        assertEquals("Updated textbook", existing.getTitle());
        assertEquals(List.of("https://img.example/new1.png", "https://img.example/new2.png"), updated.getImageUrls());
        assertEquals(List.of(0, 1), existing.getImages().stream().map(StudentListingImage::getDisplayOrder).toList());
        verify(listingRepository).flush();
        verify(listingRepository).save(existing);
    }

    @Test
    void cannotUpdateSoldOrRemovedListing() {
        when(listingRepository.findByIdAndSellerId(51L, 11L)).thenReturn(Optional.of(
                listing(51L, student, activeCategory, ListingStatus.SOLD)));
        when(listingRepository.findByIdAndSellerId(52L, 11L)).thenReturn(Optional.of(
                listing(52L, student, activeCategory, ListingStatus.REMOVED)));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateListing(51L, updateRequest(List.of()), authentication));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateListing(52L, updateRequest(List.of()), authentication));
        verify(listingRepository, never()).save(any(StudentListing.class));
    }

    @Test
    void availableListingCanBeMarkedSoldOrRemovedAndSoldAtIsSetOnlyForSold() {
        for (ListingStatus target : List.of(ListingStatus.SOLD, ListingStatus.REMOVED)) {
            StudentListing existing = listing(61L, student, activeCategory, ListingStatus.AVAILABLE);
            when(listingRepository.findByIdAndSellerId(61L, 11L)).thenReturn(Optional.of(existing));
            when(listingRepository.save(existing)).thenReturn(existing);
            ListingStatusUpdateRequest request = new ListingStatusUpdateRequest();
            request.setStatus(target);

            ListingResponse response = service.updateListingStatus(61L, request, authentication);
            assertEquals(target, response.getStatus());
            if (target == ListingStatus.SOLD) {
                assertNotNull(existing.getSoldAt());
            } else {
                assertEquals(null, existing.getSoldAt());
            }
        }
    }

    @Test
    void rejectsInvalidStatusChangesAndReactivation() {
        ListingStatusUpdateRequest invalidRequest = new ListingStatusUpdateRequest();
        invalidRequest.setStatus(ListingStatus.AVAILABLE);
        assertThrows(IllegalArgumentException.class,
                () -> service.updateListingStatus(71L, invalidRequest, authentication));

        when(listingRepository.findByIdAndSellerId(71L, 11L)).thenReturn(Optional.of(
                listing(71L, student, activeCategory, ListingStatus.SOLD)));
        when(listingRepository.findByIdAndSellerId(72L, 11L)).thenReturn(Optional.of(
                listing(72L, student, activeCategory, ListingStatus.REMOVED)));
        ListingStatusUpdateRequest changeSold = new ListingStatusUpdateRequest();
        changeSold.setStatus(ListingStatus.REMOVED);
        ListingStatusUpdateRequest changeRemoved = new ListingStatusUpdateRequest();
        changeRemoved.setStatus(ListingStatus.SOLD);
        assertThrows(IllegalArgumentException.class,
                () -> service.updateListingStatus(71L, changeSold, authentication));
        assertThrows(IllegalArgumentException.class,
                () -> service.updateListingStatus(72L, changeRemoved, authentication));
        verify(listingRepository, never()).save(any(StudentListing.class));
    }

    @Test
    void responseDoesNotExposeSellerEmailOrPhone() {
        student.setEmail("private@example.edu");
        student.setPhoneNumber("5551234567");
        ListingResponse response = new ListingResponse(listing(81L, student, activeCategory, ListingStatus.AVAILABLE));
        assertEquals("Avery Student", response.getSellerDisplayName());
        assertTrue(java.util.Arrays.stream(response.getClass().getDeclaredFields())
                .noneMatch(field -> field.getName().equalsIgnoreCase("email")
                        || field.getName().toLowerCase().contains("phone")));
    }

    private User student(Long id, String email, boolean active, Role role) {
        User user = new User();
        user.setId(id);
        user.setFullName("Avery Student");
        user.setEmail(email);
        user.setPhoneNumber("5551234567");
        user.setRole(role);
        user.setActive(active);
        return user;
    }

    private Category category(Long id, boolean active) {
        Category category = new Category();
        category.setId(id);
        category.setName("Books");
        category.setActive(active);
        return category;
    }

    private ListingCreateRequest createRequest(List<String> images) {
        ListingCreateRequest request = new ListingCreateRequest();
        request.setTitle("Course textbook");
        request.setDescription("A used course textbook in good condition");
        request.setAskingPrice(new BigDecimal("25.50"));
        request.setCategoryId(21L);
        request.setCondition(ListingCondition.GOOD);
        request.setImageUrls(images);
        return request;
    }

    private ListingUpdateRequest updateRequest(List<String> images) {
        ListingUpdateRequest request = new ListingUpdateRequest();
        request.setTitle("Updated textbook");
        request.setDescription("Updated description for my textbook");
        request.setAskingPrice(new BigDecimal("20.00"));
        request.setCategoryId(21L);
        request.setCondition(ListingCondition.LIKE_NEW);
        request.setImageUrls(images);
        return request;
    }

    private StudentListing listing(Long id, User seller, Category category, ListingStatus status) {
        StudentListing listing = new StudentListing();
        listing.setId(id);
        listing.setSeller(seller);
        listing.setCategory(category);
        listing.setTitle("Course textbook");
        listing.setDescription("A used course textbook in good condition");
        listing.setAskingPrice(new BigDecimal("25.50"));
        listing.setCondition(ListingCondition.GOOD);
        listing.setStatus(status);
        listing.setImages(new ArrayList<>());
        ReflectionTestUtils.setField(listing, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(listing, "updatedAt", LocalDateTime.now());
        return listing;
    }

    private StudentListingImage image(StudentListing listing, String url, int order) {
        StudentListingImage image = new StudentListingImage();
        image.setListing(listing);
        image.setImageUrl(url);
        image.setDisplayOrder(order);
        return image;
    }
}
