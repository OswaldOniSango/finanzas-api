package com.finanzas.users.service;

import com.finanzas.users.model.AppUser;
import com.finanzas.users.repository.AppUserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    private final AppUserRepository repository;

    public CurrentUserService(AppUserRepository repository) {
        this.repository = repository;
    }

    public AppUser require() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return repository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("El usuario autenticado ya no existe"));
    }
}
