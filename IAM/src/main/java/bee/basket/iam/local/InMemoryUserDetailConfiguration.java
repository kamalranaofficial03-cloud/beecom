package bee.basket.iam.local;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
@ConditionalOnProperty(prefix = "app.datasource", name="provider", havingValue = "inmemory", matchIfMissing =false)
public class InMemoryUserDetailConfiguration {

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        var alice = User.builder()
                .username("alice")
                .password(passwordEncoder.encode("password"))
                .roles("USER", "ADMIN")
                .build();
        var bob = User.builder()
                .username("bob")
                .password(passwordEncoder.encode("password"))
                .roles("USER")
                .build();
        var carol = User.builder()
                .username("carol")
                .password(passwordEncoder.encode("password"))
                .roles("USER", "AUDITOR")
                .build();
        return new InMemoryUserDetailsManager(alice, bob, carol);
    }
}
