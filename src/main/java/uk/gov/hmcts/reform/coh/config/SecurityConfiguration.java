package uk.gov.hmcts.reform.coh.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

@Configuration
@EnableWebSecurity
@Profile({"!cucumber & !local"})
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

//    private final AbstractPreAuthenticatedProcessingFilter filter;

//    @Autowired
//    public SecurityConfiguration(AbstractPreAuthenticatedProcessingFilter filter) {
//        super();
//        this.filter = filter;
//    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().antMatchers("/swagger-ui.html",
                "/webjars/springfox-swagger-ui/**",
                "/swagger-resources/**",
                "/v2/**",
                "/health",
                "/health/liveness",
                "/status/health",
                "/loggers/**",
                "/");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
//            .addFilter(filter)
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/error", "/health", "/health/liveness", "/status/health", "/").permitAll()
            .antMatchers("/SSCS/*").anonymous()
            .antMatchers("/swagger-ui.html", "/swagger-resources/**", "/webjars/**", "/v2/api-docs").anonymous()
            .anyRequest().authenticated();
    }
}
