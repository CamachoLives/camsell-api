package com.camsell.identity.infrastructure.web;

import com.camsell.identity.domain.port.in.AuthenticationUseCase;
import com.camsell.identity.domain.port.in.CurrentUserPort;
import com.camsell.identity.domain.port.in.UserUseCase;
import com.camsell.identity.infrastructure.web.dto.AuthPayloads;
import com.camsell.identity.infrastructure.web.dto.SessionResponse;
import com.camsell.identity.infrastructure.web.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationUseCase authentication;
    private final UserUseCase users;
    private final CurrentUserPort currentUser;

    public AuthController(AuthenticationUseCase authentication, UserUseCase users, CurrentUserPort currentUser) {
        this.authentication = authentication;
        this.users = users;
        this.currentUser = currentUser;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse register(@Valid @RequestBody AuthPayloads.RegisterPayload payload) {
        return SessionResponse.from(authentication.register(new AuthenticationUseCase.RegisterCommand(
                payload.email(),
                payload.password(),
                payload.fullName(),
                payload.phone(),
                payload.cityCode(),
                payload.role())));
    }

    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody AuthPayloads.LoginPayload payload) {
        return SessionResponse.from(authentication.login(
                new AuthenticationUseCase.LoginCommand(payload.email(), payload.password())));
    }

    @GetMapping("/me")
    public UserResponse me() {
        return UserResponse.from(users.getById(currentUser.requireCurrentUserId()));
    }

    @PutMapping("/me")
    public UserResponse updateProfile(@Valid @RequestBody AuthPayloads.UpdateProfilePayload payload) {
        return UserResponse.from(users.updateProfile(
                currentUser.requireCurrentUserId(),
                new UserUseCase.UpdateProfileCommand(
                        payload.fullName(), payload.phone(), payload.cityCode(), payload.avatarUrl())));
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@Valid @RequestBody AuthPayloads.ChangePasswordPayload payload) {
        users.changePassword(
                currentUser.requireCurrentUserId(), payload.currentPassword(), payload.newPassword());
    }
}
