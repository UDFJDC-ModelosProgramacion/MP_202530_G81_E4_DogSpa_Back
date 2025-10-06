package co.edu.udistrital.mdp.back.services;

import co.edu.udistrital.mdp.back.entities.UserEntity;
import co.edu.udistrital.mdp.back.exceptions.EntityNotFoundException;
import co.edu.udistrital.mdp.back.exceptions.IllegalOperationException;
import co.edu.udistrital.mdp.back.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserEntity createUser(UserEntity user) throws IllegalOperationException {
        if (user.getEmail() == null || !user.getEmail().contains("@")) {
            throw new IllegalOperationException("Correo inválido");
        }
        return userRepository.save(user);
    }

    public UserEntity getUser(Long id) throws EntityNotFoundException {
        Optional<UserEntity> user = userRepository.findById(id);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        return user.get();
    }

    public UserEntity updateUser(Long id, UserEntity user)
            throws EntityNotFoundException, IllegalOperationException {

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

    public void deleteUser(Long id) throws EntityNotFoundException {
        Optional<UserEntity> existing = userRepository.findById(id);
        if (existing.isEmpty()) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
        userRepository.deleteById(id);
    }
}
