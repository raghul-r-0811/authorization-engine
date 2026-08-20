package org.raghul.auth_engine.service;

import lombok.RequiredArgsConstructor;
import org.raghul.auth_engine.entity.UserEntity;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.UserRepo;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentUserService {

    private final UserRepo userRepo;

    public CurrentActor getCurrentActor() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResourceNotFoundException("Authenticated user not found");
        }

        String actorEmail = authentication.getName();

        UserEntity actorUser = userRepo.findByEmail(actorEmail);

        if (actorUser == null) {
            throw new ResourceNotFoundException(
                    "Authenticated user not found: " + actorEmail
            );
        }

        Integer actorTenantId = (Integer) authentication.getDetails();

        return new CurrentActor(
                actorUser.getuId(),
                actorTenantId
        );
    }

    public record CurrentActor(
            Integer userId,
            Integer tenantId
    ) {
    }
}