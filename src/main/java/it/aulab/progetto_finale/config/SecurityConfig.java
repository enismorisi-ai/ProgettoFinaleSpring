package it.aulab.progetto_finale.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests((authorize) -> authorize.requestMatchers("/register/**").permitAll()
            .requestMatchers("/register").permitAll()
            .anyRequest().authenticated()
        ).formLogin(form -> 
            form.loginPage("/login")
            .loginProcessingUrl("/login")
            .defaultSuccessUrl("/")
            .permitAll()
        ).logout(logout -> logout
            .logoutUrl("/logout")
            // .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
            .permitAll()
        ).exceptionHandling(exception->exception.accessDeniedPage("/error/403"))
        .sessionManagement(session->session
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .maximumSessions(1)
            .expiredUrl("/login?session-expired=true")
        );

        return http.build();
    }
}
