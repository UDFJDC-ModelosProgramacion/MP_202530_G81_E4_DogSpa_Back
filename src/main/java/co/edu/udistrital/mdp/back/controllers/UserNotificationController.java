package co.edu.udistrital.mdp.back.controllers;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import co.edu.udistrital.mdp.back.dto.NotificationDTO;
import co.edu.udistrital.mdp.back.entities.NotificationEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.UserNotificationService;

@RestController
@RequestMapping("/api/users")
public class UserNotificationController {

    private final UserNotificationService userNotificationService;
    private final ModelMapper modelMapper;

    public UserNotificationController(UserNotificationService userNotificationService, ModelMapper modelMapper) {
        this.userNotificationService = userNotificationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{userId}/notifications")
    @ResponseStatus(HttpStatus.OK)
    public List<NotificationDTO> getNotifications(@PathVariable Long userId) throws EntityNotFoundException {
        List<NotificationEntity> notifications = userNotificationService.getNotifications(userId);
        return modelMapper.map(notifications, new TypeToken<List<NotificationDTO>>() {}.getType());
    }

    @PostMapping("/{userId}/notifications/{notificationId}")
    @ResponseStatus(HttpStatus.OK)
    public NotificationDTO addNotification(@PathVariable Long userId, @PathVariable Long notificationId)
            throws EntityNotFoundException {
        NotificationEntity notification = userNotificationService.addNotificationToUser(userId, notificationId);
        return modelMapper.map(notification, NotificationDTO.class);
    }

    @DeleteMapping("/{userId}/notifications/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeNotification(@PathVariable Long userId, @PathVariable Long notificationId)
            throws EntityNotFoundException {
        userNotificationService.removeNotificationFromUser(userId, notificationId);
    }
}
