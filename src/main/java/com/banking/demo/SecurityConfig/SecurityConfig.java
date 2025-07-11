package com.banking.demo.SecurityConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.banking.demo.Service.MyUserDetailsService;

@Configuration
public class SecurityConfig {

    @Autowired
    private MyUserDetailsService userDetailsService;

    // Bean to encode passwords using BCrypt hashing algorithm
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Authentication provider using custom UserDetailsService and password encoder
    @Bean
    public AuthenticationProvider authProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // Main security configuration for HTTP requests
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Authorization rules for URLs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/",                 // Home
                    "/login",           // Login page
                    "/faqs",            // FAQs
                    "/faq.css",         // Styles
                    "/css/**",          // Static CSS
                    "/js/**",           // Static JS
                    "/customer-care",   // Customer support
                    "/privacy-policy",  // Privacy Policy
                    "/openAccount",     // Account creation
                    "/saveAccount",     // Save new account
                    "/account-success", // Success page
                    "/login/save",      // Login submit URL
                    "/forgotpassword",  // Forgot password form
                    "/forgotmail",      // Forgot password handling
                    "/resetpassword",   // Reset form
                    "/changepassword",  // Change password
                    "/accounthome",     // Account home
                    "/logout"           // Logout
                ).permitAll()           // All above URLs are public
                .anyRequest().authenticated() // All others require login
            )
            // Login form customization
            .formLogin(form -> form
                .loginPage("/login")                 // Custom login page
                .loginProcessingUrl("/login/save")   // Spring handles POST here
                .defaultSuccessUrl("/accounthome", true) // On successful login
                .failureUrl("/login?error=true")     // On login failure
                .permitAll()
            )
            // Logout configuration
            .logout(logout -> logout
                .logoutUrl("/logout")                    // Logout URL
                .logoutSuccessUrl("/login?logout")       // Redirect after logout
                .invalidateHttpSession(true)             // Invalidate session
                .deleteCookies("JSESSIONID")             // Remove session cookie
                .clearAuthentication(true)               // Clear auth object
                .permitAll()
            );

        return http.build();
    }
}
