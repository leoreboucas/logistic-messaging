package com.github.leoreboucas.logisticmessaging.auth;

import com.github.leoreboucas.logisticmessaging.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/login")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @PostMapping
    public String loginUser(@RequestBody @Valid  LoginUserDTO loginUserDTO) {
        return userService.login(loginUserDTO);
    }

}
