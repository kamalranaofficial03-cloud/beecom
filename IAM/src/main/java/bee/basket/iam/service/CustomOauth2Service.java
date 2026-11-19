package bee.basket.iam.service;

import bee.basket.iam.dao.RoleDao;
import bee.basket.iam.dao.UserDao;
import bee.basket.iam.dao.UserIdentityDao;
import bee.basket.iam.entity.RoleEntity;
import bee.basket.iam.entity.UserEntity;
import bee.basket.iam.entity.UserIdentity;
import bee.basket.commonutil.constant.IdentityProvider;
import bee.basket.commonutil.constant.RoleEnum;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ConditionalOnProperty(prefix = "app.bee.sso", name = "enabled", havingValue = "true")
public class CustomOauth2Service extends DefaultOAuth2UserService {
    private final UserDao userDao;
    private final UserIdentityDao userIdentityDao;
    private final RoleDao roleDao;

    public CustomOauth2Service(UserDao userDao, UserIdentityDao userIdentityDao, RoleDao roleDao) {
        this.userDao = userDao;
        this.userIdentityDao = userIdentityDao;
        this.roleDao = roleDao;
    }

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        IdentityProvider provider;
        String providerUserId;
        String email;
        String firstName;
        String lastName;

        if ("google".equals(registrationId)) {
            provider = IdentityProvider.GOOGLE;
            providerUserId = oauthUser.getAttribute("sub");
            email = oauthUser.getAttribute("email");
            firstName = oauthUser.getAttribute("given_name");
            lastName = oauthUser.getAttribute("family_name");
        } else if ("microsoft".equals(registrationId)) {
            provider = IdentityProvider.MICROSOFT;
            providerUserId = oauthUser.getAttribute("oid");
            if (providerUserId == null) {
                providerUserId = oauthUser.getAttribute("sub");
            }
            email = oauthUser.getAttribute("email");
            if (email == null) {
                email = oauthUser.getAttribute("preferred_username");
            }
            firstName = oauthUser.getAttribute("given_name");
            lastName = oauthUser.getAttribute("family_name");
        } else {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("unsupported_provider"),
                    "Unsupported OAuth2 provider: " + registrationId
            );
        }

        if (providerUserId == null || email == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info"),
                    "OAuth2 provider did not return a user identifier and email"
            );
        }

        UserIdentity identity = userIdentityDao
                .findByProviderAndProviderUserId(provider, providerUserId)
                .orElse(null);
        if (identity == null) {
            identity = createIdentity(provider, providerUserId, email, firstName, lastName);
        }
        if (identity.getUser() == null) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_identity"),
                    "OAuth2 identity is not linked to a user"
            );
        }
        if (!identity.isEnabled() || !identity.getUser().isEnabled()) {
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("disabled_user"),
                    "This user account is disabled"
            );
        }
        return oauthUser;
    }

    private UserIdentity createIdentity(
            IdentityProvider provider,
            String providerUserId,
            String email,
            String firstName,
            String lastName
    ) {
        UserEntity user = userDao.findByEmail(email).orElseGet(() -> createUser(email, firstName, lastName));
        UserIdentity identity = new UserIdentity();
        identity.setProvider(provider);
        identity.setProviderUserId(providerUserId);
        identity.setUsername(email);
        identity.setEnabled(true);
        user.addIdentity(identity);
        userDao.save(user);
        return identity;
    }

    private UserEntity createUser(String email, String firstName, String lastName) {
        RoleEntity userRole = roleDao.findByRole(RoleEnum.USER.value());
        if (userRole == null) {
            throw new IllegalStateException("The default user role is not configured");
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEnabled(true);
        user.setRole(java.util.Set.of(userRole));
        return userDao.save(user);
    }
}
