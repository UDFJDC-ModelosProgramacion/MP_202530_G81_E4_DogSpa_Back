package co.edu.udistrital.mdp.back.controllers;

import java.util.List;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import co.edu.udistrital.mdp.back.dto.UserDTO;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.services.NotificationUserService;

@RestController
@RequestMapping("/notifications")
public class NotificationUserController {

    private final NotificationUserService notificationUserService;
    private final ModelMapper modelMapper;

    public NotificationUserController(NotificationUserService notificationUserService, ModelMapper modelMapper) {
        this.notificationUserService = notificationUserService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{notificationId}/users")
    @ResponseStatus(HttpStatus.OK)
    public List<UserDTO> getUsers(@PathVariable Long notificationId) throws EntityNotFoundException {
        List<UserEntity> users = notificationUserService.getUsers(notificationId);
        return modelMapper.map(users, new TypeToken<List<UserDTO>>() {}.getType());
    }

    @PostMapping("/{notificationId}/users/{userId}")
    @ResponseStatus(HttpStatus.OK)
    public UserDTO addUser(@PathVariable Long notificationId, @PathVariable Long userId)
            throws EntityNotFoundException {
        UserEntity user = notificationUserService.addUserToNotification(notificationId, userId);
        return modelMapper.map(user, UserDTO.class);
    }

    @DeleteMapping("/{notificationId}/users/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(@PathVariable Long notificationId, @PathVariable Long userId)
            throws EntityNotFoundException {
        notificationUserService.removeUserFromNotification(notificationId, userId);
    }
}
