package co.edu.udistrital.mdp.back.controllers;
import java.lang.reflect.Type;

import co.edu.udistrital.mdp.back.entities.NotificationEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.NotificationService;


import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.modelmapper.TypeToken;

import java.util.List;


import co.edu.udistrital.mdp.back.dto.NotificationDTO;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final ModelMapper modelMapper;

    public NotificationController(NotificationService notificationService, ModelMapper modelMapper) {
        this.notificationService = notificationService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<NotificationDTO> listAll() {
        List<NotificationEntity> notifications = notificationService.getAll();
        Type listType = new TypeToken<List<NotificationDTO>>() {}.getType();
        return modelMapper.map(notifications, listType);
    }

    @GetMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.OK)
    public NotificationDTO getOne(@PathVariable Long notificationId) throws EntityNotFoundException {
        NotificationEntity notification = notificationService.getById(notificationId);
        return modelMapper.map(notification, NotificationDTO.class);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationDTO create(@RequestBody NotificationDTO body)
            throws EntityNotFoundException {
        NotificationEntity notification = notificationService.createNotification(body.getMessage(), body.getUserIds());
        return modelMapper.map(notification, NotificationDTO.class);
    }

    @PutMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.OK)
    public NotificationDTO updateMessage(@PathVariable Long notificationId,
                                            @RequestBody NotificationDTO body)
            throws EntityNotFoundException {
        NotificationEntity updatedNotification = notificationService.updateMessage(notificationId, body.getMessage());
        return modelMapper.map(updatedNotification, NotificationDTO.class);
    }

    @DeleteMapping("/{notificationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long notificationId) throws EntityNotFoundException {
        notificationService.deleteNotification(notificationId);
    }

    /* ========================= ESTADO (READ) ========================= */

    @PatchMapping("/{notificationId}/read")
    @ResponseStatus(HttpStatus.OK)
    public void markAsRead(@PathVariable Long notificationId) throws EntityNotFoundException {
        notificationService.markAsRead(notificationId);
    }

    /* (Opcional) marcar todas como leídas para un usuario */
    @PatchMapping("/users/{userId}/mark-all-read")
    @ResponseStatus(HttpStatus.OK)
    public int markAllAsReadForUser(@PathVariable Long userId) {
        return notificationService.markAllAsReadForUser(userId);
    }

   
    /* ========================= ASOCIACIÓN USUARIOS ========================= */

    @PostMapping("/{notificationId}/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public NotificationDTO addUser(@PathVariable Long notificationId, @PathVariable Long userId)
            throws EntityNotFoundException {
        NotificationEntity notification = notificationService.addUserToNotification(notificationId, userId);
        return modelMapper.map(notification, NotificationDTO.class);
    }

    @DeleteMapping("/{notificationId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable Long notificationId, @PathVariable Long userId)
            throws EntityNotFoundException {
        notificationService.removeUserFromNotification(notificationId, userId);
    }

}
