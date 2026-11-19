package bee.basket.iam.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.KeyType;
import com.nimbusds.jose.jwk.JWKSelector;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;
import java.util.HashMap;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mockStatic;
import static org.springframework.security.oauth2.server.authorization.OAuth2TokenType.ACCESS_TOKEN;

class SecurityConfigTest {

    @Test
    void corsConfigurationAllowsConfiguredOriginsMethodsHeadersAndCredentials() {
        SecurityConfig configuration = new SecurityConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(
                configuration, "frontendUri", "https://frontend.example"
        );

        HttpServletRequest request = new MockHttpServletRequest("GET", "/users");
        CorsConfiguration cors = configuration.corsConfigurationSource().getCorsConfiguration(request);

        assertThat(cors.getAllowedOrigins()).containsExactly("https://frontend.example", "http://localhost:9090");
        assertThat(cors.getAllowedMethods()).containsExactly("GET", "POST", "OPTIONS", "PUT");
        assertThat(cors.getAllowedHeaders()).containsExactly("*");
        assertThat(cors.getAllowCredentials()).isTrue();
    }

    @Test
    void passwordEncoderProducesVerifiableEncodedPasswords() {
        PasswordEncoder encoder = new SecurityConfig().passwordEncoder();

        String encoded = encoder.encode("secret");

        assertThat(encoded).startsWith("{bcrypt}");
        assertThat(encoder.matches("secret", encoded)).isTrue();
        assertThat(encoder.matches("wrong", encoded)).isFalse();
    }

    @Test
    void registeredClientRepositoryUsesProvidedJdbcOperations() {
        JdbcOperations jdbcOperations = mock(JdbcOperations.class);

        assertThat(new SecurityConfig().registeredClientRepository(jdbcOperations))
                .isInstanceOf(JdbcRegisteredClientRepository.class);
    }

    @Test
    void registeredClientInitializerSavesMissingDefaultClients() throws Exception {
        var repository = mock(org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repository.findByClientId(any())).thenReturn(null);
        when(encoder.encode("resource-server-secret")).thenReturn("encoded-resource-secret");
        when(encoder.encode("demo-secret")).thenReturn("encoded-demo-secret");

        new SecurityConfig().registeredClientInitializer(repository, encoder).run(mock(ApplicationArguments.class));

        var clients = org.mockito.ArgumentCaptor.forClass(RegisteredClient.class);
        verify(repository, org.mockito.Mockito.times(3)).save(clients.capture());
        assertThat(clients.getAllValues()).extracting(RegisteredClient::getClientId)
                .containsExactlyInAnyOrder("frontend-client", "resource-server-client", "demo-client");
        RegisteredClient frontend = clients.getAllValues().stream()
                .filter(client -> client.getClientId().equals("frontend-client"))
                .findFirst().orElseThrow();
        assertThat(frontend.getRedirectUris()).containsExactly("http://localhost:4200/callback");
        assertThat(frontend.getPostLogoutRedirectUris()).containsExactly("http://localhost:4200");
        assertThat(frontend.getClientSecret()).isNull();

        RegisteredClient resourceServer = clients.getAllValues().stream()
                .filter(client -> client.getClientId().equals("resource-server-client"))
                .findFirst().orElseThrow();
        assertThat(resourceServer.getClientSecret()).isEqualTo("encoded-resource-secret");
        assertThat(resourceServer.getAuthorizationGrantTypes())
                .containsExactly(AuthorizationGrantType.CLIENT_CREDENTIALS);

        RegisteredClient demo = clients.getAllValues().stream()
                .filter(client -> client.getClientId().equals("demo-client"))
                .findFirst().orElseThrow();
        assertThat(demo.getRedirectUris())
                .containsExactlyInAnyOrder(
                        "http://localhost:8080/login/oauth2/code/demo-client",
                        "http://localhost:9090/login/oauth2/code/demo-client"
                );
        assertThat(demo.getClientSecret()).isEqualTo("encoded-demo-secret");
    }

