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
        user.setName("testname");
        user.setEmail("testemail@mail.com");

        userRequest = new UserRequest();
        userRequest.setName("testname");
        userRequest.setEmail("testemail@mail.com");

        userDto = new UserDto();
        userDto.setId(1);
        userDto.setName("testname");
        userDto.setEmail("testemail@mail.com");
    }

    @Test
    public void createUser_Successful() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.createUser(any())).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.createUser(userRequest);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository, times(1)).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).createUser(any());
    }

    @Test
    public void createUser_ThrowException() {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        DataExistException result = assertThrows(DataExistException.class, () -> userService.createUser(userRequest));

        assertEquals(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(userRequest.getEmail()),
                result.getMessage());

        verify(userRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    public void getUserById_Successful() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        UserDto result = userService.getUserById(1);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getEmail(), result.getEmail());

        verify(userRepository, times(1)).findById(anyInt());
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void getUserById_ThrowException() {
        when(userRepository.findById(anyInt())).
                thenThrow(new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1)));
        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.getUserById(1));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    public void getAllUsers_Successful() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> response = new PageImpl<>(Collections.singletonList(user), pageable, 1L);

        when(userRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(response);
        when(userMapper.toDto(any(User.class))).thenReturn(userDto);

        Page<UserDto> result = userService.getAllUsers("testname", "testSurname", pageable);

        assertNotNull(result);
        assertEquals(response.getTotalElements(), result.getTotalElements());
        assertEquals(userDto.getName(), result.getContent().get(0).getName());

        verify(userRepository, times(1)).findAll(any(Specification.class), eq(pageable));
    }

    @Test
    public void updateUser_Successful() {
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

        UserDto result = userService.updateUser(anyInt(), userRequest);

        assertNotNull(result);
        assertEquals(user.getId(), result.getId());
        assertEquals(user.getName(), result.getName());
        assertEquals(user.getSurname(), result.getSurname());

        verify(userRepository, times(1)).findById(anyInt());
        verify(userMapper, times(1)).updateUser(userRequest, user);
        verify(userRepository, times(1)).save(any(User.class));
        verify(userMapper, times(1)).toDto(any(User.class));
    }

    @Test
    public void updateUser_UserNotFound_ThrowException() {
        user.setName("newName");
        user.setSurname("newSurname");

        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.updateUser(1, userRequest));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void updateUser_EmailAlreadyExists_ThrowException() {
        user.setName("newName");
        user.setSurname("newSurname");

        userRequest.setName("newName");
        userRequest.setSurname("newSurname");
        userRequest.setEmail("newemail@mail.com");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        DataExistException result = assertThrows(DataExistException.class, () -> userService.updateUser(1, userRequest));

        assertEquals(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(userRequest.getEmail()), result.getMessage());
        verify(userRepository, times(1)).findById(anyInt());
        verify(userRepository, times(1)).existsByEmail(anyString());
    }

    @Test
    public void updateUser_CardCountError_ThrowException() {
        user.setName("newName");
        user.setSurname("newSurname");
        user.setCards(List.of(new Card(), new Card(), new Card(), new Card(), new Card(), new Card()));

        userRequest.setName("newName");
        userRequest.setSurname("newSurname");

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        InvalidDataException result = assertThrows(InvalidDataException.class, () -> userService.updateUser(1, userRequest));

        assertEquals(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(userRequest.getEmail()), result.getMessage());
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void activateUser_Successful() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        user.setActive(ActiveStatus.ACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Boolean result = userService.activateUser(anyInt());

        assertNotNull(result);
        assertEquals(true, result);
        verify(userRepository, times(1)).findById(anyInt());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void activateUser_UserNotFound_ThrowException() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.activateUser(1));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void deactivateUser_Successful() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        user.setActive(ActiveStatus.INACTIVE);
        when(userRepository.save(any(User.class))).thenReturn(user);

        Boolean result = userService.activateUser(anyInt());

        assertNotNull(result);
        assertEquals(true, result);
        verify(userRepository, times(1)).findById(anyInt());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void deactivateUser_UserNotFound_ThrowException() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.empty());

        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.activateUser(1));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void deleteById_Successful() {
        when(userRepository.existsById(1)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1);

        userService.deleteById(1);

        verify(userRepository, times(1)).existsById(1);
        verify(userRepository, times(1)).deleteById(1);
    }

    @Test
    public void deleteById_UserNotFound_ThrowException() {
        when(userRepository.existsById(anyInt())).thenReturn(false);

        NotFoundException result = assertThrows(NotFoundException.class, () -> userService.deleteById(1));

        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(userRepository, times(1)).existsById(anyInt());
    }
}
