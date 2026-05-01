package com.github.leoreboucas.logisticmessaging.user;

import com.github.leoreboucas.logisticmessaging.infra.client.LogisticClient;
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

    public User register (CreateUserDTO createUserDTO) {
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

        return newUser;
    }
}
