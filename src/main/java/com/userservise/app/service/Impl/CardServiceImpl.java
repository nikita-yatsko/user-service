package com.userservise.app.service.Impl;

import com.userservise.app.mapper.CardMapper;
import com.userservise.app.model.constants.ErrorMessage;
import com.userservise.app.model.dto.CardDto;
import com.userservise.app.model.entity.Card;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.model.exception.DataExistException;
import com.userservise.app.model.exception.InvalidDataException;
import com.userservise.app.model.exception.NotFoundException;
import com.userservise.app.repository.CardRepository;
import com.userservise.app.repository.UserRepository;
import com.userservise.app.service.CardService;
import com.userservise.app.utils.CardNumberGenerator;
import com.userservise.app.utils.specifications.CardSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CardDto createCard(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(userId)));

        if (user.getCards().size() >= 5)
            throw new InvalidDataException(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(userId));

        Card card = cardRepository.save(generateCard(user));

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public CardDto getCardById(Integer id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(id)));

        return cardMapper.toDto(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> getAllCards(String holder, Pageable pageable) {
        Specification<Card> specification = CardSpecifications.hasHolder(holder);

        Page<Card> cards = cardRepository.findAll(specification, pageable);

        return cards.map(cardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CardDto> getAllByUserId(Integer userId) {
        List<Card> cards = cardRepository.findCardsByOwnerId(userId);

        return cards.stream()
                .map(cardMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CardDto updateCard(Integer id, CardDto requestUpdate) {
        Card card = cardRepository.findCardById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(id)));

        if (!requestUpdate.getNumber().equals(card.getNumber()) && cardRepository.existsCardByNumber(requestUpdate.getNumber()))
            throw new DataExistException(ErrorMessage.CARD_NUMBER_ALREADY_EXISTS.getMessage(requestUpdate.getNumber()));

        cardMapper.updateCard(requestUpdate, card);
        Card updatedCard = cardRepository.save(card);

        return cardMapper.toDto(updatedCard);
    }

    @Override
    @Transactional
    public Boolean activateCard(Integer id) {
        Card card = cardRepository.findCardById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(id)));

        card.setActive(ActiveStatus.ACTIVE);
        cardRepository.save(card);

        return card.getActive().equals(ActiveStatus.ACTIVE);
    }

    @Override
    @Transactional
    public Boolean deactivateCard(Integer id) {
        Card card = cardRepository.findCardById(id)
                .orElseThrow(() -> new NotFoundException(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(id)));

        card.setActive(ActiveStatus.INACTIVE);
        cardRepository.save(card);

        return card.getActive().equals(ActiveStatus.INACTIVE);
    }

    @Override
    @Transactional
    public void deleteCard(Integer id) {
        if (!cardRepository.existsById(id))
            throw new NotFoundException(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(id));
        cardRepository.deleteById(id);
    }

    private Card generateCard (User user) {
        Card card = new Card();
        card.setOwner(user);
        card.setNumber(generateCardNumber());
        card.setHolder(user.getName() + " " + user.getSurname());
        card.setExpirationDate(new Date(System.currentTimeMillis() + 4L * 365 * 24 * 60 * 60 * 1000)); // add 4 years
        card.setActive(ActiveStatus.INACTIVE);

        return card;
    }

    private String generateCardNumber() {
        String cardNumber;
        do {
            cardNumber = CardNumberGenerator.generate();
        } while (cardRepository.existsCardByNumber(cardNumber));

        return cardNumber;
    }
}