    @Test
    void registeredClientInitializerLeavesExistingClientsUntouched() throws Exception {
        var repository = mock(org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository.class);
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(repository.findByClientId(any())).thenReturn(mock(RegisteredClient.class));

        new SecurityConfig().registeredClientInitializer(repository, encoder).run(mock(ApplicationArguments.class));

        verify(repository, never()).save(any());
        verify(repository).findByClientId("frontend-client");
        verify(repository).findByClientId("resource-server-client");
        verify(repository).findByClientId("demo-client");
    }

    @Test
    void jwkSourceContainsAnRsaSigningKeyWithPrivateMaterial() throws Exception {
        List<JWK> keys = new SecurityConfig().jwkSource().get(
                new JWKSelector(new JWKMatcher.Builder().keyType(KeyType.RSA).build()),
                null
        );

        assertThat(keys).hasSize(1);
        assertThat(keys.getFirst().getKeyType().getValue()).isEqualTo("RSA");
        assertThat(keys.getFirst().isPrivate()).isTrue();
        assertThat(keys.getFirst().getKeyID()).isNotBlank();
    }

    @Test
    void jwkSourceWrapsKeyGenerationFailures() {
        KeyPairGenerator keyPairGenerator = mock(KeyPairGenerator.class);
        try (var keyPairGeneratorMock = mockStatic(KeyPairGenerator.class)) {
            keyPairGeneratorMock.when(() -> KeyPairGenerator.getInstance("RSA"))
                    .thenReturn(keyPairGenerator);
            doThrow(new IllegalArgumentException("key generation failed"))
                    .when(keyPairGenerator).initialize(2048);

            assertThatThrownBy(() -> new SecurityConfig().jwkSource())
                    .isInstanceOf(IllegalStateException.class)
                    .hasCauseInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void jwtDecoderIsCreatedFromTheJwkSource() {
        SecurityConfig configuration = new SecurityConfig();

        assertThat(configuration.jwtDecoder(configuration.jwkSource())).isNotNull();
    }

    @Test
    void authorizationServerSettingsUsesConfiguredIssuer() {
        SecurityConfig configuration = new SecurityConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(
                configuration, "issuerUri", "https://issuer.example"
        );

        AuthorizationServerSettings settings = configuration.authorizationServerSettings();

        assertThat(settings.getIssuer()).isEqualTo("https://issuer.example");
    }

    @Test
    void tokenCustomizerAddsRolesAndPermissionsOnlyForAccessTokens() {
        OAuth2TokenCustomizer<JwtEncodingContext> customizer = new SecurityConfig().tokenCustomizer();
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        JwtClaimsSet.Builder claims = mock(JwtClaimsSet.Builder.class);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "ada",
                "n/a",
                List.of(new SimpleGrantedAuthority("ROLE_USER"), new SimpleGrantedAuthority("read"))
        );
        when(context.getTokenType()).thenReturn(ACCESS_TOKEN);
        when(context.getPrincipal()).thenReturn(authentication);
        when(context.getClaims()).thenReturn(claims);

        customizer.customize(context);

        verify(claims).claim("roles", List.of("ROLE_USER", "read"));
        verify(claims).claim("permission", "write,read");
    }

    @Test
    void tokenCustomizerAddsPermissionWithoutRolesWhenAuthoritiesAreNull() {
        OAuth2TokenCustomizer<JwtEncodingContext> customizer = new SecurityConfig().tokenCustomizer();
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        JwtClaimsSet.Builder claims = mock(JwtClaimsSet.Builder.class);
        Authentication authentication = mock(Authentication.class);
        when(context.getTokenType()).thenReturn(ACCESS_TOKEN);
        when(context.getPrincipal()).thenReturn(authentication);
        when(context.getClaims()).thenReturn(claims);
        when(authentication.getAuthorities()).thenReturn(null);
        when(authentication.getName()).thenReturn("ada");

        customizer.customize(context);

        verify(claims, never()).claim(org.mockito.ArgumentMatchers.eq("roles"), any());
        verify(claims).claim("permission", "write,read");
    }

    @Test
    void tokenCustomizerHandlesMissingPrincipal() {
        OAuth2TokenCustomizer<JwtEncodingContext> customizer = new SecurityConfig().tokenCustomizer();
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        JwtClaimsSet.Builder claims = mock(JwtClaimsSet.Builder.class);
        when(context.getTokenType()).thenReturn(ACCESS_TOKEN);
        when(context.getPrincipal()).thenReturn(null);
        when(context.getClaims()).thenReturn(claims);

        customizer.customize(context);

        verify(claims, never()).claim(org.mockito.ArgumentMatchers.eq("roles"), any());
        verify(claims).claim("permission", "write,read");
    }

    @Test
    void tokenCustomizerDoesNotModifyNonAccessTokenClaims() {
        OAuth2TokenCustomizer<JwtEncodingContext> customizer = new SecurityConfig().tokenCustomizer();
        JwtEncodingContext context = mock(JwtEncodingContext.class);
        when(context.getTokenType()).thenReturn(new OAuth2TokenType("refresh_token"));

        customizer.customize(context);

        verify(context, never()).getPrincipal();
        verify(context, never()).getClaims();
    }

    @Test
    void httpSecurityCustomizerAppliesSettingsWhenOptionalOauthBeansAreAbsent() {
        var beanFactory = new org.springframework.beans.factory.support.DefaultListableBeanFactory();
        var applicationContext = new org.springframework.context.support.GenericApplicationContext();
        applicationContext.refresh();
        SecurityConfig configuration = new SecurityConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(
                configuration, "frontendUri", "http://localhost:4200"
        );
        ObjectPostProcessor<Object> postProcessor = objectPostProcessor();
        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(ApplicationContext.class, applicationContext);
        sharedObjects.put(PathPatternRequestMatcher.Builder.class, PathPatternRequestMatcher.withDefaults());
        HttpSecurity http = new HttpSecurity(
                postProcessor,
                new AuthenticationManagerBuilder(postProcessor),
                sharedObjects
        );

        configuration.httpSecurityFilterChain(
                beanFactory.getBeanProvider(ClientRegistrationRepository.class),
                beanFactory.getBeanProvider(bee.basket.iam.service.CustomOauth2Service.class)
        ).customize(http);

        assertThat(http).isNotNull();
    }

    @Test
    void httpSecurityCustomizerConfiguresOauthLoginWhenBothOptionalBeansArePresent() {
        var beanFactory = new org.springframework.beans.factory.support.DefaultListableBeanFactory();
        beanFactory.registerSingleton("clientRegistrationRepository", mock(ClientRegistrationRepository.class));
        beanFactory.registerSingleton(
                "customOauth2Service",
                mock(bee.basket.iam.service.CustomOauth2Service.class)
        );
        var applicationContext = new org.springframework.context.support.GenericApplicationContext();
        applicationContext.refresh();
        SecurityConfig configuration = new SecurityConfig();
        org.springframework.test.util.ReflectionTestUtils.setField(
                configuration, "frontendUri", "http://localhost:4200"
        );
        ObjectPostProcessor<Object> postProcessor = objectPostProcessor();
        HashMap<Class<?>, Object> sharedObjects = new HashMap<>();
        sharedObjects.put(ApplicationContext.class, applicationContext);
        sharedObjects.put(PathPatternRequestMatcher.Builder.class, PathPatternRequestMatcher.withDefaults());
        HttpSecurity http = new HttpSecurity(
                postProcessor,
                new AuthenticationManagerBuilder(postProcessor),
                sharedObjects
        );

        configuration.httpSecurityFilterChain(
                beanFactory.getBeanProvider(ClientRegistrationRepository.class),
                beanFactory.getBeanProvider(bee.basket.iam.service.CustomOauth2Service.class)
        ).customize(http);

        assertThat(http).isNotNull();
    }

    private static ObjectPostProcessor<Object> objectPostProcessor() {
        return new ObjectPostProcessor<>() {
            @Override
            public <T> T postProcess(T object) {
                return object;
            }
        };
    }
}
