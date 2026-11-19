package bee.basket.iam.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginControllerTest {

    @Test
    void loginAddsRequestContextPathAndReturnsLoginView() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        Model model = mock(Model.class);
        when(request.getContextPath()).thenReturn("/iam");

        String view = new LoginController().login(request, model);

        assertThat(view).isEqualTo("login");
        verify(model).addAttribute("contextPath", "/iam");
    }

    @Test
    void logoutReturnsLogoutView() {
        assertThat(new LoginController().logout()).isEqualTo("logout");
    }
}
