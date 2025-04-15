package io.leedsk1y.reservault_backend.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import io.leedsk1y.reservault_backend.repositories.UserRepository;
import io.leedsk1y.reservault_backend.security.jwt.AuthEntryPointJwt;
import io.leedsk1y.reservault_backend.security.jwt.AuthTokenFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private final AuthEntryPointJwt unauthorizedHandler;
    private final UserRepository userRepository;

    public SecurityConfig(AuthEntryPointJwt unauthorizedHandler, UserRepository userRepository) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.userRepository = userRepository;
    }

    /**
     * Exposes the default Spring `AuthenticationManager` bean.
     * Used to perform authentication operations (e.g. login).
     * @param builder Spring's AuthenticationConfiguration.
     * @return Configured AuthenticationManager.
     * @throws Exception If creation fails.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration builder)
        throws Exception {
        return builder.getAuthenticationManager();
    }

    /**
     * Creates a BCryptPasswordEncoder bean used for password hashing and verification.
     * @return A PasswordEncoder instance.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides the custom JWT authentication filter bean to validate tokens before standard filters.
     * @return An instance of AuthTokenFilter.
     */
    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        return new AuthTokenFilter();
    }

    /**
     * Configures a custom UserDetailsService that loads users from the UserRepository by email.
     * @return A lambda-based implementation of UserDetailsService.
     * @throws UsernameNotFoundException If the user is not found in the database.
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    logger.warn("UserDetailsService: No user found for email '{}'", username);
                    return new UsernameNotFoundException("User not found");
                });
    }

    /**
     * Defines the main security filter chain configuration.
     * Sets up:
     * - endpoint access rules
     * - JWT exception handling
     * - stateless session policy
     * - CSRF and CORS config
     * - OAuth2 login paths
     * - JWT filter integration
     *
     * @param http The HttpSecurity object to customize.
     * @return Configured SecurityFilterChain.
     * @throws Exception If configuration fails.
     */
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests ->
            authorizeRequests
                .requestMatchers("/auth/register", "/auth/login", "/oauth2/**").permitAll() // security endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN") // admin endpoints
                .anyRequest().authenticated());

        http.sessionManagement(session ->
            session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)); // cookies

        http.exceptionHandling(exception ->
            exception.authenticationEntryPoint(unauthorizedHandler)); // choosing custom exception handler JWT

        http.csrf(csrf -> csrf.disable()); // disable csrf for mongodb

        http.addFilterBefore(authenticationJwtTokenFilter(),
            UsernamePasswordAuthenticationFilter.class); // add custom filter for JWT

        http.oauth2Login(oauth2 -> oauth2
            .loginPage("/oauth2/login/google")
            .defaultSuccessUrl("/oauth2/login/success", true)
            .failureUrl("/oauth2/login/failure"));

        http.cors(Customizer.withDefaults()); // cors

        return http.build();

    }
}
