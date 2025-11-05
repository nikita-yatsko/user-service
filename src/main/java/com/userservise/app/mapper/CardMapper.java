package com.userservise.app.mapper;

import com.userservise.app.model.dto.CardDto;
import com.userservise.app.model.entity.Card;
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

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    Card toCard(CardDto cardDto);

    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "owner", ignore = true)
    void updateCard(CardDto cardDto, @MappingTarget Card card);
}
