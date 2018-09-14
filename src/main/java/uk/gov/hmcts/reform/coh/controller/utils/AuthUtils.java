package uk.gov.hmcts.reform.coh.controller.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.ServiceAndUserDetails;

import java.util.Optional;
import java.util.function.Consumer;

public class AuthUtils {

    /**
     * Provide authenticated username read from Security Context.
     * <p>
     * The call is made only if User was authenticated and the Principal object was {@link ServiceAndUserDetails} class.
     *
     * @param consumer target called with authenticated username
     */
    public static void withIdentity(Consumer<String> consumer) {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .filter(ServiceAndUserDetails.class::isInstance)
            .map(ServiceAndUserDetails.class::cast)
            .map(User::getUsername)
            .ifPresent(consumer);
    }
}
