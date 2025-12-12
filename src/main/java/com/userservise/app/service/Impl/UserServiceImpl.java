package com.userservise.app.service.Impl;

import com.userservise.app.mapper.UserMapper;
import com.userservise.app.model.constants.ErrorMessage;
import com.userservise.app.model.dto.UserRequest;
import com.userservise.app.model.dto.UserDto;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.model.exception.DataExistException;
import com.userservise.app.model.exception.InvalidDataException;
import com.userservise.app.model.exception.NotFoundException;
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
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    @CachePut(value = "users", key = "#result.userId")
    public UserRequest createUser(UserRequest request) {
        if (userRepository.existsByEmail(request.getEmail()))
            throw new DataExistException(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(request.getEmail()));

        User savedUser = userMapper.createUser(request);
        User user =  userRepository.save(savedUser);

        return userMapper.toUserRequest(user);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserDto getUserById(Integer userId) {
        return userRepository.findUserByUserId(userId)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));
    }

    @Override
    public UserRequest getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(userMapper::toUserRequest)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_EMAIL.getMessage(email)));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserDto> getAllUsers(String firstName, String surname, Pageable pageable) {
        Specification<User> specification = UserSpecifications.hasFirstName(firstName)
                .and(UserSpecifications.hasSurname(surname));

        return userRepository.findAll(specification, pageable)
                .map(userMapper::toDto);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#userId")
    public UserDto updateUser(Integer userId, UserRequest request) {
        User user = userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        if (!request.getEmail().equals(user.getEmail()) && userRepository.existsByEmail(request.getEmail()))
            throw new DataExistException(ErrorMessage.EMAIL_ALREADY_EXISTS.getMessage(request.getEmail()));

        if (!checkCardsCount(user))
            throw new InvalidDataException(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(userId));

        userMapper.updateUser(request, user);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    @CachePut(value = "users", key = "#userId")
    public UserDto activateUser(Integer userId) {
        User user = userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        user.setActive(ActiveStatus.ACTIVE);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public UserDto deactivateUser(Integer userId) {
        User user = userRepository.findUserByUserId(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        user.setActive(ActiveStatus.INACTIVE);
        User updatedUser = userRepository.save(user);

        return userMapper.toDto(updatedUser);
    }

    @Override
    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void deleteById(Integer userId) {
        if (!userRepository.existsByUserId(userId))
            throw new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId));
        userRepository.deleteUserByUserId(userId);
    }

    private Boolean checkCardsCount(User user) {
        return user.getCards().size() <= 5;
    }
}
