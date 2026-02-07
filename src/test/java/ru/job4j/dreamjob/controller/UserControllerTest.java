package ru.job4j.dreamjob.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ui.ConcurrentModel;
import ru.job4j.dreamjob.model.User;
import ru.job4j.dreamjob.service.UserService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserControllerTest {

    private UserService userService;

    private UserController userController;

    @BeforeEach
    public void initServices() {
        userService = mock(UserService.class);
        userController = new UserController(userService);
    }

    @Test
    void whenPostUserThenGetRootPathAndUserIsSaved() {
        var user = new User(1, "TestEmail", "TestName", "Password");
        var userCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userCaptor.capture())).thenReturn(Optional.of(user));

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var expectedUser = userCaptor.getValue();

        assertThat(view).isEqualTo("redirect:/");
        assertThat(expectedUser).usingRecursiveAssertion().isEqualTo(user);
    }

    @Test
    void whenPostUserThenGetError() {
        var user = new User(1, "TestEmail", "TestName", "Password");
        var userCaptor = ArgumentCaptor.forClass(User.class);
        when(userService.save(userCaptor.capture())).thenReturn(Optional.empty());

        var model = new ConcurrentModel();
        var view = userController.register(model, user);
        var expectedUser = userCaptor.getValue();
        var message = model.getAttribute("message");

        assertThat(view).isEqualTo("errors/404");
        assertThat(expectedUser).usingRecursiveAssertion().isEqualTo(user);
        assertThat(message).isEqualTo("Пользователь с такой почтой уже существует");
    }
}