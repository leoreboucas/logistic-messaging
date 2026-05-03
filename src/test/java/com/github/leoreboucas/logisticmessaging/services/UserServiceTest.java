package com.github.leoreboucas.logisticmessaging.services;

import com.github.leoreboucas.logisticmessaging.auth.LoginUserDTO;
import com.github.leoreboucas.logisticmessaging.infra.client.LogisticClient;
import com.github.leoreboucas.logisticmessaging.infra.exception.AuthenticationException;
import com.github.leoreboucas.logisticmessaging.infra.exception.BusinessException;
import com.github.leoreboucas.logisticmessaging.infra.security.JwtService;
import com.github.leoreboucas.logisticmessaging.user.DTO.CreateUserDTO;
import com.github.leoreboucas.logisticmessaging.user.User;
import com.github.leoreboucas.logisticmessaging.user.UserRepository;
import com.github.leoreboucas.logisticmessaging.user.UserRole;
import com.github.leoreboucas.logisticmessaging.user.UserService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private LogisticClient logisticClient;
    @Mock
    private JwtService jwtService;

    private final String document = "01234567890";
    private final UserRole role = UserRole.CUSTOMER;

    @InjectMocks
    private UserService userService;

    @Nested
    class RegisterTests {
        @Test
        void notFoundUserOnLogistic () {
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    "firstName",
                    "secondName",
                    "000.000.000-00",
                    "useremail@hotmail.com",
                    "#Password123",
                    role
                    );
            doThrow(new BusinessException("Usuário não encontrado no sistema logistic."))
                    .when(logisticClient).confirmUserValidity("00000000000", String.valueOf(createUserDTO.role()));

            assertThrows(BusinessException.class, () -> userService.register(createUserDTO));

            verify(logisticClient).confirmUserValidity("00000000000", String.valueOf(createUserDTO.role()));
        }

        @Test
        void userAlreadyExists () {
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    "firstName",
                    "secondName",
                    "000.000.000-00",
                    "useremail@hotmail.com",
                    "#Password123",
                    role
            );

            doNothing().when(logisticClient).confirmUserValidity("00000000000", String.valueOf(createUserDTO.role()));

            when(userRepository.findByDocument("00000000000")).thenReturn(new User());

            assertThrows(BusinessException.class, () -> userService.register(createUserDTO));

            verify(logisticClient).confirmUserValidity("00000000000", String.valueOf(createUserDTO.role()));
            verify(userRepository).findByDocument("00000000000");
        }

        @Test
        void successOnCreateUser () {
            CreateUserDTO createUserDTO = new CreateUserDTO(
                    "firstName",
                    "secondName",
                    "000.000.000-00",
                    "useremail@hotmail.com",
                    "#Password123",
                    role
            );

            doNothing().when(logisticClient).confirmUserValidity("00000000000", String.valueOf(createUserDTO.role()));

            when(userRepository.findByDocument("00000000000")).thenReturn(null);

            when(userRepository.save(any(User.class))).thenReturn(null);

            assertDoesNotThrow(() -> userService.register(createUserDTO));


            verify(logisticClient).confirmUserValidity("00000000000", String.valueOf(createUserDTO.role()));
            verify(userRepository).findByDocument("00000000000");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    class LoginTests {
        @Test
        void userNotAuthenticated () {
            LoginUserDTO loginUserDTO = new LoginUserDTO("000.000.000-00", "#Password123");
            when(userRepository.findByDocument("00000000000")).thenReturn(null);

            assertThrows(AuthenticationException.class, () -> userService.login(loginUserDTO));

            verify(userRepository).findByDocument("00000000000");
        }

        @Test
        void wrongPassword() {
            LoginUserDTO loginUserDTO = new LoginUserDTO("000.000.000-00", "#Password123");
            User user = new User();
            user.setDocument("00000000000");
            user.setPassword("hashString");
            when(userRepository.findByDocument("00000000000")).thenReturn(user);
            when(passwordEncoder.matches(loginUserDTO.password(), user.getPassword())).thenReturn(false);

            assertThrows(AuthenticationException.class, () -> userService.login(loginUserDTO));

            verify(userRepository).findByDocument("00000000000");
            verify(passwordEncoder).matches(loginUserDTO.password(), user.getPassword());
        }

        @Test
        void successOnLogin () {
            LoginUserDTO loginUserDTO = new LoginUserDTO("000.000.000-00", "#Password123");
            User user = new User();
            user.setDocument("00000000000");
            user.setPassword("hashString");
            user.setRole(UserRole.CUSTOMER);
            when(userRepository.findByDocument("00000000000")).thenReturn(user);
            when(passwordEncoder.matches(loginUserDTO.password(), user.getPassword())).thenReturn(true);
            when(jwtService.generateToken(user.getDocument(), String.valueOf(user.getRole()))).thenReturn("token");

            String result = userService.login(loginUserDTO);
            assertEquals("token", result);

            verify(userRepository).findByDocument("00000000000");
            verify(passwordEncoder).matches(loginUserDTO.password(), user.getPassword());
            verify(jwtService).generateToken(user.getDocument(), String.valueOf(user.getRole()));
        }
    }
}
