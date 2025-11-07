package com.userservise.app.controllers;

import com.userservise.app.model.dto.UserRequest;
import com.userservise.app.model.dto.UserDto;
import com.userservise.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Integer id) {
        log.info("Received request to fetch user with ID: {}", id);
        UserDto userDto = userService.getUserById(id);
        log.debug("Fetched user data: {}", userDto);

        return ResponseEntity.ok(userDto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String surname,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        log.info("Received request to fetch all users");
        Pageable pageable = PageRequest.of(page, limit);
        Page<UserDto> response = userService.getAllUsers(firstName, surname, pageable);

        log.debug("Fetched user data: {}", response);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create")
    public ResponseEntity<UserDto> createUser(
            @RequestBody @Valid UserRequest request) {
        log.info("Received request to create user: {}", request.getEmail());
        System.out.println(request.getBirthDate());
        UserDto response = userService.createUser(request);

        log.debug("User was created: {}", request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> updateUserWithId(
            @PathVariable("id") Integer id,
            @RequestBody @Valid UserRequest updatedUser) {
        log.info("Received request to update user with ID: {}", id);
        UserDto response = userService.updateUser(id, updatedUser);

        log.debug("User was updated: {}", updatedUser);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/active")
    public ResponseEntity<Void> setActiveUser(
            @PathVariable("id") Integer id) {
        log.info("Received request to set active user with ID: {}", id);

        if (!userService.activateUser(id)) {
            log.debug("User do not activated: {}", id);
            return ResponseEntity.badRequest().build();
        }

        log.debug("User activated: {}", id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/inactive")
    public ResponseEntity<Void> setInactiveUser(
            @PathVariable("id") Integer id
    ) {
        log.info("Received request to set inactive user with ID: {}", id);

        if (!userService.deactivateUser(id)) {
            log.debug("User do not inactivated: {}", id);
            return ResponseEntity.badRequest().build();
        }

        log.debug("User inactivated: {}", id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserById(
            @PathVariable Integer id) {
        log.info("Received request to delete user with ID: {}", id);
        userService.deleteById(id);

        log.debug("User deleted: {}", id);
        return ResponseEntity.noContent().build();
    }
}
