package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserEntity createUser(UserEntity user) throws IllegalOperationException {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalOperationException("Correo inválido");
        }
        return userRepository.save(user);
    }

    public UserEntity getUser(Long id) {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        return user.get();
    }

    public UserEntity updateUser(Long id, UserEntity user)
            throws IllegalOperationException {

        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }

        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalOperationException("Correo inválido");
        }

        user.setId(id);
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }
}
