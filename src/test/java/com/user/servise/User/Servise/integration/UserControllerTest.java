package com.user.servise.User.Servise.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservise.app.UserServiseApplication;
import com.userservise.app.model.dto.UserRequest;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;


@ActiveProfiles("test")
@SpringBootTest(classes = UserServiseApplication.class)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class UserControllerTest extends BaseIntegrationTest{

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private User savedUser;

    @BeforeEach
    void setUp() {
        savedUser = new User();
        savedUser.setName("Test");
        savedUser.setSurname("User");
        savedUser.setEmail("testUserMail@mail.com");
        savedUser.setBirthDate(LocalDate.of(2000, 1, 1));
        savedUser.setActive(ActiveStatus.ACTIVE);

        savedUser = userRepository.saveAndFlush(savedUser);
    }


    @Test
    public void getUserById_200_Ok() throws Exception {
        // Given:
        User user = savedUser;

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/user/" + user.getId())
                .accept(MediaType.APPLICATION_JSON));
        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(user.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(user.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(user.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value(user.getBirthDate().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(user.getActive().toString()));
    }

    @Test
    public void getUserById_404_NotFound() throws Exception {
        // Given:
        int id = 999;

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/user/" + id)
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void getAllUsers_200_Ok() throws Exception {
        // Given:
        String firstName = "Test";

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/user/all")
                .param("firstName", firstName)
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(savedUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(savedUser.getSurname()));

    }

    @Test
    public void createUser_201_Ok() throws Exception {
        // Given:
        UserRequest request = new UserRequest();
        request.setName("Name");
        request.setSurname("Surname");
        request.setBirthDate(LocalDate.of(2000, 1, 1));
        request.setEmail("NameSurname@mail.com");

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/user/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        response.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(request.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(request.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(request.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value(request.getBirthDate().toString()));
    }

    @Test
    public void updateUser_200_Ok() throws Exception {
        // Given:
        UserRequest request = new UserRequest();
        request.setName(savedUser.getName());
        request.setSurname(savedUser.getSurname());
        request.setBirthDate(savedUser.getBirthDate());
        request.setEmail("NewEmail@mail.com");

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/user/update/" + savedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedUser.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(request.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(request.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(request.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value(request.getBirthDate().toString()));
    }

    @Test
    public void setActiveUser_200_Ok() throws Exception {
        // Given:
        savedUser.setActive(ActiveStatus.INACTIVE);
        userRepository.saveAndFlush(savedUser);

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/user/{id}/active", savedUser.getId())
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedUser.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(savedUser.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.ACTIVE.toString()));
    }


    @Test
    public void setInactiveUser_200_Ok() throws Exception {
        // Given:
        savedUser.setActive(ActiveStatus.ACTIVE);

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/user/{id}/inactive", savedUser.getId())
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(savedUser.getId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(savedUser.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.INACTIVE.toString()));
    }

    @Test
    public void deleteUserById_200_Ok() throws Exception {
        // Given:
        User user = savedUser;

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/user/{id}", user.getId()));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk());

        boolean exists = userRepository.existsById(user.getId());
        Assertions.assertFalse(exists, "User should be deleted from database");
    }
}
