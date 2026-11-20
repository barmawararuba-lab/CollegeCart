package com.collegecart.service;

import com.collegecart.dto.StudentRegistrationRequest;
import com.collegecart.dto.VendorRegistrationRequest;
import com.collegecart.entity.User;
import com.collegecart.entity.VendorProfile;
import com.collegecart.enums.Role;
import com.collegecart.enums.ApprovalStatus;
import com.collegecart.repository.UserRepository;
import com.collegecart.repository.VendorProfileRepository;
import com.collegecart.exception.InvalidCredentialsException;
import com.collegecart.exception.DuplicateResourceException;

import com.collegecart.dto.LoginRequest;
import com.collegecart.dto.AuthResponse;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final VendorProfileRepository vendorProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
        UserRepository userRepository,
        VendorProfileRepository vendorProfileRepository,
        PasswordEncoder passwordEncoder,
        JwtService jwtService) {

    this.userRepository = userRepository;
    this.vendorProfileRepository = vendorProfileRepository;
    this.passwordEncoder = passwordEncoder;
    this.jwtService = jwtService;
    }   

    @Transactional
    public void registerStudent(StudentRegistrationRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
    throw new DuplicateResourceException("Email is already registered");
}

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.STUDENT);
        user.setActive(true);

        userRepository.save(user);
    }

    @Transactional
    public void registerVendor(VendorRegistrationRequest request) {

        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
    throw new DuplicateResourceException("Email is already registered");
}

        User user = new User();

        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword()));
        user.setPhoneNumber(request.getPhoneNumber());
        user.setRole(Role.VENDOR);
        user.setActive(true);

        userRepository.save(user);

        VendorProfile vendorProfile = new VendorProfile();

        vendorProfile.setUser(user);
        vendorProfile.setShopName(request.getShopName().trim());
        vendorProfile.setShopDescription(
                request.getShopDescription());
        vendorProfile.setShopAddress(request.getShopAddress());
        vendorProfile.setApprovalStatus(ApprovalStatus.PENDING);

        vendorProfileRepository.save(vendorProfile);
    }
public AuthResponse login(LoginRequest request) {

    String email = request.getEmail()
            .trim()
            .toLowerCase(Locale.ROOT);

    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new InvalidCredentialsException(
                "Invalid email or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
    throw new InvalidCredentialsException("Invalid email or password");
}

    if (!user.isActive()) {
        throw new IllegalArgumentException(
                "Your account is inactive");
    }

    String token = jwtService.generateToken(user.getEmail());

    return new AuthResponse(
            token,
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole()
    );
}
}
