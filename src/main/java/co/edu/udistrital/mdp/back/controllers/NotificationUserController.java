package co.edu.udistrital.mdp.back.controllers;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.NotificationUserService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationUserController {

    private final NotificationUserService notificationUserService;

    @GetMapping("/{notificationId}/users")
    @ResponseStatus(code = HttpStatus.OK)
    public List<UserEntity> getUsers(@PathVariable Long notificationId) throws EntityNotFoundException {
        return notificationUserService.getUsers(notificationId);
    }

    @PostMapping("/{notificationId}/users/{userId}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserEntity addUser(@PathVariable Long notificationId, @PathVariable Long userId)
            throws EntityNotFoundException {
        return notificationUserService.addUserToNotification(notificationId, userId);
    }

    @DeleteMapping("/{notificationId}/users/{userId}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable Long notificationId, @PathVariable Long userId)
            throws EntityNotFoundException {
        notificationUserService.removeUserFromNotification(notificationId, userId);
    }
}
