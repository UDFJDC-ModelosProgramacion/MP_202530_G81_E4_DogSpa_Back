package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.entities.NotificationEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import co.edu.udistrital.mdp.back.repositories.NotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserNotificationService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String NOTIFICATION_NOT_FOUND = "Notificación no encontrada";

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public UserNotificationService(UserRepository userRepository, NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    @Transactional
    public NotificationEntity addNotificationToUser(Long userId, Long notificationId) throws EntityNotFoundException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND));

        user.getNotifications().add(notification);
        userRepository.save(user);
        return notification;
    }

    public List<NotificationEntity> getNotifications(Long userId) throws EntityNotFoundException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        return user.getNotifications();
    }

    @Transactional
    public void removeNotificationFromUser(Long userId, Long notificationId) throws EntityNotFoundException {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND));

        user.getNotifications().remove(notification);
        userRepository.save(user);
    }
}
