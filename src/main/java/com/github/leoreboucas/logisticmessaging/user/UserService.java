package com.github.leoreboucas.logisticmessaging.user;

import com.github.leoreboucas.logisticmessaging.auth.LoginUserDTO;
import com.github.leoreboucas.logisticmessaging.infra.client.LogisticClient;
import com.github.leoreboucas.logisticmessaging.infra.exception.AuthenticationException;
import com.github.leoreboucas.logisticmessaging.infra.exception.BusinessException;
import com.github.leoreboucas.logisticmessaging.infra.exception.NotFoundException;
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
            logisticClient.confirmUserValidity(documentFormatted(createUserDTO.document()), String.valueOf(createUserDTO.role()));
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("Usuário não encontrado no sistema logistic.");
        } catch (Exception e) {
            throw new BusinessException("Erro ao comunicar com o serviço logistic.");
        }

        Optional.ofNullable(userRepository.findByDocument(documentFormatted(createUserDTO.document())))
                .ifPresent(u -> {
                    throw new BusinessException("Usuário já está cadastrado.");
                });

        User newUser = new User();
        newUser.setDocument(documentFormatted(createUserDTO.document()));
        newUser.setFirstName(createUserDTO.firstName());
        newUser.setSecondName(createUserDTO.secondName());
        newUser.setEmail(createUserDTO.email());
        newUser.setRole(createUserDTO.role());
        newUser.setPassword(passwordEncoder.encode(createUserDTO.password()));

        userRepository.save(newUser);

    }

    public String login (LoginUserDTO loginUserDTO) {

        User user = Optional.ofNullable(userRepository.findByDocument(documentFormatted(loginUserDTO.document())))
                .orElseThrow(() -> new AuthenticationException("Usuário e/ou senha incorretos."));

        if(passwordEncoder.matches(loginUserDTO.password(), user.getPassword())) {
            return jwtService.generateToken(user.getDocument(), String.valueOf(user.getRole()));
        } else {
            throw new AuthenticationException("Usuário e/ou senha incorretos.");
        }
    }

    private String documentFormatted (String document) {
        return document.trim()
                .replace(".", "")
                .replace("-", "")
                .replace("/", "");
    }
}
