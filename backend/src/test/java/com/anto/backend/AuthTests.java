package com.anto.backend;

import com.anto.backend.service.JwtService;
import com.anto.backend.service.UserService;
import com.anto.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthTests {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(1, "test@test.com", "USER", List.of("CREATE_RECIPE"));
        assertNotNull(token);
    }

    @Test
    void testTokenIsValid() {
        String token = jwtService.generateToken(1, "test@test.com", "USER", List.of("CREATE_RECIPE"));
        assertTrue(jwtService.isTokenValid(token));
    }

    @Test
    void testExtractUserId() {
        String token = jwtService.generateToken(1, "test@test.com", "USER", List.of("CREATE_RECIPE"));
        assertEquals(1, jwtService.extractUserId(token));
    }

    @Test
    void testExtractRole() {
        String token = jwtService.generateToken(1, "test@test.com", "ADMIN", List.of("DELETE_ANY_RECIPE"));
        assertEquals("ADMIN", jwtService.extractRole(token));
    }

    @Test
    void testExtractEmail() {
        String token = jwtService.generateToken(1, "test@test.com", "USER", List.of("CREATE_RECIPE"));
        assertEquals("test@test.com", jwtService.extractEmail(token));
    }

    @Test
    void testRegisterAndLogin() {
        userService.register("Test User", "newuser@test.com", "testuser", "Password1!", "none", "pet name", "fluffy");
        User loggedIn = userService.login("newuser@test.com", "Password1!");
        assertNotNull(loggedIn);
        assertEquals("newuser@test.com", loggedIn.getEmail());
    }

    @Test
    void testLoginWrongPassword() {
        userService.register("Test User", "wrong@test.com", "wronguser", "Password1!", "none", "pet name", "fluffy");
        assertThrows(RuntimeException.class, () -> userService.login("wrong@test.com", "WrongPassword!"));
    }

    @Test
    void testGetById() {
        User registered = userService.register("Get User", "getuser@test.com", "getuser", "Password1!", "none", "pet name", "fluffy");
        User found = userService.getUserById(registered.getId());
        assertEquals(registered.getId(), found.getId());
    }

    @Test
    void testGetAll() {
        userService.register("List User", "listuser@test.com", "listuser", "Password1!", "none", "pet name", "fluffy");
        assertFalse(userService.getAllUsers().isEmpty());
    }

    @Test
    void testRegisterDuplicateEmail() {
        userService.register("Dup User", "dup@test.com", "dupuser", "Password1!", "none", "pet name", "fluffy");
        assertThrows(RuntimeException.class, () ->
                userService.register("Dup User2", "dup@test.com", "dupuser2", "Password1!", "none", "pet name", "fluffy"));
    }
}