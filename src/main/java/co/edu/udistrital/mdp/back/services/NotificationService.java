    package co.edu.udistrital.mdp.back.services;

    import co.edu.udistrital.mdp.back.entities.NotificationEntity;
    import co.edu.udistrital.mdp.back.entities.UserEntity;
    import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
    import co.edu.udistrital.mdp.back.repositories.NotificationRepository;
    import co.edu.udistrital.mdp.back.repositories.UserRepository;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.sql.Date;
    import java.util.List;

    @Service
    public class NotificationService {

        @Autowired
        private NotificationRepository notificationRepository;

        @Autowired
        private UserRepository userRepository;

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
            notification.setUser(user);
            return notificationRepository.save(notification);
        }

        // Regla: Marcar notificación como leída
        @Transactional
        public void markAsRead(Long notificationId) throws EntityNotFoundException {
            NotificationEntity notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
            notification.setRead(true);
            notificationRepository.save(notification);
        }

        // Regla: Obtener notificaciones de un usuario (no leídas o todas)
        public List<NotificationEntity> getUserNotifications(Long userId, boolean onlyUnread) {
            if (onlyUnread) {
                return notificationRepository.findByUserIdAndReadFalse(userId);
            } else {
                return notificationRepository.findByUserId(userId);
            }
        }

        // Regla: Eliminar notificación (solo si ya fue leída)
        @Transactional
        public void deleteNotification(Long notificationId) throws EntityNotFoundException {
            NotificationEntity notification = notificationRepository.findById(notificationId)
                    .orElseThrow(() -> new EntityNotFoundException("Notification not found"));
            if (!Boolean.TRUE.equals(notification.getRead())) {
                throw new IllegalArgumentException("Cannot delete unread notification");
            }
            notificationRepository.delete(notification);
        }
    }
