package com.user.servise.User.Servise.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservise.app.UserServiseApplication;
import com.userservise.app.mapper.CardMapper;
import com.userservise.app.model.dto.CardDto;
import com.userservise.app.model.entity.Card;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.repository.CardRepository;
import com.userservise.app.repository.UserRepository;
import com.userservise.app.service.CardService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@ActiveProfiles("test")
@AutoConfigureMockMvc
@SpringBootTest(classes = UserServiseApplication.class)
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
        user.setName("Test");
        user.setSurname("User");
        user.setEmail("testUserMail@mail.com");
        user.setBirthDate(LocalDate.of(2000, 1, 1));
        user.setActive(ActiveStatus.ACTIVE);

        user = userRepository.saveAndFlush(user);

        card = cardMapper.toCard(cardService.createCard(user.getId()));
    }

    @AfterAll
    public void tearDown() {
        if (user != null && user.getId() != null)
            userRepository.deleteById(user.getId());

        if (card != null && card.getId() != null)
            cardRepository.deleteById(card.getId());
    }

    @Test
    public void getCardById_200_Ok() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                .get("/api/card/" + card.getId())
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(card.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(card.getNumber()));
    }

    @Test
    public void getAllCards_200_Ok() throws Exception {
        String holder = user.getName() + " " + user.getSurname();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/card/all")
                        .param("holder", holder)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].id").value(card.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].number").value(card.getNumber()));
    }

    @Test
    public void getCardByUserId_200_Ok() throws Exception {
        String holder = user.getName() + " " + user.getSurname();

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/api/card/user/" + user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(card.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].number").value(card.getNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].holder").value(holder));
    }

    @Test
    @Transactional
    public void createCard_200_Ok() throws Exception {
        String holder = user.getName() + " " + user.getSurname();

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/api/card/create/" + user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.holder").value(holder));
    }

    @Test
    public void updateCard_200_Ok() throws Exception {
        CardDto updateDto = new CardDto();
        updateDto.setId(card.getId());
        updateDto.setNumber("1234567890123456");
        updateDto.setHolder(card.getHolder());
        updateDto.setActive(ActiveStatus.ACTIVE);
        updateDto.setExpirationDate(LocalDate.now().plusYears(4));

        card.setActive(ActiveStatus.ACTIVE);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/card/update/" + card.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.number").value(updateDto.getNumber()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.ACTIVE.toString()));
    }

    @Test
    public void setActiveCard_200_Ok() throws Exception {
        card.setActive(ActiveStatus.INACTIVE);

        mockMvc.perform(MockMvcRequestBuilders
                    .put("/api/card/{id}/active", card.getId())
                    .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Card updatedCard = cardRepository.findById(card.getId()).get();
        Assertions.assertEquals(ActiveStatus.ACTIVE, updatedCard.getActive());
    }

    @Test
    public void serInactiveCard_200_Ok() throws Exception {
        card.setActive(ActiveStatus.ACTIVE);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/api/card/{id}/inactive", card.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());

        Card updatedCard = cardRepository.findById(card.getId()).get();
        Assertions.assertEquals(ActiveStatus.INACTIVE, updatedCard.getActive());
    }

    @Test
    @Transactional
    public void deleteCard_200_Ok() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders
                        .delete("/api/card/{id}/delete", card.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk());

        boolean exists = cardRepository.existsById(card.getId());
        Assertions.assertFalse(exists, "Card should be deleted from database");
    }
}
