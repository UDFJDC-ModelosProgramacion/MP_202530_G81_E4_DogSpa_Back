package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.entities.NotificationEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import co.edu.udistrital.mdp.back.repositories.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserNotificationService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    public NotificationEntity addNotificationToUser(Long userId, Long notificationId) throws EntityNotFoundException {
        UserEntity user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Usuario no encontrado"));
        NotificationEntity notification = notificationRepository.findById(notificationId).orElseThrow(()-> new EntityNotFoundException("Notificación no encontrada")); 

        user.getNotifications().add(notification);
        userRepository.save(user);
        return notification;
    }

    public List<NotificationEntity> getNotifications(Long userId) throws EntityNotFoundException {
        UserEntity user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Usuario no encontrado"));
        return user.getNotifications();
    }

    public void removeNotificationFromUser(Long userID, Long notificationId) throws EntityNotFoundException {
        UserEntity user = userRepository.findById(UserId).orElseThrow(()-> new EntityNotFoundException("Usuario no encontrado"));
        NotificationEntity notification = notificationRepository.findById(notificationId).orElseThrow(()-> new EntityNotFoundException("Notificación no encontrada"));
         user.getNotifications().remove(notification);
         userRepository.save(user);
    }
}
