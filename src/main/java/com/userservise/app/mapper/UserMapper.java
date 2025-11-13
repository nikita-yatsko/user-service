package com.userservise.app.mapper;

import com.userservise.app.model.dto.UserRequest;
import com.userservise.app.model.dto.UserDto;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CardMapper.class},
        imports = {ActiveStatus.class}
)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateUser(UserRequest userDto, @MappingTarget User user);

    @Mapping(target = "cards", ignore = true)
    User toUser(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "active", expression = "java(ActiveStatus.INACTIVE)")
    User createUser(UserRequest request);

}
