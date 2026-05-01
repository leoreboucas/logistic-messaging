package com.github.leoreboucas.logisticmessaging.user;

import com.github.leoreboucas.logisticmessaging.auth.LoginUserDTO;
import com.github.leoreboucas.logisticmessaging.infra.client.LogisticClient;
import com.github.leoreboucas.logisticmessaging.infra.security.JwtService;
import com.github.leoreboucas.logisticmessaging.user.DTO.CreateUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LogisticClient logisticClient;
    private final JwtService jwtService;

    public void register (CreateUserDTO createUserDTO) {
        try {
            logisticClient.confirmUserValidity(createUserDTO.document(), String.valueOf(createUserDTO.role()));
        } catch (HttpClientErrorException.NotFound e) {
            throw new IllegalArgumentException("Usuário não encontrado no sistema logistic.");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao comunicar com o serviço logistic.");
        }

        Optional.ofNullable(userRepository.findByDocument(createUserDTO.document()))
                .ifPresent(u -> {;
                    throw new IllegalArgumentException("Usuário já está cadastrado.");
                });

        User newUser = new User();
        newUser.setDocument(createUserDTO.document());
        newUser.setFirstName(createUserDTO.firstName());
        newUser.setSecondName(createUserDTO.secondName());
        newUser.setEmail(createUserDTO.email());
        newUser.setRole(createUserDTO.role());
        newUser.setPassword(passwordEncoder.encode(createUserDTO.password()));

        userRepository.save(newUser);

    }

    public String login (LoginUserDTO loginUserDTO) {
        User user = Optional.ofNullable(userRepository.findByDocument(loginUserDTO.document()))
                .orElseThrow(() -> new IllegalArgumentException("Usuário e/ou senha incorretos."));

        if(passwordEncoder.matches(loginUserDTO.password(), user.getPassword())) {
            return jwtService.generateToken(user.getDocument(), String.valueOf(user.getRole()));
        } else {
            throw new IllegalArgumentException("Usuário e/ou senha incorretos.");
        }
    }
}
