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
@Import(UserNotificationService.class)
class UserNotificationServiceTest {

    @Autowired
    private UserNotificationService userNotificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private UserEntity user;
    private NotificationEntity notification;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setEmail("user@udistrital.com");
        user.setName("Leonardo");
        userRepository.save(user);

        notification = new NotificationEntity();
        notification.setMessage("Bienvenido al sistema");
        notificationRepository.save(notification);
    }

    @Test
    void testAddNotificationToUser() throws Exception {
        userNotificationService.addNotificationToUser(user.getId(), notification.getId());
        List<NotificationEntity> notifications = userNotificationService.getNotifications(user.getId());
        assertEquals(1, notifications.size());
        assertEquals(notification.getMessage(), notifications.get(0).getMessage());
    }

    @Test
    void testRemoveNotificationFromUser() throws Exception {
        userNotificationService.addNotificationToUser(user.getId(), notification.getId());
        userNotificationService.removeNotificationFromUser(user.getId(), notification.getId());
        List<NotificationEntity> notifications = userNotificationService.getNotifications(user.getId());
        assertTrue(notifications.isEmpty());
    }
}
