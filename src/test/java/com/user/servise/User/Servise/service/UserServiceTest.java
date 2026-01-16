package com.user.servise.User.Servise.service;

import com.userservise.app.mapper.UserMapper;
import com.userservise.app.model.constants.ErrorMessage;
import com.userservise.app.model.dto.UserDto;
import com.userservise.app.model.dto.UserRequest;
import com.userservise.app.model.entity.Card;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.model.exception.DataExistException;
import com.userservise.app.model.exception.InvalidDataException;
import com.userservise.app.model.exception.NotFoundException;
import com.userservise.app.repository.UserRepository;
import com.userservise.app.service.Impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto userDto;
    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1);
        user.setUserId(1);
        user.setUserId(1);
        user.setName("name");
        user.setEmail("testemail@mail.com");

        userRequest = new UserRequest();
        userRequest.setUserId(1);
        userRequest.setName("name");
        userRequest.setEmail("testemail@mail.com");

        userDto = new UserDto();
        userDto.setUserId(1);
        userDto.setName("name");
        userDto.setEmail("testemail@mail.com");
    }

    @Test
    public void createUserSuccessful() {
        // Arrange:
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.createUser(any())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserRequest(any())).thenReturn(userRequest);

        // Act:
        UserRequest result = userService.createUser(userRequest);

        // Assert:
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        // Verify
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).createUser(any());
        verify(userMapper, times(1)).toUserRequest(any());
    }

    @Test
    public void createUserThrowException() {
        // Arrange:
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act:
        DataExistException result = assertThrows(DataExistException.class, () -> userService.createUser(userRequest));

        // Assert:
        assertEquals(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(userRequest.getEmail()),
                result.getMessage());

        // Verify:
        verify(userRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    public void getUserByIdSuccessful() {
        // Arrange:
        when(userRepository.findUserByUserId(anyInt())).thenReturn(Optional.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto result = userService.getUserById(1);

        // Assert:
        assertNotNull(result);
        assertEquals(user.getId(), result.getUserId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(anyInt());
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void getUserByIdThrowException() {
        // Arrange:
        when(userRepository.findUserByUserId(anyInt())).
                thenThrow(new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1)));

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.getUserById(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(1);
    }

    @Test
    public void getAllUsersSuccessful() {
        // Arrange:
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> response = new PageImpl<>(Collections.singletonList(user), pageable, 1L);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(response);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act
        Page<UserDto> result = userService.getAllUsers("testName", "testSurname", pageable);

        // Assert:
        assertNotNull(result);
        assertEquals(response.getTotalElements(), result.getTotalElements());
        assertEquals(userDto.getName(), result.getContent().getFirst().getName());

        // Verify:
        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void updateUserSuccessful() {
        // Arrange:
        user.setName("newName");
        user.setSurname("newSurname");

        userDto.setName("newName");
        userDto.setSurname("newSurname");

        userRequest.setName("newName");
        userRequest.setSurname("newSurname");

        when(userRepository.findUserByUserId(anyInt())).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUser(userRequest, user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto result = userService.updateUser(anyInt(), userRequest);

        // Assert:
        assertNotNull(result);
        assertEquals(user.getUserId(), result.getUserId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getSurname(), result.getSurname());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(anyInt());
        verify(userMapper, times(1)).updateUser(userRequest, user);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void updateUserUserNotFoundThrowException() {
        // Arrange:
        user.setName("newName");
        user.setSurname("newSurname");

        when(userRepository.findUserByUserId(anyInt())).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.updateUser(1, userRequest));

        // Assert
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(anyInt());
    }

    @Test
    public void updateUserEmailAlreadyExistsThrowException() {
        // Arrange:
        user.setName("newName");
        user.setSurname("newSurname");

        userRequest.setName("newName");
        userRequest.setSurname("newSurname");
        userRequest.setEmail("newemail@mail.com");

        when(userRepository.findUserByUserId(anyInt())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act:
        DataExistException result = assertThrows(DataExistException.class, () -> userService.updateUser(1, userRequest));

        // Assert:
        assertEquals(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(userRequest.getEmail()), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(anyInt());
        verify(userRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    public void updateUserCardCountErrorThrowException() {
        // Arrange:
        user.setName("newName");
        user.setSurname("newSurname");
        user.setCards(List.of(new Card(), new Card(), new Card(), new Card(), new Card(), new Card()));
        userRequest.setName("newName");
        userRequest.setSurname("newSurname");

        when(userRepository.findUserByUserId(anyInt())).thenReturn(Optional.of(user));

        // Act:
        InvalidDataException result = assertThrows(InvalidDataException.class, () -> userService.updateUser(1, userRequest));

        // Assert:
        assertEquals(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(user.getId()), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(anyInt());
    }

    @Test
    public void activateUserSuccessful() {
        // Arrange:
        userDto.setActive(ActiveStatus.ACTIVE);

        when(userRepository.findUserByUserId(anyInt())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto result = userService.activateUser(anyInt());

        // Assert:
        assertNotNull(result);
        assertEquals(ActiveStatus.ACTIVE, result.getActive());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(anyInt());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void activateUser_serNotFoundThrowException() {
        // Arrange:
        when(userRepository.findUserByUserId(anyInt())).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.activateUser(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(anyInt());
    }

    @Test
    public void deactivateUserSuccessful() {
        // Arrange:
        userDto.setActive(ActiveStatus.INACTIVE);

        when(userRepository.findUserByUserId(user.getUserId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto response = userService.deactivateUser(user.getId());

        // Assert:
        assertEquals(ActiveStatus.INACTIVE, response.getActive());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(user.getUserId());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void deactivateUserUserNotFoundThrowException() {
        // Arrange:
        when(userRepository.findUserByUserId(1)).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.deactivateUser(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findUserByUserId(1);
    }

    @Test
    public void deleteByIdSuccessful() {
        // Arrange:
        when(userRepository.existsByUserId(1)).thenReturn(true);
        doNothing().when(userRepository).deleteUserByUserId(1);

        // Act:
        userService.deleteById(1);

        // Verify:
        verify(userRepository, times(1)).existsByUserId(1);
        verify(userRepository, times(1)).deleteUserByUserId(1);
    }

    @Test
    public void deleteByIdUserNotFoundThrowException() {
        // Arrange:
        when(userRepository.existsByUserId(anyInt())).thenReturn(false);

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.deleteById(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).existsByUserId(anyInt());
    }
}
