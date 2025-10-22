package co.edu.udistrital.mdp.back.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import co.edu.udistrital.mdp.back.dto.UserDTO;
import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.services.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final ModelMapper modelMapper;

    public UserController(UserService userService, ModelMapper modelMapper) {
        this.userService = userService;
        this.modelMapper = modelMapper;
    }

    @GetMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDTO findOne(@PathVariable("id") Long id) {
        UserEntity user = userService.getUser(id);
        return modelMapper.map(user, UserDTO.class);
    }

    @PostMapping
    @ResponseStatus(code = HttpStatus.CREATED)
    public UserDTO create(@RequestBody UserDTO userDTO) throws IllegalOperationException {
        UserEntity user = modelMapper.map(userDTO, UserEntity.class);
        UserEntity newUser = userService.createUser(user);
        return modelMapper.map(newUser, UserDTO.class);
    }

    @PutMapping("/{id}")
    @ResponseStatus(code = HttpStatus.OK)
    public UserDTO update(@PathVariable("id") Long id, @RequestBody UserDTO userDTO)
            throws IllegalOperationException {
        UserEntity user = modelMapper.map(userDTO, UserEntity.class);
        UserEntity updatedUser = userService.updateUser(id, user);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }
}
