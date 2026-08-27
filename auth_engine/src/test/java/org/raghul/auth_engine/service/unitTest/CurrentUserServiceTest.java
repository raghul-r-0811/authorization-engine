package org.raghul.auth_engine.service.unitTest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.raghul.auth_engine.entity.UserEntity;
import org.raghul.auth_engine.exception.ResourceNotFoundException;
import org.raghul.auth_engine.repository.UserRepo;
import org.raghul.auth_engine.service.CurrentUserService;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    private static final Integer USER_ID = 1;
    private static final Integer TENANT_ID = 10;
    private static final String USER_EMAIL = "raghul@example.com";

    @Mock private UserRepo userRepo;
    @InjectMocks private CurrentUserService currentUserService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentActor_whenAuthenticationIsMissing_throwsResourceNotFoundException() {
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> currentUserService.getCurrentActor());

        assertEquals("Authenticated user not found", exception.getMessage());
        verify(userRepo, never()).findByEmail(USER_EMAIL);
    }

    @Test
    void getCurrentActor_whenAuthenticationIsNotAuthenticated_throwsResourceNotFoundException() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(USER_EMAIL, null);
        authentication.setAuthenticated(false);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> currentUserService.getCurrentActor());

        assertEquals("Authenticated user not found", exception.getMessage());
        verify(userRepo, never()).findByEmail(USER_EMAIL);
    }

    @Test
    void getCurrentActor_whenAuthenticatedUserDoesNotExist_throwsResourceNotFoundException() {
        setAuthentication(USER_EMAIL, TENANT_ID);

        when(userRepo.findByEmail(USER_EMAIL)).thenReturn(null);

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> currentUserService.getCurrentActor());

        assertEquals("Authenticated user not found: " + USER_EMAIL, exception.getMessage());
        verify(userRepo).findByEmail(USER_EMAIL);
    }

    @Test
    void getCurrentActor_whenAuthenticatedUserExists_returnsUserIdAndTenantId() {
        UserEntity user = new UserEntity();
        user.setuId(USER_ID);

        setAuthentication(USER_EMAIL, TENANT_ID);

        when(userRepo.findByEmail(USER_EMAIL)).thenReturn(user);

        CurrentUserService.CurrentActor actor = currentUserService.getCurrentActor();

        assertEquals(USER_ID, actor.userId());
        assertEquals(TENANT_ID, actor.tenantId());
        verify(userRepo).findByEmail(USER_EMAIL);
    }

    private void setAuthentication(String email, Integer tenantId) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(email, null);

        authentication.setDetails(tenantId);
        authentication.setAuthenticated(true);

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}