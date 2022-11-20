package tchat.microervices.ms_content_management.autorisationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf().disable();

        String base_url = "/api/v*/content_management/";
        http.authorizeRequests()
                .antMatchers("/uploads/**").permitAll()
                .antMatchers(base_url + "comments/**").hasAuthority("ROLE_USER")
                .antMatchers(base_url + "posts/**").hasAuthority("ROLE_USER")
                .antMatchers(base_url + "likes/**").hasAuthority("ROLE_USER")
                .antMatchers(base_url + "reports/**").hasAuthority("ROLE_ADMIN")
                .antMatchers(base_url + "reports/save").hasAuthority("ROLE_USER")
                .anyRequest().authenticated()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        http.addFilterBefore(new JwtAutorisationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
