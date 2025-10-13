package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(UserService.class)
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        UserEntity u = new UserEntity();
        u.setName("BaseUser");
        u.setLastname("Perez");
        u.setEmail("base@user.com");
        u.setTelephone("3010000000");
        u.setAddress("Calle Base");
        u.setPassword("pass123");
        u.setLoyaltypoints(100);
        userRepository.save(u);
    }

    @Test
    void testCreateUser_valid_ok() throws IllegalOperationException {
        UserEntity user = new UserEntity();
        user.setName("Nuevo");
        user.setLastname("Cliente");
        user.setEmail("nuevo@user.com");
        user.setTelephone("3021111111");
        user.setAddress("Calle 10");
        user.setPassword("clave321");
        user.setLoyaltypoints(200);

        UserEntity result = userService.createUser(user);
        assertNotNull(result);
        assertEquals("Nuevo", result.getName());
    }

    @Test
    void testCreateUser_invalidEmail_exception() {
        UserEntity user = new UserEntity();
        user.setName("Pepe");
        user.setLastname("Martinez");
        user.setEmail("correoSinArroba");
        user.setTelephone("3032222222");
        user.setAddress("Calle 12");
        user.setPassword("clave123");
        user.setLoyaltypoints(0);

        assertThrows(IllegalOperationException.class, () -> {
            userService.createUser(user);
        });
    }

    @Test
    void testGetUser_valid_ok() throws EntityNotFoundException {
        UserEntity base = userRepository.findAll().get(0);
        UserEntity found = userService.getUser(base.getId());
        assertEquals(base.getEmail(), found.getEmail());
    }

    @Test
    void testGetUser_notFound_exception() {
        assertThrows(EntityNotFoundException.class, () -> {
            userService.getUser(9999L);
        });
    }
}
