package com.camsell.identity.domain.port.in;

import com.camsell.identity.domain.model.User;
import com.camsell.identity.domain.model.UserRole;

import java.util.List;

public interface UserUseCase {

    User getById(Long id);

    User updateProfile(Long id, UpdateProfileCommand command);

    void changePassword(Long id, String currentPassword, String newPassword);

    List<User> listUsers();

    User changeRole(Long id, UserRole role);

    record UpdateProfileCommand(String fullName, String phone, String cityCode, String avatarUrl) {
    }
}
