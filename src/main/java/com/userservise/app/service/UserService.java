package com.userservise.app.service;

import com.userservise.app.model.dto.UserRequest;
import com.userservise.app.model.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    UserDto createUser(UserRequest request);

    UserDto getUserById(Integer id);

    Page<UserDto> getAllUsers(String firstName, String surname, Pageable pageable);

    UserDto updateUser(Integer id, UserRequest updatedUser);

    UserDto activateUser(Integer id);

    UserDto deactivateUser(Integer id);

    void deleteById(Integer id);
}
