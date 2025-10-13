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
public class NotificationUserService {
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    public UserEntity addUserToNotification(Long notificationId, Long userId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElseThrow(()-> new EntityNotFoundException("Notificación no encontrada"));
        UserEntity user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Usuario no encontrado"));

        notification.getUsers().add(user);
        notificationRepository.save(notification);
        return user;
    }

    public List<UserEntity> getUsers(Long notificationId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElseThrow(()-> new EntityNotFoundException("Notificación no encontrada"));
        return notification.getUsers();
    }

    public void removeUserFromNotification(Long notificationId, Long userId) throws EntityNotFoundException {
        NotificationEntity notification = notificationRepository.findById(notificationId).orElseThrow(()-> new EntityNotFoundException("Notificación no encontrada"));
        UserEntity user = userRepository.findById(userId).orElseThrow(()-> new EntityNotFoundException("Usuario no encontrado"));

        notification.getUsers().remove(user);
        notificationRepository.save(notification);
    }
}
