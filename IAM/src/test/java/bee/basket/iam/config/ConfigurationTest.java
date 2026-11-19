package bee.basket.iam.config;

import bee.basket.iam.IAMApplication;
import bee.basket.iam.local.InMemoryUserDetailConfiguration;
import bee.basket.iam.local.LdapConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.server.UnboundIdContainer;
import org.springframework.security.ldap.userdetails.LdapUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class ConfigurationTest {

    @Test
    void auditConfigurationIsEnabledUnlessProviderIsNotRelational() {
        ConditionalOnProperty condition = AuditConfig.class.getAnnotation(ConditionalOnProperty.class);

        assertThat(condition.prefix()).isEqualTo("app.datasource");
        assertThat(condition.name()).containsExactly("provider");
        assertThat(condition.havingValue()).isEqualTo("rd");
        assertThat(condition.matchIfMissing()).isTrue();
    }

    @Test
    void inMemoryConfigurationCreatesThreeUsersWithExpectedRolesAndEncodedPasswords() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode("password")).thenReturn("encoded-password");

        var manager = (org.springframework.security.provisioning.InMemoryUserDetailsManager)
                new InMemoryUserDetailConfiguration().userDetailsService(encoder);

        assertThat(manager.loadUserByUsername("alice").getPassword()).isEqualTo("encoded-password");
        assertThat(manager.loadUserByUsername("alice").getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(manager.loadUserByUsername("bob").getAuthorities()).extracting("authority")
                .containsExactly("ROLE_USER");
        assertThat(manager.loadUserByUsername("carol").getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_AUDITOR");
    }

    @Test
    void inMemoryConfigurationIsConditionalOnInMemoryProvider() {
        ConditionalOnProperty condition = InMemoryUserDetailConfiguration.class
                .getAnnotation(ConditionalOnProperty.class);

        assertThat(condition.prefix()).isEqualTo("app.datasource");
        assertThat(condition.name()).containsExactly("provider");
        assertThat(condition.havingValue()).isEqualTo("inmemory");
        assertThat(condition.matchIfMissing()).isFalse();
    }

    @Test
    void ldapConfigurationBuildsContainerContextSourceAndAuthenticationBeans() {
        LdapConfiguration configuration = new LdapConfiguration();
        UnboundIdContainer container = configuration.ldapContainer();
        assertThat(container.getPort()).isZero();

        UnboundIdContainer runningContainer = mock(UnboundIdContainer.class);
        when(runningContainer.getPort()).thenReturn(12345);
        var contextSource = configuration.contextSource(runningContainer);
        assertThat(contextSource.getUrls()).containsExactly("ldap://localhost:12345/");

        UserDetailsManager userDetailsManager = configuration.userDetailsManager(mock(ContextSource.class));
        assertThat(userDetailsManager).isInstanceOf(LdapUserDetailsManager.class);

        BaseLdapPathContextSource baseContextSource = mock(BaseLdapPathContextSource.class);
        BindAuthenticator authenticator = configuration.authenticator(baseContextSource);
        assertThat(authenticator).isNotNull();
        assertThat(configuration.authenticationProvider(authenticator))
                .isInstanceOf(LdapAuthenticationProvider.class);
    }

    @Test
    void ldapConfigurationIsConditionalOnLdapProvider() {
        ConditionalOnProperty condition = LdapConfiguration.class.getAnnotation(ConditionalOnProperty.class);

        assertThat(condition.prefix()).isEqualTo("app.datasource");
        assertThat(condition.name()).containsExactly("provider");
        assertThat(condition.havingValue()).isEqualTo("ldap");
        assertThat(condition.matchIfMissing()).isFalse();
    }

    @Test
    void applicationMainDelegatesToSpringApplication() {
        String[] args = {"--spring.main.web-application-type=none"};
        try (var springApplication = mockStatic(SpringApplication.class)) {
            springApplication.when(() -> SpringApplication.run(IAMApplication.class, args))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            IAMApplication.main(args);

            springApplication.verify(() -> SpringApplication.run(IAMApplication.class, args));
        }
    }
}
