package com.acervodigital.identity.admin;

import com.acervodigital.identity.account.CollectorAccount;
import com.acervodigital.identity.account.CollectorAccountRepository;
import com.acervodigital.identity.account.CollectorRole;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class AdminCollectorService {

    private final CollectorAccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminCollectorService(
            CollectorAccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<AdminCollectorResponse> findAll() {
        return accountRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(AdminCollectorResponse::from)
                .toList();
    }

    @Transactional
    public AdminCollectorResponse create(CreateAdminCollectorRequest request) {
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }
        CollectorAccount account = CollectorAccount.register(
                email,
                passwordEncoder.encode(request.password()),
                request.displayName().trim()
        );
        return AdminCollectorResponse.from(accountRepository.save(account));
    }

    @Transactional
    public AdminCollectorResponse update(UUID actorId, UUID targetId, UpdateAdminCollectorRequest request) {
        CollectorAccount account = findOtherAccount(actorId, targetId);
        String email = normalizeEmail(request.email());
        if (accountRepository.existsByEmailAndIdNot(email, targetId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe una cuenta con ese email");
        }
        ensureAnActiveAdminRemains(account, request.role(), request.active());
        account.update(email, request.displayName().trim(), request.role(), request.active());
        return AdminCollectorResponse.from(account);
    }

    @Transactional
    public void deactivate(UUID actorId, UUID targetId) {
        CollectorAccount account = findOtherAccount(actorId, targetId);
        ensureAnActiveAdminRemains(account, account.getRole(), false);
        account.update(account.getEmail(), account.getProfile().getDisplayName(), account.getRole(), false);
    }

    @Transactional
    public void resetPassword(UUID actorId, UUID targetId, ResetAdminCollectorPasswordRequest request) {
        CollectorAccount account = findOtherAccount(actorId, targetId);
        account.resetPassword(passwordEncoder.encode(request.password()));
    }

    private CollectorAccount findOtherAccount(UUID actorId, UUID targetId) {
        if (actorId.equals(targetId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No se puede modificar la propia cuenta desde administración");
        }
        return accountRepository.findById(targetId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));
    }

    private void ensureAnActiveAdminRemains(
            CollectorAccount account,
            CollectorRole newRole,
            boolean newActive
    ) {
        boolean removesActiveAdmin = account.isActive()
                && account.getRole() == CollectorRole.ADMIN
                && (!newActive || newRole != CollectorRole.ADMIN);
        if (removesActiveAdmin && accountRepository.lockActiveAccountsByRole(CollectorRole.ADMIN).size() <= 1) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Debe permanecer al menos un administrador activo");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}