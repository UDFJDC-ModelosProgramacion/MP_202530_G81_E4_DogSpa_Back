package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.NotificationEntity;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.repositories.NotificationRepository;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Import(NotificationUserService.class)
class NotificationUserServiceTest {

    @Autowired
    private NotificationUserService notificationUserService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    private NotificationEntity notification;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        notification = new NotificationEntity();
        notification.setMessage("Actualización disponible");
        notificationRepository.save(notification);

        user = new UserEntity();
        user.setEmail("leonardo@udistrital.com");
        user.setName("Leonardo");
        userRepository.save(user);
    }

    @Test
    void testAddUserToNotification() throws Exception {
        notificationUserService.addUserToNotification(notification.getId(), user.getId());
        List<UserEntity> users = notificationUserService.getUsers(notification.getId());
        assertEquals(1, users.size());
        assertEquals(user.getEmail(), users.get(0).getEmail());
    }

    @Test
    void testRemoveUserFromNotification() throws Exception {
        notificationUserService.addUserToNotification(notification.getId(), user.getId());
        notificationUserService.removeUserFromNotification(notification.getId(), user.getId());
        List<UserEntity> users = notificationUserService.getUsers(notification.getId());
        assertTrue(users.isEmpty());
    }
}
