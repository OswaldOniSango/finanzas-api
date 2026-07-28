package com.finanzas.users.repository;

import java.util.Optional;

import com.finanzas.users.model.AppUser;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppUserRepository extends ListCrudRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    boolean existsByUsername(String username);
}
