package uk.gov.hmcts.reform.coh.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import javax.servlet.http.HttpServletRequest;

@Configuration
@Profile("!cucumber")
public class SecurityClientConfiguration {

    @Value("${authorization.roles}")
    private String authorizedRoles;

    @Value("${authorization.s2s-names-whitelist}")
    private String whitelistedServices;

    private static Collection<String> split(String value) {
        if (value == null) {
            return Collections.emptyList();
        } else {
            return Arrays.asList(value.split(","));
        }
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return request -> split(authorizedRoles);
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return request -> split(whitelistedServices);
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return request -> Optional.empty();
    }

    @Bean
    public AbstractPreAuthenticatedProcessingFilter preAuthenticatedProcessingFilter(
        AuthenticationManager authenticationManager,
        @Qualifier("serviceRequestAuthorizer") RequestAuthorizer<Service> serviceRequestAuthorizer,
        RequestAuthorizer<User> userRequestAuthorizer
    ) {
        AbstractPreAuthenticatedProcessingFilter filter
            = new AuthCheckerServiceAndUserFilter(serviceRequestAuthorizer, userRequestAuthorizer);

        filter.setAuthenticationManager(authenticationManager);

        return filter;
    }

}
