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
    void testGetUser_valid_ok() {
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

    @Test
    void testUpdateUser_valid_ok() throws Exception {
        UserEntity base = userRepository.findAll().get(0);
        base.setName("Actualizado");
        UserEntity result = userService.updateUser(base.getId(), base);
        assertEquals("Actualizado", result.getName());
    }

    @Test
    void testUpdateUser_notFound_exception() {
        UserEntity fake = new UserEntity();
        fake.setId(999L);
        fake.setName("Falso");
        fake.setEmail("falso@correo.com");

        assertThrows(EntityNotFoundException.class, () -> {
            userService.updateUser(999L, fake);
        });
    }

    @Test
    void testDeleteUser_valid_ok() {
        UserEntity base = userRepository.findAll().get(0);
        userService.deleteUser(base.getId());
        assertTrue(userRepository.findAll().isEmpty());
    }

    @Test
    void testDeleteUser_notFound_exception() {
        assertThrows(EntityNotFoundException.class, () -> {
            userService.deleteUser(123456L);
        });
    }
}
