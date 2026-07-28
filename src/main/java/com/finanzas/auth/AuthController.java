package com.finanzas.auth;

import java.time.Duration;
import java.time.Instant;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final Duration tokenDuration;
    private final UserDetailsService userDetailsService;
    private final String demoUsername;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtEncoder jwtEncoder,
                          UserDetailsService userDetailsService,
                          @Value("${app.security.token-hours}") long tokenHours,
                          @Value("${app.demo.username:demo}") String demoUsername) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.userDetailsService = userDetailsService;
        this.tokenDuration = Duration.ofHours(tokenHours);
        this.demoUsername = demoUsername;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password()));
            return ResponseEntity.ok(createLoginResponse(authentication));
        } catch (BadCredentialsException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/demo")
    public LoginResponse demo() {
        UserDetails user = userDetailsService.loadUserByUsername(demoUsername);
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(
                user, null, user.getAuthorities());
        return createLoginResponse(authentication);
    }

    private LoginResponse createLoginResponse(Authentication authentication) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("finanzas-api")
                .issuedAt(now)
                .expiresAt(now.plus(tokenDuration))
                .subject(authentication.getName())
                .claim("roles", authentication.getAuthorities().stream().map(Object::toString).toList())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        String role = authentication.getAuthorities().iterator().next().getAuthority().replaceFirst("^ROLE_", "");
        return new LoginResponse(token, tokenDuration.toSeconds(), authentication.getName(), role);
    }
}
