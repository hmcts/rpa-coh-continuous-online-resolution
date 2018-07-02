package uk.gov.hmcts.reform.coh.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import uk.gov.hmcts.reform.auth.checker.core.RequestAuthorizer;
import uk.gov.hmcts.reform.auth.checker.core.service.Service;
import uk.gov.hmcts.reform.auth.checker.core.user.User;
import uk.gov.hmcts.reform.auth.checker.spring.serviceanduser.AuthCheckerServiceAndUserFilter;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Autowired
    private RequestAuthorizer<Service> serviceRequestAuthorizer;

    @Autowired
    private RequestAuthorizer<User> userRequestAuthorizer;

    @Autowired
    private AuthenticationManager authenticationManager;

    private AuthCheckerServiceAndUserFilter serviceAndUserFilter;


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .addFilter(serviceAndUserFilter)
            .sessionManagement().sessionCreationPolicy(STATELESS).and()
            .csrf().disable()
            .formLogin().disable()
            .logout().disable()
            .authorizeRequests()
            .anyRequest().authenticated();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring()
            .antMatchers("/swagger-ui.html",
                "/webjars/springfox-swagger-ui/**",
                "/swagger-resources/**",
                "/v2/**",
                "/favicon.ico",
                "/health",
                "/mappings",
                "/info");
    }

    @Value("#{'${authorization.s2s-names-whitelist}'.split(',')}")
    private List<String> s2sNamesWhiteList;

    @Value("#{'${authorization.idam-roles-whitelist}'.split(',')}")
    private List<String> idamRolesWhitelist;


    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedServicesExtractor() {
        return any -> s2sNamesWhiteList;
    }

    @Bean
    public Function<HttpServletRequest, Collection<String>> authorizedRolesExtractor() {
        return any -> idamRolesWhitelist;
    }

    @Bean
    public Function<HttpServletRequest, Optional<String>> userIdExtractor() {
        return (request) -> Optional.empty();
    }

    @Autowired
    public void setServiceAndUserFilter(Optional<AuthCheckerServiceAndUserFilter> serviceAndUserFilter) {
        this.serviceAndUserFilter = serviceAndUserFilter.orElseGet(() -> {
            AuthCheckerServiceAndUserFilter filter = new AuthCheckerServiceAndUserFilter(
                serviceRequestAuthorizer,
                userRequestAuthorizer
            );
            filter.setAuthenticationManager(authenticationManager);
            return filter;
        });
    }
}
