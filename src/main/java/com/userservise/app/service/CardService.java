package com.userservise.app.service;


import com.userservise.app.model.dto.CardDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CardService {

    CardDto createCard(Integer userId);

    CardDto getCardById(Integer id);

    Page<CardDto> getAllCards(String holder, Pageable pageable);

    List<CardDto> getAllByUserId(Integer userId);

    CardDto updateCard(Integer id, CardDto updateCard);

    Boolean activateCard(Integer id);

    Boolean deactivateCard(Integer id);

    void deleteCard(Integer id);
}
