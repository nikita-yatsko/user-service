package com.userservise.app.service.Impl;

import com.userservise.app.mapper.UserMapper;
import com.userservise.app.model.constants.ErrorMessage;
import com.userservise.app.model.dto.UpdateUserDto;
import com.userservise.app.model.dto.UserDto;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.model.exception.DataExistException;
import com.userservise.app.model.exception.InvalidDataException;
import com.userservise.app.model.exception.NotFoundException;
import com.userservise.app.model.request.CreateUserRequest;
import com.userservise.app.repository.UserRepository;
import com.userservise.app.service.UserService;
import com.userservise.app.utils.specifications.UserSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @CachePut(value = "users", key = "#result.id")
    public UserDto createUser(CreateUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new DataExistException(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(request.getEmail()));

        User user =  userRepository.save(userMapper.createUser(request));

        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));
        return userMapper.toDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> specification = UserSpecifications.hasFirstName(firstName)
                .and(UserSpecifications.hasSurname(surname));

        Page<User> usersPage = userRepository.findAll(specification, pageable);

        return usersPage.map(userMapper::toDto);
    }

    @Override
    @CachePut(value = "users", key = "#id")
    public UserDto updateUser(Integer id, UpdateUserDto request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail()))
            throw new DataExistException(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(request.getEmail()));

        if (!checkCardsCount(user))
            throw new InvalidDataException(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(request.getEmail()));

        userMapper.updateUser(request, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public Boolean activateUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));

        user.setActive(ActiveStatus.ACTIVE);
        User updatedUser = userRepository.save(user);

        return updatedUser.getActive().equals(ActiveStatus.ACTIVE);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public Boolean deactivateUser(Integer id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id)));

        user.setActive(ActiveStatus.INACTIVE);
        User updatedUser = userRepository.save(user);

        return updatedUser.getActive().equals(ActiveStatus.INACTIVE);
    }

    @Override
    @CacheEvict(value = "users", key = "#id")
    public void deleteById(Integer id) {
        if (!userRepository.existsById(id))
            throw new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(id));
        userRepository.deleteById(id);
    }

    private Boolean checkCardsCount(User user) {
        return user.getCards().size() <= 5;
    }
}
