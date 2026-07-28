package com.finanzas.users.service;

import java.util.Comparator;
import java.util.List;

import com.finanzas.api.ConflictException;
import com.finanzas.users.dto.CreateUserRequest;
import com.finanzas.users.dto.UserResponse;
import com.finanzas.users.model.AppUser;
import com.finanzas.users.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AppUserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<UserResponse> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(AppUser::username))
                .map(UserResponse::from)
                .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String username = request.username().trim();
        if (repository.existsByUsername(username)) {
            throw new ConflictException("Ya existe el usuario " + username);
        }
        AppUser saved = repository.save(new AppUser(
                null, username, passwordEncoder.encode(request.password()), request.role(), true, null, null));
        return UserResponse.from(saved);
    }
}
