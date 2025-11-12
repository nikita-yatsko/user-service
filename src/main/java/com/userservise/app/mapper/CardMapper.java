package com.userservise.app.mapper;

import com.userservise.app.model.dto.CardDto;
import com.userservise.app.model.entity.Card;
import com.userservise.app.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;


@Mapper(
        componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CardMapper {

    CardDto toDto(Card card);

    @Mapping(target = "owner", ignore = true)
    Card toCard(CardDto cardDto);

    @Mapping(target = "owner", ignore = true)
    void updateCard(CardDto cardDto, @MappingTarget Card card);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", source = "cardDto.active")
    @Mapping(source = "user", target = "owner")
    Card createCard(CardDto cardDto, User user);

}
