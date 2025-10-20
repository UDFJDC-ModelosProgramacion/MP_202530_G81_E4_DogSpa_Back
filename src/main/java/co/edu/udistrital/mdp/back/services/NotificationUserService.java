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
public class NotificationUserService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String NOTIFICATION_NOT_FOUND = "Notificación no encontrada";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationUserService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public UserEntity addUserToNotification(Long notificationId, Long userId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        notification.getUsers().add(user);
        notificationRepository.save(notification);
        return user;
    }

    public List<UserEntity> getUsers(Long notificationId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND));
        return notification.getUsers();
    }

    @Transactional
    public void removeUserFromNotification(Long notificationId, Long userId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND));
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND));

        notification.getUsers().remove(user);
        notificationRepository.save(notification);
    }
}
