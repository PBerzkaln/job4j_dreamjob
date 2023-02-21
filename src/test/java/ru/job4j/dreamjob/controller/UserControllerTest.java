package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    private UserService userService;

    private UserController userController;
    private HttpServletRequest httpServletRequest;
    private HttpSession httpSession;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        httpServletRequest = mock(HttpServletRequest.class);
        httpSession = mock(HttpSession.class);
        userController = new UserController(userService);
    }

    @Test
    public void whenRequestGetLoginPageThenReturnLoginPage() {
        var view = userController.getLoginPage();
        assertThat(view).isEqualTo("users/login");
    }

    @Test
    public void whenRequestGetRegisterPageThenReturnRegistrationPage() {
        var view = userController.getRegistrationPage();
        assertThat(view).isEqualTo("users/register");
    }

    @Test
    public void whenUserRegisterAndRedirectToVacanciesPage() {
        var user = new User(1, "user@mail.ru", "Vasya", "12345");
        var userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userArgumentCaptor.capture())).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var actualUser = userArgumentCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/vacancies");
        assertThat(actualUser).isEqualTo(user);
    }

    @Test
    public void whenUserRegisterGetSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Пользователь с такой почтой уже существует");
        when(userService.save(any())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(model, new User());
        var actualExceptionMessage = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenUserLogInAndRedirectToVacanciesPage() {
        var user = new User(1, "mail", "name", "123");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword()))
                .thenReturn(Optional.of(user));
        when(httpServletRequest.getSession()).thenReturn(httpSession);

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, httpServletRequest);

        assertThat(view).isEqualTo("redirect:/vacancies");
    }

    @Test
    public void whenUserLogInGetSomeExceptionThrownThenGetErrorPageWithMessage() {
        var expectedException = new RuntimeException("Почта или пароль введены неверно");
        var user = new User(1, "mail", "name", "123");
        when(userService.findByEmailAndPassword(user.getEmail(), user.getPassword()))
                .thenReturn(Optional.empty());
        when(httpServletRequest.getSession()).thenReturn(httpSession);

        var model = new ConcurrentModel();
        var view = userController.loginUser(user, model, httpServletRequest);
        var actualExceptionMessage = model.getAttribute("error");

        assertThat(view).isEqualTo("users/login");
        assertThat(actualExceptionMessage).isEqualTo(expectedException.getMessage());
    }

    @Test
    public void whenRequestLogoutUserThenRedirectLoginPage() {
        HttpSession httpSession = mock(HttpSession.class);
        var view = userController.logout(httpSession);
        assertThat(view).isEqualTo("redirect:/users/login");
    }
}