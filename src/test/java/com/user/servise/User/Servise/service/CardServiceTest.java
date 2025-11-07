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
    public void createCard_Successful() {
        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(cardRepository.save(any())).thenReturn(card);
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.createCard(anyInt());

        assertNotNull(result);
        assertEquals(cardDto.getId(), result.getId());
        assertEquals(cardDto.getHolder(), result.getHolder());
        assertEquals(cardDto.getNumber(), result.getNumber());
        assertEquals(user.getCards().get(0).getNumber(), result.getNumber());

        verify(userRepository, times(1)).findById(anyInt());
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    public void createCard_UserNotFound_ThrowException() {
        when(userRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.createCard(1));

        assertNotNull(result);
        assertEquals(ErrorMessage.USER_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
    }

    @Test
    public void createCard_CardCountMoreFive_ThrowException() {
        user.setCards(List.of(new Card(), new Card(), new Card(), new Card(), new Card()));
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        InvalidDataException result = assertThrows(InvalidDataException.class, () -> cardService.createCard(1));

        assertNotNull(result);
        assertEquals(ErrorMessage.USER_CANNOT_HAVE_MORE_THAN_5_CARDS.getMessage(1), result.getMessage());
    }

    @Test
    public void getCardById_Successful() {
        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        CardDto result = cardService.getCardById(1);

        assertNotNull(result);
        assertEquals(cardDto.getId(), result.getId());
        assertEquals(cardDto.getHolder(), result.getHolder());
        assertEquals(cardDto.getNumber(), result.getNumber());
        assertEquals(user.getCards().getFirst().getNumber(), result.getNumber());

        verify(cardRepository, times(1)).findById(anyInt());
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    public void getCardById_CardNotFound_ThrowException() {
        when(cardRepository.findById(1)).thenReturn(Optional.empty());

        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.getCardById(1));

        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
    }

    //TODO add getAllCards tests

    @Test
    public void getAllByUserId_Successful() {
        when(cardRepository.findCardsByOwnerId(anyInt())).thenReturn(List.of(card));
        when(cardMapper.toDto(card)).thenReturn(cardDto);

        List<CardDto> result = cardService.getAllByUserId(1);

        assertNotNull(result);
        assertEquals(cardDto.getId(), result.getFirst().getId());
        assertEquals(cardDto.getHolder(), result.getFirst().getHolder());
        assertEquals(cardDto.getNumber(), result.getFirst().getNumber());
        assertEquals(user.getCards().getFirst().getNumber(), result.getFirst().getNumber());

        verify(cardRepository, times(1)).findCardsByOwnerId(anyInt());
        verify(cardMapper, times(1)).toDto(card);
    }

    @Test
    public void activateCard_Successful() {
        card.setActive(ActiveStatus.ACTIVE);

        when(cardRepository.findCardById(anyInt())).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenReturn(card);

        Boolean result = cardService.activateCard(1);

        assertNotNull(result);
        assertEquals(true, result);

        verify(cardRepository, times(1)).findCardById(anyInt());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    public void activateCard_CardNotFound_ThrowException() {
        when(cardRepository.findCardById(anyInt())).thenReturn(Optional.empty());

        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.activateCard(1));

        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(cardRepository, times(1)).findCardById(anyInt());
    }

    @Test
    public void deactivateCard_Successful() {
        card.setActive(ActiveStatus.INACTIVE);

        when(cardRepository.findCardById(anyInt())).thenReturn(Optional.of(card));
        when(cardRepository.save(any())).thenReturn(card);

        Boolean result = cardService.deactivateCard(1);

        assertNotNull(result);
        assertEquals(true, result);

        verify(cardRepository, times(1)).findCardById(anyInt());
        verify(cardRepository, times(1)).save(any(Card.class));
    }

    @Test
    public void deactivateCard_CardNotFound_ThrowException() {
        when(cardRepository.findCardById(anyInt())).thenReturn(Optional.empty());

        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.deactivateCard(1));

        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(cardRepository, times(1)).findCardById(anyInt());
    }

    @Test
    public void deleteCard_Successful() {
        when(cardRepository.existsById(anyInt())).thenReturn(true);
        doNothing().when(cardRepository).deleteById(anyInt());

        cardService.deleteCard(1);

        verify(cardRepository, times(1)).existsById(anyInt());
        verify(cardRepository, times(1)).deleteById(anyInt());
    }

    @Test
    public void deleteCard_CardNotFound_ThrowException() {
        when(cardRepository.existsById(anyInt())).thenReturn(false);

        NotFoundException result = assertThrows(NotFoundException.class, () -> cardService.deleteCard(1));

        assertNotNull(result);
        assertEquals(ErrorMessage.CARD_NOT_FOUND_BY_ID.getMessage(1), result.getMessage());
        verify(cardRepository, times(1)).existsById(anyInt());
    }


}
