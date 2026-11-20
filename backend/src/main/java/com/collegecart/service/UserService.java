
package com.collegecart.service;

import com.collegecart.entity.User;
import com.collegecart.repository.UserRepository;

import org.springframework.stereotype.Service;

import com.collegecart.dto.UserManagementResponse;
import java.util.List;
import com.collegecart.exception.ResourceNotFoundException;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public User findById(Long userId) {

    return userRepository.findById(userId)
            .orElseThrow(() ->
                    new ResourceNotFoundException("User not found"));
}

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public List<UserManagementResponse> findAllUsers() {

    return userRepository.findAll()
                .stream()
                .map(UserManagementResponse::new)
                .toList();
    }

    public void updateUserStatus(Long userId, boolean active) {

    User user = findById(userId);

    user.setActive(active);

    userRepository.save(user);
}
}
