package com.camsell.identity.infrastructure.web;

import com.camsell.identity.domain.model.UserRole;
import com.camsell.identity.domain.port.in.UserUseCase;
import com.camsell.identity.infrastructure.web.dto.UserResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {

    private final UserUseCase users;

    public UserAdminController(UserUseCase users) {
        this.users = users;
    }

    @GetMapping
    public List<UserResponse> list() {
        return users.listUsers().stream().map(UserResponse::from).toList();
    }

    @PutMapping("/{id}/role")
    public UserResponse changeRole(@PathVariable Long id, @RequestParam UserRole role) {
        return UserResponse.from(users.changeRole(id, role));
    }
}
