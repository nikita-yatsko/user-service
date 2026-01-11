package com.user.servise.User.Servise.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.userservise.app.UserServiceApplication;
import com.userservise.app.model.dto.UserRequest;
import com.userservise.app.model.entity.User;
import com.userservise.app.model.enums.ActiveStatus;
import com.userservise.app.repository.UserRepository;
import com.userservise.app.security.model.CustomUserDetails;
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
        userRepository.deleteAll();

        savedUser = new User();
        savedUser.setUserId(1);
        savedUser.setName("Test");
        savedUser.setSurname("User");
        savedUser.setEmail("testUserMail@mail.com");
        savedUser.setBirthDate(LocalDate.of(2000, 1, 1));
        savedUser.setActive(ActiveStatus.ACTIVE);

        savedUser = userRepository.saveAndFlush(savedUser);
    }


    @Test
    public void getUserByIdReturn200Ok() throws Exception {
        // Given:
        User user = savedUser;
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/user/" + user.getUserId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));
        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(user.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(user.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(user.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(user.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value(user.getBirthDate().toString()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(user.getActive().toString()));
    }

    @Test
    public void getUserByIdReturn404NotFound() throws Exception {
        // Given:
        int id = 999;
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/user/" + id)
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    public void getAllUsersReturn200Ok() throws Exception {
        // Given:
        String firstName = "Test";
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/user/all")
                .with(authentication(auth))
                .param("firstName", firstName)
                .accept(MediaType.APPLICATION_JSON));

        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].name").value(savedUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.content[0].surname").value(savedUser.getSurname()));

    }

    @Test
    public void createUserReturn201Ok() throws Exception {
        // Given:
        UserRequest request = new UserRequest();
        request.setUserId(2);
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
    public void updateUserReturn200Ok() throws Exception {
        // Given:
        UserRequest request = new UserRequest();
        request.setName(savedUser.getName());
        request.setSurname(savedUser.getSurname());
        request.setBirthDate(savedUser.getBirthDate());
        request.setEmail("NewEmail@mail.com");

        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/user/update/" + savedUser.getUserId())
                .with(authentication(auth))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(request.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(request.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(request.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.birthDate").value(request.getBirthDate().toString()));
    }

    @Test
    public void setActiveUserReturn200Ok() throws Exception {
        // Given:
        savedUser.setActive(ActiveStatus.INACTIVE);
        userRepository.saveAndFlush(savedUser);
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/user/{id}/active", savedUser.getUserId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(savedUser.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.ACTIVE.toString()));
    }


    @Test
    public void setInactiveUserReturn200Ok() throws Exception {
        // Given:
        savedUser.setActive(ActiveStatus.ACTIVE);
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .put("/api/user/{id}/inactive", savedUser.getUserId())
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value(savedUser.getName()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.surname").value(savedUser.getSurname()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value(savedUser.getEmail()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.active").value(ActiveStatus.INACTIVE.toString()));
    }

    @Test
    public void deleteUserByIdReturn200Ok() throws Exception {
        // Given:
        User user = savedUser;
        CustomUserDetails principal = new CustomUserDetails(1L, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions response = mockMvc.perform(MockMvcRequestBuilders
                .delete("/api/user/{userId}", user.getUserId())
                .with(authentication(auth)));

        // Then:
        response.andExpect(MockMvcResultMatchers.status().isOk());

        boolean exists = userRepository.existsById(user.getUserId());
        Assertions.assertFalse(exists, "User should be deleted from database");
    }
}
