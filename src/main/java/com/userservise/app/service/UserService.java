package com.userservise.app.service;

import com.userservise.app.model.dto.UpdateUserDto;
import com.userservise.app.model.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDto createUser(UserDto newUser);

    UserDto getUserById(Integer id);

    Page<UserDto> getAllUsers(String firstName, String surname, Pageable pageable);

    UserDto updateUser(Integer id, UpdateUserDto updatedUser);

    Boolean activateUser(Integer id);

    Boolean deactivateUser(Integer id);

    void deleteById(Integer id);
}
