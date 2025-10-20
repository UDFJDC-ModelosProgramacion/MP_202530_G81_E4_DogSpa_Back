package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private static final String USER_NOT_FOUND = "Usuario no encontrado";
    private static final String INVALID_EMAIL = "Correo inválido";

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity createUser(UserEntity user) throws IllegalOperationException {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalOperationException(INVALID_EMAIL);
        }
        return userRepository.save(user);
    }

    public UserEntity getUser(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException(USER_NOT_FOUND);
        }
        return user.get();
    }

    public UserEntity updateUser(Long id, UserEntity user)
            throws IllegalOperationException {

        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(USER_NOT_FOUND);
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalOperationException(INVALID_EMAIL);
        }

        user.setId(id);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException(USER_NOT_FOUND);
        }
        userRepository.deleteById(id);
    }
}
