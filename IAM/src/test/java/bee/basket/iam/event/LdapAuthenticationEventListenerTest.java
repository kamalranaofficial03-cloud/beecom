package bee.basket.iam.event;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.InetOrgPerson;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.provisioning.UserDetailsManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LdapAuthenticationEventListenerTest {

    @Test
    void ignoresAuthenticationWhenPrincipalIsNotOidcUser() {
        UserDetailsManager manager = mock(UserDetailsManager.class);
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(
                new UsernamePasswordAuthenticationToken("local-user", "password")
        );

        new LdapAuthenticationEventListener(manager).onAuthenticationSuccess(event);

        verify(manager, never()).userExists(any());
        verify(manager, never()).createUser(any());
    }

    @Test
    void doesNotCreateLdapUserWhenOidcUserAlreadyExists() {
        UserDetailsManager manager = mock(UserDetailsManager.class);
        OidcUser oidcUser = oidcUser("ada", "subject-123");
        when(manager.userExists("ada")).thenReturn(true);

        new LdapAuthenticationEventListener(manager).onAuthenticationSuccess(successEvent(oidcUser));

        verify(manager).userExists("ada");
        verify(manager, never()).createUser(any());
    }

    @Test
    void createsLdapUserWithExternalAuthorityAndGeneratedPasswordWhenMissing() {
        UserDetailsManager manager = mock(UserDetailsManager.class);
        OidcUser oidcUser = oidcUser("ada", "subject-123");
        when(manager.userExists("ada")).thenReturn(false);

        new LdapAuthenticationEventListener(manager).onAuthenticationSuccess(successEvent(oidcUser));

        var userCaptor = org.mockito.ArgumentCaptor.forClass(UserDetails.class);
        verify(manager).createUser(userCaptor.capture());
        UserDetails createdUser = userCaptor.getValue();
        assertThat(createdUser.getUsername()).isEqualTo("ada");
        assertThat(createdUser.getAuthorities()).extracting("authority").containsExactly("EXTERNAL_USER");
        assertThat(createdUser.getPassword()).isNotBlank();
        InetOrgPerson person = (InetOrgPerson) createdUser;
        assertThat(person.getUid()).isEqualTo("subject-123");
        assertThat(person.getDn()).isEqualTo("uid=ada,ou=people,dc=springframework,dc=org");
    }

    private static OidcUser oidcUser(String username, String subject) {
        OidcUser user = mock(OidcUser.class);
        when(user.getName()).thenReturn(username);
        when(user.getSubject()).thenReturn(subject);
        return user;
    }

    private static AuthenticationSuccessEvent successEvent(OidcUser user) {
        return new AuthenticationSuccessEvent(
                new UsernamePasswordAuthenticationToken(user, "token", user.getAuthorities())
        );
    }
}
