package com.user.servise.User.Servise.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservise.app.UserServiceApplication;
import com.userservise.app.mapper.CardMapper;
import com.userservise.app.model.dto.CardDto;
import com.userservise.app.model.entity.Card;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.repository.CardRepository;
import com.userservise.app.repository.UserRepository;
import com.userservise.app.security.model.CustomUserDetails;
import com.userservise.app.service.CardService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@ActiveProfiles("test")
@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class CardControllerTest extends BaseIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardService cardService;

    @Autowired
    private CardMapper cardMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;
    private Card card;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setUserId(1);
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("testUserMail@mail.com");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setActive(ActiveStatus.ACTIVE);

        user = userRepository.saveAndFlush(user);
        card = cardMapper.toCard(cardService.createCard(user.getUserId()));
    }

    @AfterEach
    public void tearDown() {
        cardRepository.deleteAllInBatch(); // deleteAllInBatch быстрее обычного deleteAll
        userRepository.deleteAllInBatch();
    }


    @Test
    public void getCardByIdReturn200Ok() throws Exception {
        // Given:
        Card savedCard = card;
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(
                MockMvcRequestBuilders.get("/api/card/" + card.getId())
                        .with(authentication(auth))
                        .accept(MediaType.APPLICATION_JSON)
        );

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedCard.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.holder").value(savedCard.getHolder()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(savedCard.getNumber()));
    }

    @Test
    public void getAllCardsReturn200Ok() throws Exception {
        // Given:
        String holder = user.getName() + " " + user.getSurname();
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/card/all")
                .param("holder", holder)
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].holder").value(card.getHolder()));
    }

    @Test
    public void getCardByUserIdReturn200Ok() throws Exception {
        // Given:
        String holder = user.getName() + " " + user.getSurname();
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/card/user/" + user.getId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(card.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].number").value(card.getNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].holder").value(holder));
    }

    @Test
    public void createCardReturn200Ok() throws Exception {
        // Given:
        String holder = user.getName() + " " + user.getSurname();
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/card/create/" + user.getId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.holder").value(holder));
    }

    @Test
    public void updateCardReturn200Ok() throws Exception {
        //Given:
        CardDto updateDto = new CardDto();
        updateDto.setId(card.getId());
        updateDto.setNumber("1234567890123456");
        updateDto.setHolder(card.getHolder());
        updateDto.setActive(ActiveStatus.ACTIVE);
        updateDto.setExpirationDate(LocalDate.now().plusYears(4));
        card.setActive(ActiveStatus.ACTIVE);

        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/card/update/" + card.getId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(updateDto.getNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.holder").value(updateDto.getHolder()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.ACTIVE.toString()));
    }

    @Test
    public void setActiveCardReturn200Ok() throws Exception {
        // Given:
        card.setActive(ActiveStatus.INACTIVE);
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/card/{id}/active", card.getId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(card.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.holder").value(card.getHolder()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(card.getNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.ACTIVE.toString()));
    }

    @Test
    public void setInactiveCardReturn200Ok() throws Exception {
        // Given:
        card.setActive(ActiveStatus.ACTIVE);
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/card/{id}/inactive", card.getId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(card.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.holder").value(card.getHolder()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(card.getNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.INACTIVE.toString()));
    }

    @Test
    public void deleteCardReturn200Ok() throws Exception {
        // Given:
        Card deleteCard = card;
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/card/{id}/delete", deleteCard.getId())
                .with(authentication(auth)));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isNoContent());
        boolean exists = cardRepository.existsById(deleteCard.getId());
        Assertions.assertFalse(exists, "Card should be deleted from database");
    }
}