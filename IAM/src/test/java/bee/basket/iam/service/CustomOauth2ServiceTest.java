package bee.basket.iam.service;

import bee.basket.commonutil.constant.IdentityProvider;
import bee.basket.commonutil.constant.RoleEnum;
import bee.basket.iam.dao.RoleDao;
import bee.basket.iam.dao.UserDao;
import bee.basket.iam.dao.UserIdentityDao;
import bee.basket.iam.entity.RoleEntity;
import bee.basket.iam.entity.UserEntity;
import bee.basket.iam.entity.UserIdentity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class CustomOauth2ServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private UserIdentityDao userIdentityDao;

    @Mock
    private RoleDao roleDao;

    private CustomOauth2Service service;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        service = new CustomOauth2Service(userDao, userIdentityDao, roleDao);
        service.setRestOperations(restTemplate);
    }

    @Test
    void loadUserReturnsEnabledLinkedGoogleUser() {
        UserEntity user = enabledUser();
        UserIdentity identity = identity(IdentityProvider.GOOGLE, "google-123", user, true);
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(identity));
        expectUserInfo("{\"sub\":\"google-123\",\"email\":\"ada@example.com\","
                + "\"given_name\":\"Ada\",\"family_name\":\"Lovelace\"}");

        OAuth2User result = service.loadUser(request("google"));

        assertThat(result.getName()).isEqualTo("google-123");
        assertThat((String) result.getAttribute("email")).isEqualTo("ada@example.com");
        verify(userDao, never()).save(any());
    }

    @Test
    void loadUserCreatesGoogleIdentityForExistingEmailAccount() {
        UserEntity user = enabledUser();
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.empty());
        when(userDao.findByEmail("ada@example.com")).thenReturn(Optional.of(user));
        when(userDao.save(user)).thenReturn(user);
        expectUserInfo("{\"sub\":\"google-123\",\"email\":\"ada@example.com\","
                + "\"given_name\":\"Ada\",\"family_name\":\"Lovelace\"}");

        service.loadUser(request("google"));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userDao).save(userCaptor.capture());
        UserIdentity createdIdentity = userCaptor.getValue().getIdentities().getFirst();
        assertThat(createdIdentity.getProvider()).isEqualTo(IdentityProvider.GOOGLE);
        assertThat(createdIdentity.getProviderUserId()).isEqualTo("google-123");
        assertThat(createdIdentity.getUsername()).isEqualTo("ada@example.com");
        assertThat(createdIdentity.isEnabled()).isTrue();
        assertThat(createdIdentity.getUser()).isSameAs(user);
        verify(roleDao, never()).findByRole(any());
    }

    @Test
    void loadUserCreatesMicrosoftUserAndIdentityUsingSubAndPreferredUsernameFallbacks() {
        RoleEntity userRole = new RoleEntity();
        userRole.setRole(RoleEnum.USER.value());
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.MICROSOFT, "microsoft-sub"))
                .thenReturn(Optional.empty());
        when(userDao.findByEmail("ada@example.com")).thenReturn(Optional.empty());
        when(roleDao.findByRole(RoleEnum.USER.value())).thenReturn(userRole);
        when(userDao.save(any(UserEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        expectUserInfo("{\"sub\":\"microsoft-sub\",\"preferred_username\":\"ada@example.com\","
                + "\"given_name\":\"Ada\",\"family_name\":\"Lovelace\"}");

        service.loadUser(request("microsoft"));

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userDao, org.mockito.Mockito.times(2)).save(userCaptor.capture());
        UserEntity createdUser = userCaptor.getAllValues().getFirst();
        assertThat(createdUser.getEmail()).isEqualTo("ada@example.com");
        assertThat(createdUser.getFirstName()).isEqualTo("Ada");
        assertThat(createdUser.getLastName()).isEqualTo("Lovelace");
        assertThat(createdUser.isEnabled()).isTrue();
        assertThat(createdUser.getRole()).containsExactly(userRole);
        UserIdentity identity = createdUser.getIdentities().getFirst();
        assertThat(identity.getProvider()).isEqualTo(IdentityProvider.MICROSOFT);
        assertThat(identity.getProviderUserId()).isEqualTo("microsoft-sub");
    }

    @Test
    void loadUserPrefersMicrosoftOidAndEmailAttributesWhenPresent() {
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.MICROSOFT, "microsoft-oid"))
                .thenReturn(Optional.of(identity(IdentityProvider.MICROSOFT, "microsoft-oid", enabledUser(), true)));
        expectUserInfo("{\"sub\":\"fallback-sub\",\"oid\":\"microsoft-oid\","
                + "\"email\":\"primary@example.com\",\"preferred_username\":\"fallback@example.com\"}");

        OAuth2User user = service.loadUser(request("microsoft"));

        assertThat((String) user.getAttribute("oid")).isEqualTo("microsoft-oid");
        verify(userIdentityDao).findByProviderAndProviderUserId(IdentityProvider.MICROSOFT, "microsoft-oid");
    }

    @Test
    void loadUserRejectsUnsupportedProvider() {
        expectUserInfo("{\"sub\":\"external-123\",\"email\":\"ada@example.com\"}");

        assertThatThrownBy(() -> service.loadUser(request("unknown")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(exception -> assertThat(((OAuth2AuthenticationException) exception).getError().getErrorCode())
                        .isEqualTo("unsupported_provider"));
    }

    @Test
    void loadUserRejectsProviderResponseMissingIdentifierOrEmail() {
        expectUserInfo("{\"sub\":\"provider-123\"}");

        assertThatThrownBy(() -> service.loadUser(request("google")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(exception -> assertThat(((OAuth2AuthenticationException) exception).getError().getErrorCode())
                        .isEqualTo("invalid_user_info"));
    }

    @Test
    void loadUserRejectsProviderResponseMissingIdentifierEvenWhenEmailIsPresent() {
        expectUserInfo("{\"name\":\"Ada\",\"email\":\"ada@example.com\"}");

        assertThatThrownBy(() -> service.loadUser(request("google", "name")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(exception -> assertThat(((OAuth2AuthenticationException) exception).getError().getErrorCode())
                        .isEqualTo("invalid_user_info"));
    }

    @Test
    void loadUserRejectsUnlinkedIdentity() {
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(identity(IdentityProvider.GOOGLE, "google-123", null, true)));
        expectUserInfo("{\"sub\":\"google-123\",\"email\":\"ada@example.com\"}");

        assertThatThrownBy(() -> service.loadUser(request("google")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(exception -> assertThat(((OAuth2AuthenticationException) exception).getError().getErrorCode())
                        .isEqualTo("invalid_user_identity"));
    }

    @Test
    void loadUserRejectsDisabledIdentity() {
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(identity(IdentityProvider.GOOGLE, "google-123", enabledUser(), false)));
        expectUserInfo("{\"sub\":\"google-123\",\"email\":\"ada@example.com\"}");

        assertThatThrownBy(() -> service.loadUser(request("google")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(exception -> assertThat(((OAuth2AuthenticationException) exception).getError().getErrorCode())
                        .isEqualTo("disabled_user"));
    }

    @Test
    void loadUserRejectsDisabledParentUser() {
        UserEntity disabledUser = enabledUser();
        disabledUser.setEnabled(false);
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.of(identity(IdentityProvider.GOOGLE, "google-123", disabledUser, true)));
        expectUserInfo("{\"sub\":\"google-123\",\"email\":\"ada@example.com\"}");

        assertThatThrownBy(() -> service.loadUser(request("google")))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(exception -> assertThat(((OAuth2AuthenticationException) exception).getError().getErrorCode())
                        .isEqualTo("disabled_user"));
    }

    @Test
    void loadUserFailsWhenDefaultRoleIsMissingForNewAccount() {
        when(userIdentityDao.findByProviderAndProviderUserId(IdentityProvider.GOOGLE, "google-123"))
                .thenReturn(Optional.empty());
        when(userDao.findByEmail("ada@example.com")).thenReturn(Optional.empty());
        when(roleDao.findByRole(RoleEnum.USER.value())).thenReturn(null);
        expectUserInfo("{\"sub\":\"google-123\",\"email\":\"ada@example.com\"}");

        assertThatThrownBy(() -> service.loadUser(request("google")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("The default user role is not configured");
        verify(userDao, never()).save(any());
    }

    private OAuth2UserRequest request(String registrationId) {
        return request(registrationId, "sub");
    }

    private OAuth2UserRequest request(String registrationId, String usernameAttribute) {
        ClientRegistration registration = ClientRegistration.withRegistrationId(registrationId)
                .clientId("client")
                .clientSecret("secret")
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost/callback")
                .scope("profile")
                .authorizationUri("https://provider.example/auth")
                .tokenUri("https://provider.example/token")
                .userInfoUri("https://provider.example/userinfo")
                .userNameAttributeName(usernameAttribute)
                .clientName(registrationId)
                .build();
        OAuth2AccessToken token = new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "access-token",
                Instant.now().minusSeconds(1),
                Instant.now().plusSeconds(300)
        );
        return new OAuth2UserRequest(registration, token);
    }

    private void expectUserInfo(String json) {
        server.expect(requestTo("https://provider.example/userinfo"))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    private static UserEntity enabledUser() {
        UserEntity user = new UserEntity();
        user.setEnabled(true);
        user.setEmail("ada@example.com");
        return user;
    }

    private static UserIdentity identity(
            IdentityProvider provider,
            String providerUserId,
            UserEntity user,
            boolean enabled
    ) {
        UserIdentity identity = new UserIdentity();
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setUsername("ada@example.com");
        identity.setUser(user);
        identity.setEnabled(enabled);
        return identity;
    }
}
