package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.NotificationEntity;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.repositories.NotificationRepository;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Service
public class NotificationService {

    private static final String NOTIFICATION_NOT_FOUND_MESSAGE = "Notification not found";
    private static final String USER_NOT_FOUND_MESSAGE = "User not found";

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // Constructor injection
    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // Regla: Crear notificación para uno o varios usuarios
    @Transactional
    public NotificationEntity createNotification(String message, List<Long> userIds) throws EntityNotFoundException {
        if (message == null || message.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        List<UserEntity> users = userRepository.findAllById(userIds);
        if (users.isEmpty()) {
            throw new EntityNotFoundException("No users found for notification");
        }
        NotificationEntity notification = new NotificationEntity();
        notification.setMessage(message);
        notification.setDate(new Date(System.currentTimeMillis()));
        notification.setRead(false);
        notification.setUsers(users);
        return notificationRepository.save(notification);
    }

    public List<NotificationEntity> getAll() {
        return notificationRepository.findAll();
    }

    public NotificationEntity getById(Long notificationId) throws EntityNotFoundException {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND_MESSAGE));
    }


    // Regla: Marcar notificación como leída
    @Transactional
    public void markAsRead(Long notificationId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND_MESSAGE));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public int markAllAsReadForUser(Long userId) {
        List<NotificationEntity> list = notificationRepository.findByUsers_IdAndReadFalse(userId);
        list.forEach(n -> n.setRead(true));
        notificationRepository.saveAll(list);
        return list.size();
    }


    // Regla: Obtener notificaciones de un usuario (no leídas o todas)
    public List<NotificationEntity> getUserNotifications(Long userId, boolean onlyUnread) {
        if (onlyUnread) {
            return notificationRepository.findByUsers_IdAndReadFalse(userId);
        } else {
            return notificationRepository.findByUsers_Id(userId);
        }
    }

    @Transactional
    public NotificationEntity updateMessage(Long notificationId, String newMessage)
            throws EntityNotFoundException {
        if (newMessage == null || newMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Message cannot be empty");
        }
        NotificationEntity n = getById(notificationId);
        n.setMessage(newMessage.trim());
        return notificationRepository.save(n);
    }

    @Transactional
    public NotificationEntity addUserToNotification(Long notificationId, Long userId)
            throws EntityNotFoundException {
        NotificationEntity n = getById(notificationId);
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
        if (!n.getUsers().contains(u)) {
            n.getUsers().add(u);
        }
        return notificationRepository.save(n);
    }

    @Transactional
    public void removeUserFromNotification(Long notificationId, Long userId)
            throws EntityNotFoundException {
        NotificationEntity n = getById(notificationId);
        UserEntity u = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(USER_NOT_FOUND_MESSAGE));
        n.getUsers().remove(u);
        notificationRepository.save(n);
    }


    // Regla: Eliminar notificación (solo si ya fue leída)
    @Transactional
    public void deleteNotification(Long notificationId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException(NOTIFICATION_NOT_FOUND_MESSAGE));
        if (!Boolean.TRUE.equals(notification.getRead())) {
            throw new IllegalArgumentException("Cannot delete unread notification");
        }
        notificationRepository.delete(notification);
    }
}