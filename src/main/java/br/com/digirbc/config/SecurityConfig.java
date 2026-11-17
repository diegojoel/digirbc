package br.com.digirbc.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .headers().frameOptions().sameOrigin()
            .and()
            .authorizeRequests()
                .antMatchers("/login.xhtml", "/javax.faces.resource/**").permitAll()
                .antMatchers("/cadastros/**", "/usuarios/**").hasRole("ADMINISTRADOR")
                .antMatchers("/solucoes/revisao.xhtml").hasAnyRole("ADMINISTRADOR", "TECNICO")
                .anyRequest().authenticated()
            .and()
            .formLogin()
                .loginPage("/login.xhtml")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/index.xhtml", true)
                .failureUrl("/login.xhtml?erro=true")
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login.xhtml?saiu=true")
                .permitAll();
        return http.build();
    }
}

