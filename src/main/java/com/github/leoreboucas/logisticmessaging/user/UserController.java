package com.github.leoreboucas.logisticmessaging.user;

import com.github.leoreboucas.logisticmessaging.user.DTO.CreateUserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUser(@RequestBody @Valid CreateUserDTO createUserDTO) {
        userService.register(createUserDTO);
    }
}
