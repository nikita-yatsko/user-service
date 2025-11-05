package com.userservise.app.mapper;

import com.userservise.app.model.dto.UserDto;
import com.userservise.app.model.entity.User;
import org.mapstruct.*;

@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        uses = {CardMapper.class}
)
public interface UserMapper {

    UserDto toDto(User user);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateUser(UserDto userDto, @MappingTarget User user);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "cards", ignore = true)
    User toUser(UserDto userDto);

}
