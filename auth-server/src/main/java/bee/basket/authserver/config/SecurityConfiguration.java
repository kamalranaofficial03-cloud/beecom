package bee.basket.authserver.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

	@Bean
	Customizer<HttpSecurity> httpSecCustomizer() {
		return http -> http
				.cors(cors-> cors.configurationSource(corsConfigurationSource()))
				.authorizeHttpRequests(authz -> {
					authz.requestMatchers(HttpMethod.POST,"/user").permitAll();
				})
				.exceptionHandling((exceptions)-> exceptions.defaultAuthenticationEntryPointFor(
						new LoginUrlAuthenticationEntryPoint("/login"),
						new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
				))
				.formLogin(form-> form.loginPage("/login").permitAll())
				.logout((logout) -> logout
						.logoutUrl("/logout")
				);

	}

	@Bean
	Customizer<OAuth2AuthorizationServerConfigurer> authzServerConfigurer() {
		return authzServer -> {
		//	authzServer.clientRegistrationEndpoint(reg -> reg.openRegistrationAllowed(true));
			authzServer.oidc(oidc -> oidc.providerConfigurationEndpoint(
					pce -> pce.providerConfigurationCustomizer(config -> config.claim("conference", "Spring I/O 2026"))));
		};
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOrigins(List.of("http://localhost:3000", "http://localhost:9090"));
		config.setAllowedMethods(List.of("GET", "POST", "OPTIONS"));
		config.setAllowedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

}