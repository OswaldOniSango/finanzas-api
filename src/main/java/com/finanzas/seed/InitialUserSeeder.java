package com.finanzas.seed;

import com.finanzas.periods.repository.FinancialPeriodRepository;
import com.finanzas.users.model.AppUser;
import com.finanzas.users.model.UserRole;
import com.finanzas.users.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(1)
public class InitialUserSeeder implements ApplicationRunner {

    private final AppUserRepository userRepository;
    private final FinancialPeriodRepository periodRepository;
    private final PasswordEncoder passwordEncoder;
    private final String username;
    private final String password;

    public InitialUserSeeder(AppUserRepository userRepository,
                             FinancialPeriodRepository periodRepository,
                             PasswordEncoder passwordEncoder,
                             @Value("${app.security.username}") String username,
                             @Value("${app.security.password}") String password) {
        this.userRepository = userRepository;
        this.periodRepository = periodRepository;
        this.passwordEncoder = passwordEncoder;
        this.username = username;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppUser admin = userRepository.findByUsername(username)
                .orElseGet(() -> userRepository.save(new AppUser(
                        null, username, passwordEncoder.encode(password), UserRole.ADMIN, true, null, null)));

        periodRepository.findAll().stream()
                .filter(period -> period.ownerUserId() == null)
                .map(period -> period.withOwnerUserId(admin.id()))
                .forEach(periodRepository::save);
    }
}
