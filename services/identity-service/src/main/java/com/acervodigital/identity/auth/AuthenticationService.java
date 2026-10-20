package com.acervodigital.identity.auth;

import com.acervodigital.identity.account.CollectorAccount;
import com.acervodigital.identity.account.CollectorAccountRepository;
import com.acervodigital.identity.security.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class AuthenticationService {

    private final CollectorAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthenticationService(
            CollectorAccountRepository accountRepository,
            PasswordEncoder passwordEncoder,
            JwtTokenService jwtTokenService
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public CollectorResponse register(RegisterCollectorRequest request) {
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }

        CollectorAccount account = CollectorAccount.register(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim()
        );
        return CollectorResponse.from(accountRepository.save(account));
    }

    public TokenResponse createToken(CreateTokenRequest request) {
        String email = normalizeEmail(request.email());
        CollectorAccount account = accountRepository.findByEmail(email)
            .filter(CollectorAccount::isActive)
                .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
        return jwtTokenService.issue(account);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}