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
        user.setName("name");
        user.setEmail("testemail@mail.com");

        userRequest = new UserRequest();
        userRequest.setName("name");
        userRequest.setEmail("testemail@mail.com");

        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("name");
        userDto.setEmail("testemail@mail.com");
    }

    @Test
    public void createUser_Successful() {
        // Arrange:
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.createUser(any())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto result = userService.createUser(userRequest);

        // Assert:
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        // Verify
        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).createUser(any());
    }

    @Test
    public void createUser_ThrowException() {
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
    public void getUserById_Successful() {
        // Arrange:
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto result = userService.getUserById(1);

        // Assert:
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void getUserById_ThrowException() {
        // Arrange:
        when(userRepository.findById(anyInt())).
                thenThrow(new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1)));

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.getUserById(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    public void getAllUsers_Successful() {
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
    public void updateUser_Successful() {
        // Arrange:
        user.setName("newName");
        user.setSurname("newSurname");

        userDto.setName("newName");
        userDto.setSurname("newSurname");

        userRequest.setName("newName");
        userRequest.setSurname("newSurname");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        doNothing().when(userMapper).updateUser(userRequest, user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto result = userService.updateUser(anyInt(), userRequest);

        // Assert:
        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getSurname(), result.getSurname());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
        verify(userMapper, times(1)).updateUser(userRequest, user);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void updateUser_UserNotFound_ThrowException() {
        // Arrange:
        user.setName("newName");
        user.setSurname("newSurname");

        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.updateUser(1, userRequest));

        // Assert
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void updateUser_EmailAlreadyExists_ThrowException() {
        // Arrange:
        user.setName("newName");
        user.setSurname("newSurname");

        userRequest.setName("newName");
        userRequest.setSurname("newSurname");
        userRequest.setEmail("newemail@mail.com");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // Act:
        DataExistException result = assertThrows(DataExistException.class, () -> userService.updateUser(1, userRequest));

        // Assert:
        assertEquals(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(userRequest.getEmail()), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
        verify(userRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    public void updateUser_CardCountError_ThrowException() {// Arrange:
        user.setName("newName");
        user.setSurname("newSurname");
        user.setCards(List.of(new Card(), new Card(), new Card(), new Card(), new Card(), new Card()));
        userRequest.setName("newName");
        userRequest.setSurname("newSurname");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        // Act:
        InvalidDataException result = assertThrows(InvalidDataException.class, () -> userService.updateUser(1, userRequest));

        // Assert:
        assertEquals(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(user.getId()), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void activateUser_Successful() {
        // Arrange:
        userDto.setActive(ActiveStatus.ACTIVE);

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto result = userService.activateUser(anyInt());

        // Assert:
        assertNotNull(result);
        assertEquals(ActiveStatus.ACTIVE, result.getActive());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void activateUser_UserNotFound_ThrowException() {
        // Arrange:
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.activateUser(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void deactivateUser_Successful() {
        // Arrange:
        userDto.setActive(ActiveStatus.INACTIVE);

        when(userRepository.findUserById(user.getId())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        // Act:
        UserDto response = userService.deactivateUser(user.getId());

        // Assert:
        assertEquals(ActiveStatus.INACTIVE, response.getActive());

        // Verify:
        verify(userRepository, times(1)).findUserById(user.getId());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void deactivateUser_UserNotFound_ThrowException() {// Arrange:
        when(userRepository.findUserById(1)).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.deactivateUser(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findUserById(1);
    }

    @Test
    public void deleteById_Successful() {
        // Arrange:
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);

        // Act:
        userService.deleteById(1);

        // Verify:
        verify(userRepository, times(1)).existsById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    public void deleteById_UserNotFound_ThrowException() {
        // Arrange:
        when(userRepository.existsById(anyInt())).thenReturn(false);

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.deleteById(1));

        // Assert:
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).existsById(anyInt());
    }
}