package org.raghul.auth_engine.config;

import org.raghul.auth_engine.security.CustomUserDetailService;
import org.raghul.auth_engine.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsService customUserDetailService;
    private final BCryptPasswordEncoder  passwordEncoder;
    private final JwtAuthFilter jwtAuthFilter;


    @Autowired
    public SecurityConfig( @Qualifier("customUserDetailsService") CustomUserDetailService customUserDetailService,BCryptPasswordEncoder bCryptPasswordEncoder,JwtAuthFilter jwtAuthFilter){
        System.out.println("Secutity config construtor is called ");
        this.customUserDetailService = customUserDetailService;
        this.passwordEncoder = bCryptPasswordEncoder;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //System.out.println("");
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/app/home/register","/app/home/login","/app/home/test").permitAll()
                        .anyRequest().authenticated()).sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(){
            public void debugStack() {
                StackTraceElement[] stackTrace =
                        Thread.currentThread().getStackTrace();

                for (StackTraceElement ste : stackTrace) {
                    System.out.println(ste);
                }
            }

            @Override
            public void setUserDetailsService(UserDetailsService customUserDetailService){
                super.setUserDetailsService(customUserDetailService);
                System.out.println("setUserDetailService() is called in DaoAuthenticationManager");

            }
            @Override
            protected Authentication createSuccessAuthentication(Object principal, Authentication authentication,
                                                                 UserDetails user) {
                System.out.println("-------------createSuccessAuthentication() is called in DaoAuthenticationManager");
                debugStack();
                return super.createSuccessAuthentication(principal,authentication,user);
            }
        };


        authProvider.setUserDetailsService(customUserDetailService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
       System.out.println("SecurityConfig.authernticationManager");
        AuthenticationManager authenticationManager = config.getAuthenticationManager();
        System.out.println("================"+ authenticationManager.getClass());
        return authenticationManager;
    }

}
