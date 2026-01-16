package com.user.servise.User.Servise.service;

import com.userservise.app.mapper.CardMapper;
import com.userservise.app.model.constants.ErrorMessage;
import com.userservise.app.model.dto.CardDto;
import com.userservise.app.model.entity.Card;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.model.exception.InvalidDataException;
import com.userservise.app.model.exception.NotFoundException;
import com.userservise.app.repository.CardRepository;
import com.userservise.app.repository.UserRepository;
import com.userservise.app.service.Impl.CardServiceImpl;
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

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card card;
    private CardDto cardDto;
    private User user;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        cardRepository.deleteAll();

        card = new Card();
        card.setId(1);
        card.setHolder("user");
        card.setNumber("1111222233334444");

        cardDto = new CardDto();
        cardDto.setId(1);
        cardDto.setHolder("user");
        cardDto.setNumber("1111222233334444");

        user = new User();
        user.setId(1);
        user.setName("user");
        user.setCards(List.of(card));
    }

    @Test
    public void createCardSuccessful() {
        // Arrange:
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(cardRepository.save(any())).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act:
        CardDto result = cardService.createCard(anyInt());

        // Assert:
        assertNotNull(result);
        assertEquals(cardDto.getId(), result.getId());
        assertEquals(cardDto.getHolder(), result.getHolder());
        assertEquals(cardDto.getNumber(), result.getNumber());
        assertEquals(user.getCards().getFirst().getNumber(), result.getNumber());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    public void createCardUserNotFoundThrowException() {
        // Arrange:
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.createCard(1));

        // Assert
        assertNotNull(result);
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(userRepository, times(1)).findById(anyInt());
    }

    @Test
    public void createCardCardCountMoreFiveThrowException() {
        user.setCards(List.of(new Card(), new Card(), new Card(), new Card(), new Card()));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        InvalidDataException result = assertThrows(InvalidDataException.class, () -> cardService.createCard(1));

        assertNotNull(result);
        assertEquals(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(1), result.getMessage());
    }

    @Test
    public void getCardByIdSuccessful() {
        // Arrange:
        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act:
        CardDto result = cardService.getCardById(1);

        // Assert:
        assertNotNull(result);
        assertEquals(cardDto.getId(), result.getId());
        assertEquals(cardDto.getHolder(), result.getHolder());
        assertEquals(cardDto.getNumber(), result.getNumber());
        assertEquals(user.getCards().getFirst().getNumber(), result.getNumber());

        // Verify:
        verify(cardRepository, times(1)).findById(anyInt());
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    public void getCardByIdCardNotFoundThrowException() {
        // Arrange:
        when(cardRepository.findById(1)).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.getCardById(1));

        // Assert:
        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
    }

    @Test
    public void getAllCardsSuccessful() {
        // Arrange:
        Pageable pageable = PageRequest.of(0, 10);
        Page<Card> response = new PageImpl<>(Collections.singletonList(card), pageable, 1L);

        when(cardRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(response);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act:
        Page<CardDto> result = cardService.getAllCards("test", pageable);

        // Assert
        assertNotNull(result);
        assertEquals(cardDto.getId(), result.getContent().getFirst().getId());
        assertEquals(cardDto.getHolder(), result.getContent().getFirst().getHolder());
        assertEquals(cardDto.getNumber(), result.getContent().getFirst().getNumber());

        // Verify:
        verify(cardRepository, times(1)).findAll(any(Specification.class), eq(pageable));
        verify(cardMapper, times(1)).toDto(card);
    }


    @Test
    public void getAllByUserIdSuccessful() {
        // Arrange:
        when(cardRepository.findCardsByOwnerId(anyInt())).thenReturn(List.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act:
        List<CardDto> result = cardService.getAllByUserId(1);

        // Assert:
        assertNotNull(result);
        assertEquals(cardDto.getId(), result.getFirst().getId());
        assertEquals(cardDto.getHolder(), result.getFirst().getHolder());
        assertEquals(cardDto.getNumber(), result.getFirst().getNumber());
        assertEquals(user.getCards().getFirst().getNumber(), result.getFirst().getNumber());

        // Verify:
        verify(cardRepository, times(1)).findCardsByOwnerId(anyInt());
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    public void activateCardSuccessful() {
        // Arrange:
        int cardId = card.getId();
        cardDto.setActive(ActiveStatus.ACTIVE);
        card.setActive(ActiveStatus.INACTIVE);

        when(cardRepository.findCardById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(card)).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act:
        CardDto result = cardService.activateCard(cardId);

        // Assert:
        assertEquals(ActiveStatus.ACTIVE, result.getActive());

        // Verify:
        verify(cardRepository, times(1)).findCardById(cardId);
        verify(cardRepository, times(1)).save(card);
    }

    @Test
    public void activateCardCardNotFoundThrowException() {
        // Arrange:
        when(cardRepository.findCardById(anyInt())).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.activateCard(1));

        // Assert:
        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(cardRepository, times(1)).findCardById(anyInt());
    }

    @Test
    public void deactivateCardSuccessful() {
        // Arrange:
        int cardId = card.getId();
        cardDto.setActive(ActiveStatus.INACTIVE);

        when(cardRepository.findCardById(cardId)).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        // Act:
        CardDto result = cardService.deactivateCard(cardId);

        // Assert:
        assertNotNull(result);
        assertEquals(ActiveStatus.INACTIVE, result.getActive());

        // Verify:
        verify(cardRepository, times(1)).findCardById(cardId);
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    public void deactivateCardCardNotFoundThrowException() {
        // Arrange:
        when(cardRepository.findCardById(anyInt())).thenReturn(Optional.empty());

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.deactivateCard(1));

        // Assert:
        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(cardRepository, times(1)).findCardById(anyInt());
    }

    @Test
    public void deleteCardSuccessful() {
        // Arrange:
        when(cardRepository.existsById(anyInt())).thenReturn(true);
        doNothing().when(cardRepository).deleteById(anyInt());

        // Act:
        cardService.deleteCard(1);

        // Verify:
        verify(cardRepository, times(1)).existsById(anyInt());
        verify(cardRepository, times(1)).deleteById(anyInt());
    }

    @Test
    public void deleteCardCardNotFoundThrowException() {
        // Arrange:
        when(cardRepository.existsById(anyInt())).thenReturn(false);

        // Act:
        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.deleteCard(1));

        // Assert
        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());

        // Verify:
        verify(cardRepository, times(1)).existsById(anyInt());
    }
}
